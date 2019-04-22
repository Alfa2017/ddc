package ddc.service.blockchain.contract.gasProvider;

import ddc.sc2.RoleModel;

import java.math.BigInteger;

/**
 * Провайдер параметров Газа для вызовов отдельных методов СК RoleModel
 */
public class RoleModelGasProvider extends AbstractDdsGasProvider {

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        switch (contractFunc) {
            case RoleModel.FUNC_ASSIGNWRITER:
                return BigInteger.valueOf(200_000L);
            default:
                return getGasLimit();
        }
    }

}
