package ddc.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.CryptoPro.JCP.JCP;
import ddc.AppState;
import ddc.crypto.CryptoUtils;
import ddc.model.UserContext;
import ddc.model.enums.UserRole;
import javax.servlet.http.Part;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import userSamples.Constants;

/**
 * @author pc 04.10.18
 */
@Aspect
@Component
@Slf4j
public class AuthAspect {

    @Autowired
    private AppState appState;

    @Autowired
    private SessionInfo sessionInfo;


    @Around("@annotation(authorized) || @within(authorized)")
    public Object authorized(ProceedingJoinPoint pjp, Authorized authorized) throws Throwable {
        initSessionInfo();

        HttpServletResponse httpResponse = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Unauthorized unauthorized = method.getAnnotation(Unauthorized.class);
        if (unauthorized == null) {
            Authorized methodAnnotation = method.getAnnotation(Authorized.class);
            if (methodAnnotation != null) {
                authorized = methodAnnotation;
            }
            if (!isAuthorized()) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return null;
            }
            if (!checkRole(authorized.value())) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                log.warn("Action not allowed for {}", sessionInfo.getUserRole());
                return null;
            }
        }
        httpResponse.setHeader("token", this.sessionInfo.getSessionToken());
        return pjp.proceed();
    }

    public void initSessionInfo() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String bodyData = null;
        byte[] bodyBytes = null;
        String bodySign = null;
        String token = null;
        String sessionToken = null;
        try {
            if( request.getMethod().equalsIgnoreCase("POST") ) {
                if( request.getContentType().indexOf("multipart/") > -1 ) {

                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (Part part : request.getParts()) {
                        String partName = part.getName();

                        // записываем имя секции
                        baos.write(partName.getBytes("UTF-8"));

                        // записываем тело секции
                        final byte[] b = new byte[1024];
                        InputStream is = part.getInputStream();
                        int c;
                        try {
                            while ((c = is.read(b, 0, b.length)) > 0) {
                                baos.write(b, 0, c);
                            }
                        }
                        finally {
                            is.close();
                        }
                    }
                    bodyBytes = baos.toByteArray();
                }
                else {
                    bodyData = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));  // получаем данные запроса
                    bodyBytes = bodyData.getBytes("UTF-8");
                }
            }
            else if(request.getMethod().equalsIgnoreCase("GET")) {
                bodyData = request.getQueryString();
                if( bodyData != null && bodyData != "" ) {
                    bodyBytes = bodyData.getBytes("UTF-8");
                }
            }
            else {
                log.error("unknown method");
                return;
            }

            token = request.getHeader("token");
            bodySign = request.getHeader("clisig");

            if( StringUtils.isEmpty(token) ) {
                return;
            }

            if( bodySign == null ) {
                return;
            }

            //20190416
            try {
                SessionInfo sessionInfo = appState.getSessions().get(token);
                String userId = sessionInfo.getAccountAddress();
            }
            catch(Exception ex) {

            }

            UserContext userContext = appState.getUserContext().get(token);

            //
            String rqUrl = request.getServletPath(); //request.getRequestURL().toString();

            byte[] certBytes = CryptoUtils.fromHexString(CryptoUtils.removePrefix(userContext.getClientPubKey()));
            byte[] tokenBytes = CryptoUtils.fromHexString(CryptoUtils.removePrefix(token));
            byte[] signBytes = CryptoUtils.fromHexString(CryptoUtils.removePrefix(bodySign));

            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            outputStream.write( tokenBytes );
            if( bodyBytes != null ) {
                outputStream.write(bodyBytes);
            }
            byte[] dd2 = outputStream.toByteArray( );

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate userCert = (X509Certificate)(certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes)));

            // проверяем подпись
            final boolean verifies = CryptoUtils.verify(signBytes, dd2, userCert, JCP.GOST_SIGN_2012_256_NAME);  // JCP.RAW_GOST_SIGN_2012_256_NAME -> без(!) дополнительного хэширования => ограничене на размер входящего массива
            if( verifies ) {
                //генерируем новый сессионный токен
                final byte[] randomBytes = new byte[32];
                final SecureRandom random = SecureRandom.getInstance(Constants.RANDOM_ALG);
                random.nextBytes(randomBytes);

                sessionToken = "0x" + DatatypeConverter.printHexBinary(randomBytes);
                userContext.setSessionToken(sessionToken);
                userContext.setDt(LocalDateTime.now());  // <- обновляем дату последнего обращения

                appState.getUserContext().remove(token);  // <- удаляем токен авторизации
                appState.getUserContext().put(sessionToken, userContext);  // сохраняем сессионый токен

                //20190416
                appState.getSessions().remove(token);
                appState.getSessions().put(sessionToken, sessionInfo);  // <- сохраняем сессионый токен

                //this.sessionInfo.setCreateTime(sessionInfo.getCreateTime());
                //this.sessionInfo.setAccountAddress(sessionInfo.getAccountAddress());
                //this.sessionInfo.setDepositoryOgrn(sessionInfo.getDepositoryOgrn());
                //this.sessionInfo.setManagerName(sessionInfo.getManagerName());
                //this.sessionInfo.setUserRole(sessionInfo.getUserRole());
                //this.sessionInfo.setSessionToken(sessionInfo.getSessionToken());

                this.sessionInfo.setAccountAddress(userContext.getUserId());
                this.sessionInfo.setSessionToken(sessionToken);
            }
        }
        catch(Exception ex) {
            //todo: временное решение ТОЛЬКО(!) для тестирования
            //this.sessionInfo.setSessionToken(sessionToken);
        }
    }

    public boolean isAuthorized() {
        // TODO: Проверить время создания сессии и обновить его на текущее время
        return sessionInfo.getSessionToken() != null;
    }

    private boolean checkRole(UserRole[] allowedRoles) {

        //todo: добавиьт проверку роли, привязанной к пользователю
        //return allowedRoles.length == 0 || Arrays.stream(allowedRoles).anyMatch(role -> UserRole.getUserRoleFromFrontString(sessionInfo.getUserRole()) == role);

        return true;
    }
}
