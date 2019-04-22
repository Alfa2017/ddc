package ddc.service.blockchain.deploy;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrgInfo {
    private String ogrn;
    private String inn;
    private String kpp;
    private String code;
    private String shortName;
    private String fullName;
    private OrgAccounts ethAccounts;
}