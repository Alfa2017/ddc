package ddc.util;

public class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static String getMessage(Throwable t) {
        String defaultErrorMessage = "Неизвестная ошибка";
        if (t == null) return defaultErrorMessage;
        if (t.getMessage() != null) {
            return t.getMessage();
        }
        Throwable rootCause = t.getCause();
        String errorMessage;
        if (rootCause != null && (errorMessage = rootCause.getMessage()) != null) {
            return errorMessage;
        }
        return defaultErrorMessage;
    }
}
