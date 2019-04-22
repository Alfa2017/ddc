package ddc.model.request;

import lombok.Data;

@Data
public class SigninRequest {
    private String payload;    //  токен аутентификации
    private String sersig;
    private String clisig;
}
