package ddc.util;

import ddc.exception.CryptographicDdsException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {

    public CryptoUtils() {
    }

    public static byte[] digestSha256(byte[] content) throws CryptographicDdsException {
        try {
            MessageDigest digest = MessageDigest.getInstance("sha-256");
            return digest.digest(content);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptographicDdsException(e.getMessage(), e);
        }
    }
}
