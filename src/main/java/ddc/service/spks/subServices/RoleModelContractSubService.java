package ddc.service.spks.subServices;

import ddc.sc2.RoleModel;
import ddc.service.blockchain.BlockchainService;
import ddc.service.blockchain.contract.RoleModelContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ddc.AppState;

@Slf4j
public class RoleModelContractSubService {
    private final BlockchainService blockchainService;
    private final AppState appState;
    private RoleModelContract roleModelContract;


    @Autowired
    public RoleModelContractSubService(BlockchainService blockchainService, AppState appState) {
        this.blockchainService = blockchainService;
        this.appState = appState;
        this.roleModelContract = new RoleModelContract(blockchainService, appState.getRegistryAddress());
    }

    /**
     *
     * @return RoleModel SC (read only!!)
     */
    public RoleModel getRoleModelSC(){
        return roleModelContract.loadRoleModel();
    }
}
