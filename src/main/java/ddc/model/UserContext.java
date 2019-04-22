package ddc.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class UserContext {
    private String userId;  // <- идентификатор ключа сотрудника
    private String ip;  // <- IP-адрес, с которго поступил запрос
    private String rnd;  // <- случайное 256-битное значение
    private String srvPubKey;  // открытый ключ сервера
    private String clientPubKey; // сертификат открытого ключа пользователя
    private LocalDateTime dt;  // <- срок годности C

    private String authToken;  //  токен аутентификации
    private String sessionToken;  // сессионный токен

    public String getToken() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss"); //"yyyy-MM-dd HH:mm"
        String token = userId + "," + ip + "," + rnd + "," + dt.format(formatter);
        //byte[] b = string.getBytes();
        //byte[] b = string.getBytes(Charset.forName("UTF-8"));
        //return payload.getBytes();
        return token;
    }
}

