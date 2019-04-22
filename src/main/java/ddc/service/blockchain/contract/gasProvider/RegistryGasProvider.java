package ddc.service.blockchain.contract.gasProvider;

import ddc.sc2.Registry;

import java.math.BigInteger;

/**
 * Провайдер параметров Газа для вызовов отдельных методов СК Registry
 */
public class RegistryGasProvider extends AbstractDdsGasProvider {

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        switch (contractFunc) {
            case Registry.FUNC_CREATENODE:
                return getGasLimit();
            case Registry.FUNC_CREATEUSER:
                return getGasLimit();
            case Registry.FUNC_GRANTUSERROLE:
                return getGasLimit();
            case Registry.FUNC_GETORGUSERIDS:
                return getGasLimit();
            case Registry.FUNC_GETUSERORGIDANDROLEIDS:
                return getGasLimit();
            case Registry.FUNC_USERISENABLED:
                return getGasLimit();
            case Registry.FUNC_GETNODEIDS:
                return getGasLimit();
            default:
                return getGasLimit();
        }
    }

}
