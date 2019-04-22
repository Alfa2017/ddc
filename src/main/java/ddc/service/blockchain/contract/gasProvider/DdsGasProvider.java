package ddc.service.blockchain.contract.gasProvider;

import ddc.sc2.DDSystem;

import java.math.BigInteger;

/**
 * Провайдер параметров Газа для вызовов отдельных методов СК ДДС
 */
public class DdsGasProvider extends AbstractDdsGasProvider {

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        switch (contractFunc) {
            //Deploy
            case DDSystem.FUNC_ADDACCOUNT:
                return BigInteger.valueOf((long)Math.min(1_899_034L*1.5, GAS_LIMIT));
            case DDSystem.FUNC_ADDMORTGAGE:
                return BigInteger.valueOf((long)Math.min(3_520_510L*1.5, GAS_LIMIT));
            case DDSystem.FUNC_EDITORGANIZATION:
                //добавление организации ADD_ORGANIZATION
                return BigInteger.valueOf((long)Math.min(500_000*1.5, GAS_LIMIT));
            default:
                return getGasLimit();
        }
    }

}
