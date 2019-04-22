package ddc.service.blockchain.contract.gasProvider;

import ddc.sc2.Mortgage;

import java.math.BigInteger;

/**
 * Провайдер параметров Газа для вызовов отдельных методов СК Закладной
 */
public class MortgageGasProvider extends AbstractDdsGasProvider {

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        switch (contractFunc) {
            case Mortgage.FUNC_CHANGEACCOUNT:
                return BigInteger.valueOf((long) Math.min(1_500_000, GAS_LIMIT));
            case Mortgage.FUNC_CHANGEACCOUNTSECTION:
                return BigInteger.valueOf((long) Math.min(/*363_085*1.5*/1_000_000, GAS_LIMIT));
            case Mortgage.FUNC_GETSNAPSHOT:
                return getGasLimit();
            case Mortgage.FUNC_CHANGESTATUS:
                return BigInteger.valueOf((long) Math.min(64_613 * 1.5, GAS_LIMIT));
            case Mortgage.FUNC_ADDAGREEMENT:
                return BigInteger.valueOf((long) Math.min(/*513_817 * 1.5*/1_000_000, GAS_LIMIT));
            case Mortgage.FUNC_EXTENDEDINFO:
                return getGasLimit();
            case Mortgage.FUNC_CONTAINER:
                return getGasLimit();
            default:
                return getGasLimit();
        }
    }
}
