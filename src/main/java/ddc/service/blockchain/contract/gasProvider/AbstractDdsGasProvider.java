package ddc.service.blockchain.contract.gasProvider;

import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

public abstract class AbstractDdsGasProvider implements ContractGasProvider {

    public static final long GAS_LIMIT = 20_712_388L;
    public static final long GAS_PRICE = 19_000_000_000L;

    @Override
    public BigInteger getGasPrice() {
        return BigInteger.valueOf(GAS_PRICE);
    }

    @Override
    public BigInteger getGasLimit() {
        return BigInteger.valueOf(GAS_PRICE);
    }

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return BigInteger.valueOf(GAS_PRICE);
    }
}
