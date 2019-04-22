package ddc.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@JsonPropertyOrder({"ddsId", "name", "code"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SigninResponse implements Response {

    private String token;   // сессионный токен
    private String Status;

}
