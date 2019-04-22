package ddc.service.blockchain;

public class BlockchainDdsException extends RuntimeException {

    public BlockchainDdsException(String message) {
        super(message);
    }

    public BlockchainDdsException(Throwable cause) {
        super(cause);
    }
}
