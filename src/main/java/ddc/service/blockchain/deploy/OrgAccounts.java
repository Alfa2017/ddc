package ddc.service.blockchain.deploy;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrgAccounts {

    private String admin;
    private String robot;
    private String manager;

}
