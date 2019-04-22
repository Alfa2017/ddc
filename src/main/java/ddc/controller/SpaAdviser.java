package ddc.controller;

import ddc.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.iteco.aft.dds.exception.*;
import ddc.model.response.ApiErrorResponse;
import ddc.model.response.ApiValidationErrorResponse;
import ddc.model.response.Response;
import ddc.service.slf4j.DdsLogMarker;
import ddc.service.storage.exceptions.StorageServiceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

/**
 * Single Page Application (SPA) Adviser
 */
@Slf4j
@Component
@ControllerAdvice
class SpaAdviser {

    /**
     * Все страницы ведут на главную
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public String notFound(NoHandlerFoundException ex, HttpServletRequest request, HttpServletResponse response) {
        if (request.getRequestURI().startsWith("/api/")) {
            log.debug("Ресурс по пути {} не найден", ex.getRequestURL());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "forward:/static/index.html";
        }
        log.trace("Возвращаю  react app {}", ex.getRequestURL());
        return "forward:/static/index.html";
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<Response> promiseException(CompletionException ex) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getCause().getMessage();
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().startsWith("ru.iteco")) {
                sb.append(stackTraceElement.getClassName()).append(".")
                        .append(stackTraceElement.getMethodName()).append(":")
                        .append(stackTraceElement.getLineNumber()).append("<-");
            }
        }
        log.error("Ошибка асинхронного запроса: {} domainStackTrace: {}", message, sb);
        return new ResponseEntity<>(new ApiErrorResponse(message), httpStatus);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleBigFileException(MaxUploadSizeExceededException ex) {
        String message = "Загружаемый файл не должен превышать 100 мегабайт";
        log.error(DdsLogMarker.UI, message);
        return new ResponseEntity<>(new ApiErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Response> handleAll(Exception ex) {
        if (ex instanceof NotFoundDdsException) {
            HttpStatus httpStatus = HttpStatus.NOT_FOUND;
            String body = ex.getMessage();
            log.error("Ошибка: {}", ex.getMessage());
            return new ResponseEntity<>(new ApiErrorResponse(body), httpStatus);
        } else if (ex instanceof StorageServiceException || ex instanceof RuntimeStorageServiceException) {
            HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            String body = ex.getMessage();
            log.error(DdsLogMarker.UI, ex.getMessage(), ex.getClass().getSimpleName());
            return new ResponseEntity<>(new ApiErrorResponse(body), httpStatus);
        } else if (ex instanceof ObjectAlreadyExistDdsException) {
            HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
            String body = ex.getMessage();
            log.error("Ошибка: {}", ex.getMessage());
            return new ResponseEntity<>(new ApiErrorResponse(body), httpStatus);
        } else if (ex instanceof ServerErrorDdsException) {
            HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            String body = ex.getMessage();
            StackTraceElement stackTraceElement = ex.getStackTrace()[0];
            log.error("{}@{}[{}]: {}", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber(), ex.getMessage());
            return new ResponseEntity<>(new ApiErrorResponse(body), httpStatus);
        } else {
            log.error("Ошибка {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
            return new ResponseEntity<>(new ApiErrorResponse("INTERNAL_SERVER_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler({ClientAbortException.class})
    public void handleClientAbortEx(ClientAbortException ex) {
        log.warn("Обрыв соединения websocket и client (был рефреш страницы?)");
    }


    @ExceptionHandler({HttpMediaTypeNotAcceptableException.class})
    public void httpMediaException(HttpMediaTypeNotAcceptableException ex){
        log.warn("{}: {}",ex.getClass().getSimpleName(), ex.getMessage());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiValidationErrorResponse handleMissingParametrException(MissingServletRequestParameterException ex) {
        List<ApiValidationErrorResponse.Field> fields = new ArrayList<>();
        fields.add(new ApiValidationErrorResponse.Field(ex.getParameterName(), ex.getLocalizedMessage()));

        return new ApiValidationErrorResponse(fields);
    }


    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiValidationErrorResponse handleValidationException(MethodArgumentTypeMismatchException ex) {
        List<ApiValidationErrorResponse.Field> fields = new ArrayList<>();
        fields.add(new ApiValidationErrorResponse.Field(ex.getPropertyName(), ex.getLocalizedMessage()));
        return new ApiValidationErrorResponse(fields);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiValidationErrorResponse handleValidationException(MethodArgumentNotValidException ex) {

        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> "{\'свойство\':\'" + fe.getField() + "\', \'причина\':\'" + fe.getDefaultMessage() + "\', \'отклоненное значение\':" + fe.getRejectedValue() + "\'}")
                .reduce("", (acc, elem) -> {
                    String delim = "";
                    if (!acc.isEmpty()) delim = ", ";
                    return acc + delim + elem;
                });
        log.error("Ошибка валидации: {}", errorMsg);

        List<ApiValidationErrorResponse.Field> fields = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(field -> {
            fields.add(new ApiValidationErrorResponse.Field(field.getField(), field.getDefaultMessage()));
        });
        return new ApiValidationErrorResponse(fields);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiErrorResponse handleEmptyRequest(HttpMessageNotReadableException ex) {

        return new ApiErrorResponse("Пустой запрос невозможен");
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiValidationErrorResponse handleBindException(BindException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse(ex.getMessage());
        List<ApiValidationErrorResponse.Field> fields = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(field -> {
            fields.add(new ApiValidationErrorResponse.Field(field.getField(), field.getDefaultMessage()));
        });
        log.error(errorMsg);
        return new ApiValidationErrorResponse(fields);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Response> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Ошибка: {}", ex.getMessage());
        return new ResponseEntity<>(new ApiErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReportDdsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Response> handleReportDdsException(ReportDdsException ex) {
        log.error("Ошибка: {}", ex.getMessage());
        return new ResponseEntity<>(new ApiErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(EofException.class)
//    public void handleEofException(EofException eofe) {
//        String message = Optional.ofNullable(eofe.getMessage())
//                .orElse(eofe.getCause() == null ? "" :  eofe.getCause().getMessage());
//        log.debug("Разрыв соединения: {}", message );
//    }
}