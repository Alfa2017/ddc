package ddc.service.blockchain.contract.gasProvider;

import ddc.sc2.Container;

import java.math.BigInteger;

/**
 * Провайдер параметров Газа для вызовов отдельных методов СК Container
 */
public class ContainerGasProvider extends AbstractDdsGasProvider {

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        switch (contractFunc) {
            case Container.FUNC_APPENDARCHIVE:
                return getGasLimit();
            default:
                return getGasLimit();
        }
    }

}
