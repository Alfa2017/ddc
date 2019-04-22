package ddc.service.blockchain;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

@Slf4j
public class Web3jUtils {

    private Web3jUtils() {
    }


    public static final  SecureRandom secureRandom = new SecureRandom();

    public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";

    public static String createTopicFromAddress(String address) {
        return "0x" + TypeEncoder.encode(new Address(address));
    }

    public static String createTopicFromBytes32(byte[] bytes32) {
        return "0x" + Numeric.toHexStringNoPrefix(bytes32);
    }

    public static boolean isZeroAddress(String hexAdress) {
        hexAdress = removeHexPrefix(hexAdress);
        return new BigInteger(hexAdress, 16).longValue() == 0;
    }

    /**
     * Удаляет префикс '0х'
     *
     * @param target
     * @return
     */
    public static String removeHexPrefix(String target) {
        if (target.startsWith("0x")) {
            return target.substring(2);
        }
        return target;
    }

    /**
     * Создает случайный адрес длиной 20 байт
     * @return
     */
    public static String randomAddress() {
        byte[] rawAddress = getRandomBytes(20);
        return "0x" + new BigInteger(rawAddress).abs().toString(16);
    }

    public static byte[] getRandomBytes(Integer length) {
        byte[] rawAddress = new byte[length];
        secureRandom.nextBytes(rawAddress);
        return rawAddress;
    }

    public static byte[] strToBytes(String str, int length) {
        return Arrays.copyOf(str.getBytes(), length);
    }

    public static byte[] strToBytes32(String str) {
        return strToBytes(str, 32);
    }
    public static byte[] strToBytes16(String str) {
        return strToBytes(str, 16);
    }

    public static byte[] strToBytes8(String str) {
        return strToBytes(str, 8);
    }

    public static byte[] strToBytes4(String str) {
        return strToBytes(str, 4);
    }

    public static byte[] trimBytes(byte[] bytes) {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }

    public static String trimToString(byte[] bytes) {
        return new String(trimBytes(bytes));
    }

    public static byte[] expandToSize(byte[] bytes, int length) {
        return Arrays.copyOf(bytes, length);
    }

    public static byte[] expandToSize32(byte[] bytes) {
        return expandToSize(bytes, 32);
    }

    public static byte[] decodeHex(String hex) {
        try {
            return Hex.decodeHex(hex);
        } catch (DecoderException e) {
            log.error(e.getMessage());
        }
        return new byte[0];
    }

    public static String encodeHex(byte[] hex) {
        return new String(Hex.encodeHex(hex));
    }

    public static byte[] longToBytes(Long x) {
        return ByteBuffer.allocate(Long.BYTES).putLong(x).array();
    }

    public static Long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        return buffer.getLong();
    }

    public static byte[] concatByteArrays(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static boolean isAddress(String address) {
        return null != address && address.matches("0[x|X][0-9A-Fa-f]{40}");
    }

    public static boolean isNotZeroAddress(String address) {
        return isAddress(address) && !ZERO_ADDRESS.equals(address);
    }


    public static byte[] createOrgIdFromOgrn(String ogrn) {
        return Web3jUtils.strToBytes32(addOgrnPrefix(ogrn));
    }

    private static final String PREFIX = "OGRN:";

    public static String addOgrnPrefix(String ogrn) {
        if(ogrn != null && ogrn.startsWith(PREFIX)) {
            return ogrn;
        }
        return PREFIX + ogrn;
    }

    public static String removeOgrnPrefix(String ogrn) {
        if(ogrn != null && ogrn.startsWith(PREFIX)) {
            return ogrn.substring(PREFIX.length());
        }
        return ogrn;
    }

    public static String removeOgrnPrefix(byte[] ogrnBytes) {
        return removeOgrnPrefix(trimToString(ogrnBytes));
    }

}
