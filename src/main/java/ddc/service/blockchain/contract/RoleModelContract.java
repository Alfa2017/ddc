package ddc.service.blockchain.contract;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import ddc.sc2.RoleModel;
import ddc.service.blockchain.BlockchainService;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.contract.gasProvider.AbstractDdsGasProvider;
import ddc.service.blockchain.contract.gasProvider.RoleModelGasProvider;
import ddc.service.blockchain.deploy.Modifier;
import ddc.service.blockchain.deploy.OrgRole;
import ddc.model.enums.UserRole;

import java.util.concurrent.CompletableFuture;

public class RoleModelContract {
    private final BlockchainService blockchainService;
    private final AbstractDdsGasProvider gasProvider = new RoleModelGasProvider();
    private String roleModelAddress;

    public RoleModelContract(BlockchainService blockchainService, String roleModelAddress) {
        this.blockchainService = blockchainService;
        this.roleModelAddress = roleModelAddress;
    }

    public CompletableFuture<RoleModel> deploy(String sender) {

        return blockchainService.execute(
                RoleModel.deploy(blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider, "0x0")::send,
                "Деплой ролевой модели"
        ).thenApply(tr -> {
            this.roleModelAddress = tr.getContractAddress();
            return tr;
        });
    }

    public CompletableFuture<TransactionReceipt> assignWriter(Modifier action, OrgRole orgRole, UserRole userRole, String sender) {
        RoleModel roleModel = loadRoleModel(sender);

        return blockchainService.execute(
                roleModel.assignWriter(
                        action.getBytes(),
                        orgRole.getBytes(),
                        userRole.getBytes()
                )::send,
                String.format("Разрешение записи организации \"%s\" роли \"%s\" для действия \"%s\"", orgRole.name(), userRole.name(), action.name())
        );
    }

    public RoleModel loadRoleModel() {
        return RoleModel.load(roleModelAddress, blockchainService.getWeb3j(), blockchainService.getTM(Web3jUtils.ZERO_ADDRESS), gasProvider);
    }

    private RoleModel loadRoleModel(String sender) {
        return RoleModel.load(roleModelAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
    }
}
