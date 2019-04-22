package ddc.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@JsonPropertyOrder({"ddsId", "name", "code"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse implements Response {
    private String payload;  // токен аутентификации
    private String sersig;   // серверная подпись токена аутентификации.
    private String sercer;   // сертификат сервера с цепочкой, достаточной для валидации
    //private String Status;
}
