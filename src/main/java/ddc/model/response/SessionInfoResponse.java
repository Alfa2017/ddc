package ddc.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoResponse {

    private String depositoryOgrn;

    private Integer depositoryKind;

    private String depositoryName;

    private String employeeAddress;

    private String userRole;

    private String employeeName;

    private String date;

    private String apiVersion;

    private Boolean authorized = false;
}
