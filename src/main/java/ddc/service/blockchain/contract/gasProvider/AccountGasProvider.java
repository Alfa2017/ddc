package ddc.service.blockchain.contract.gasProvider;

import ddc.sc2.Account;

import java.math.BigInteger;

/**
 * Провайдер параметров Газа для вызовов отдельных методов СК Счета
 */
public class AccountGasProvider extends AbstractDdsGasProvider {

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        switch (contractFunc) {
            case Account.FUNC_EDITSECTION:
                return getGasLimit();
            case Account.FUNC_UPDATEMETA:
                return getGasLimit();
            case Account.FUNC_GETACCOUNTSTATE:
                return getGasLimit();
            case Account.FUNC_META:
                return getGasLimit();
            case Account.FUNC_ORGID:
                return getGasLimit();
            case Account.FUNC_DEPONENT:
                return getGasLimit();
            case Account.FUNC_STATUS:
                return getGasLimit();
            case Account.FUNC_ACCTYPE:
                return getGasLimit();
            case Account.FUNC_NUMBER:
                return getGasLimit();
                //getAccountSectionEvents?
            default:
                return getGasLimit();
        }

    }
}
