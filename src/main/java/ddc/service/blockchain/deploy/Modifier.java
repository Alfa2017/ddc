package ddc.service.blockchain.deploy;

public interface Modifier {

    String PREFIX = "AFTDDS";

    byte[] getBytes(String prefix);

    default byte[] getBytes() {
        return getBytes(PREFIX);
    }

    String name();
}
