package ddc.controller;

import ddc.model.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ddc.AppState;
import ddc.crypto.CryptoUtils;
import ddc.exception.ExceptionMessages;
import ddc.exception.ServerErrorDdsException;
import ddc.model.UserContext;
import ddc.model.request.AuthRequest;
import ddc.model.request.LoginRequest;
import ddc.model.request.SigninRequest;
import ddc.security.Authorized;
import ddc.security.SessionInfo;
import ddc.security.Unauthorized;
import ddc.service.domain.manager.ManagerService;
import ddc.service.domain.manager.ManagerDbService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.UUID;

import ru.CryptoPro.JCP.JCP;
import userSamples.Constants;

/**
 * @author pc 04.10.18
 */
@Slf4j
@Authorized
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private ManagerService managerService;

    @Autowired
    private ManagerDbService managerDbService;

    @Autowired
    private AppState appState;

    @Autowired
    private SessionInfo sessionInfo;

    @PostMapping("/login")
    @Unauthorized
    public ResponseEntity<Response> login(@RequestBody LoginRequest loginRequest, SessionInfo sessionInfo, HttpServletResponse response)
    throws ServerErrorDdsException {
        try {
            if (managerService.authenticate(loginRequest.getUsername(), loginRequest.getPassword())) {

                SessionInfoResponse loginInfo = managerDbService.getLoginInfo(loginRequest.getUsername());
                sessionInfo.setAccountAddress(loginRequest.getUsername());
                sessionInfo.setDepositoryOgrn(loginInfo.getDepositoryOgrn());
                sessionInfo.setManagerName(loginInfo.getEmployeeName());
                sessionInfo.setUserRole(loginInfo.getUserRole());

                String token = UUID.randomUUID().toString();
                response.addCookie(new Cookie("SESSION_TOKEN", token));
                sessionInfo.setSessionToken(token);
                sessionInfo.setCreateTime(LocalDateTime.now());

                appState.getSessions().put(token, sessionInfo);

                log.debug("Вход пользователя  {} с ролью {}. Пользователей в приложении: {}", loginInfo.getEmployeeName(), loginInfo.getUserRole(), appState.getSessions().size());
                return new ResponseEntity<>(new SimpleResponse(token), HttpStatus.OK);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ServerErrorDdsException(ExceptionMessages.SERVER_INNER_ERROR.getMessage(e.getMessage()));
        }
        return new ResponseEntity<>(new ApiErrorResponse("Неверный адрес или пароль"), HttpStatus.FORBIDDEN);
    }

    @PostMapping("/logout")
    @Unauthorized
    public SimpleResponse logout() {
        appState.getSessions().remove(sessionInfo.getSessionToken());
        log.debug("Выход пользователя: {}. Пользователей в приложении: {}", sessionInfo.getSessionToken(), appState.getSessions().size());
        return new SimpleResponse(Boolean.TRUE);
    }

    // Запрос токена авторизациидля для raw-подписи
    @PostMapping("/auth")
    @Unauthorized
    public ResponseEntity<Response> auth(@RequestBody AuthRequest authRequest, SessionInfo sessionInfo, HttpServletRequest request) throws ServerErrorDdsException {
        log.debug("Запрос токена авторизации для raw-подписи");

        AuthResponse responce = new AuthResponse();
        UserContext userContext = null;
        try {
            userContext = new UserContext();
            String client_ip= request.getRemoteAddr();   //получаем ip адрес
            userContext.setIp(client_ip);

            String userId = authRequest.getUser();
            userContext.setUserId(userId);

            // проверяем, что в реестре активен идентификатор ключа сотрудника
            if( managerService.getUserStatus(userId).equals("ACTIVE") ) {
                String clientCert = managerService.getUserSigCertificate(userId);
                if( !clientCert.isEmpty() ) {  // проверять ли последние 20ть байт?

                    //генерируем случайное число
                    final byte[] randomBytes = new byte[32];
                    final SecureRandom random = SecureRandom.getInstance(Constants.RANDOM_ALG);
                    random.nextBytes(randomBytes);
                    userContext.setRnd(CryptoUtils.toHexString(randomBytes)); // <- сохраняем случайной число
                    userContext.setDt(LocalDateTime.now());  // <- сохраняем дату создания

                    String aliasSrvCert = appState.getCertAlias();
                    String aliasSrvCertPwd = appState.getCertPwd();

                    final char[] certPwd = aliasSrvCertPwd.toCharArray();

                    KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
                    keyStore.load(null, null);

                    String tokenText = "0x" + userContext.getToken();  // <- получаем токен авторизации

                    // формируем хэш токена авторизации
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] tokenHash = digest.digest(CryptoUtils.removePrefix(tokenText).getBytes(Charset.forName("UTF-8")));

                    // формируем подпись токена авторизации
                    PrivateKey privateKey = (PrivateKey) keyStore.getKey(aliasSrvCert, certPwd);
                    final byte[] signature = CryptoUtils.sign(tokenHash, privateKey, JCP.RAW_GOST_SIGN_2012_256_NAME);

                    // получем сертификат открытого ключа для отправки клиенту
                    if( authRequest.getSercer().equalsIgnoreCase("NEED") ) {
                        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(aliasSrvCert);
                        responce.setSercer("0x" + CryptoUtils.toHexString(certificate.getEncoded()));
                    }

                    // формируем ответ
                    responce.setPayload("0x" + CryptoUtils.toHexString(tokenHash));
                    responce.setSersig("0x" + CryptoUtils.toHexString(signature));

                    userContext.setClientPubKey(clientCert); // <- сохраняем серификат открытого ключа пользователя

                    String uctx = "0x" + CryptoUtils.toHexString(tokenHash);
                    appState.getUserContext().put(uctx, userContext);  // сохраняем пользовательский контекст

                    return new ResponseEntity<>(responce, HttpStatus.OK);
                }
            }
        }
        catch( Exception e ) {
            log.error(e.getMessage(), e);
            throw new ServerErrorDdsException(ExceptionMessages.SERVER_INNER_ERROR.getMessage(e.getMessage()));
        }
        return new ResponseEntity<>(new ApiErrorResponse("Неверный адрес"), HttpStatus.FORBIDDEN);
    }

    // Получение сессионного токена по токену аутентификации
    @PostMapping("/signin")
    @Unauthorized
    public ResponseEntity<Response> sigin(@RequestBody SigninRequest signinRequest, SessionInfo sessionInfo, HttpServletRequest request) throws ServerErrorDdsException {
        log.debug("генерация сессионного токена по токену аутентификации");

        String payload = null;
        String sessionToken = null;
        String userSignature = null;
        SigninResponse responce = new SigninResponse();

        HttpServletResponse httpResponse = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        try {
            //payload = request.getHeader("payload");
            payload = signinRequest.getPayload();
            userSignature = signinRequest.getClisig();

            UserContext userContext = appState.getUserContext().get(payload);
            if( userContext != null ) {
                String userMcCert = managerService.getUserSigCertificate(userContext.getUserId());
                if( !userMcCert.isEmpty() ) {

                    //проверяем клиентскую подпись
                    byte[] c2 = CryptoUtils.fromHexString(CryptoUtils.removePrefix(userMcCert));
                    byte[] t2 = CryptoUtils.fromHexString(CryptoUtils.removePrefix(payload));
                    byte[] s2 = CryptoUtils.fromHexString(CryptoUtils.removePrefix(userSignature));
                    //Utils.reverse(s2);  // <- важно: сама cryptopro переворачивает подпись!!
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate)(certificateFactory.generateCertificate(new ByteArrayInputStream(c2)));
                    //final PublicKey clientPublicKey = cert.getPublicKey();

                    // проверяем подпись
                    final boolean verifies = CryptoUtils.verify(s2, t2, cert, JCP.RAW_GOST_SIGN_2012_256_NAME);
                    if( verifies ) {
                        //генерируем сессионный токен
                        final byte[] randomBytes = new byte[32];
                        final SecureRandom random = SecureRandom.getInstance(Constants.RANDOM_ALG);
                        random.nextBytes(randomBytes);
                        sessionToken = "0x" + CryptoUtils.toHexString(randomBytes);
                        userContext.setSessionToken(sessionToken);

                        responce.setToken(sessionToken);
                        //responce.setSercer(serverCert);

                        appState.getUserContext().remove(payload);  // <- удаляем токен авторизации
                        appState.getUserContext().put(sessionToken, userContext);  // <- сохраняем сессионый токен

                        //20190416
                        SessionInfoResponse loginInfo = managerDbService.getLoginInfo(userContext.getUserId());
                        sessionInfo.setAccountAddress(userContext.getUserId());
                        sessionInfo.setSessionToken(sessionToken);
                        sessionInfo.setUserRole(loginInfo.getUserRole());
                        sessionInfo.setCreateTime(LocalDateTime.now());
                        appState.getSessions().put(sessionToken, sessionInfo);  // <- сохраняем сессионый токен

                        httpResponse.setHeader("token", sessionToken); // <- отправляем токен клиенту

                        return new ResponseEntity<>(responce, HttpStatus.OK);
                    }
                }
            }
        }
        catch( Exception e ) {
            log.error(e.getMessage(), e);
            throw new ServerErrorDdsException(ExceptionMessages.SERVER_INNER_ERROR.getMessage(e.getMessage()));
        }
        return new ResponseEntity<>(new ApiErrorResponse("Неверный токен"), HttpStatus.FORBIDDEN);
    }

    // Инвалидация сессионного токена
    @PostMapping("/signout")
    //@Authorized
    @Unauthorized
    public SimpleResponse sigout(HttpServletResponse response) throws ServerErrorDdsException {
        log.debug("Инвалидация сессионного токена: {}. Пользователей в приложении: {}", sessionInfo.getSessionToken(), appState.getSessions().size());

        Boolean rs = Boolean.FALSE;
        String token = null;
        //String userSignature = null;
        try {
            token = response.getHeader("token");
            UserContext userContext = appState.getUserContext().get(token);
            if( userContext != null ) {
                appState.getUserContext().remove(token);
                appState.getSessions().remove(token);
                log.debug("Выход пользователя: {}. Пользователей в приложении: {}", sessionInfo.getSessionToken(), appState.getSessions().size());
                rs = Boolean.TRUE;
            }

            appState.cleanup();
        }
        catch( Exception e ) {
            log.error(e.getMessage(), e);
            throw new ServerErrorDdsException(ExceptionMessages.SERVER_INNER_ERROR.getMessage(e.getMessage()));
        }
        return new SimpleResponse(rs);
    }

    /**
     * Проверка авторизации пользователя при перемещении по страницам
     * При потери сессии Authorized редиректит на главную страницу
     */
    @Authorized
    @GetMapping("/checkAuth") // TODO: refs #106962
    public String checkAuth(){
        return "Authorized";
    }
}
