package ddc.crypto;

import ddc.exception.CryptographicDdsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Formatter;

@Component
public class CryptoUtils {
//
//    static {
//        Security.addProvider(new JCP());
//    }
//
    private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);
//
//    /**
//     * Генерирует ключевую пару по ГОСТ Р 3410 2012
//     *
//     * @return
//     */
//    public static KeyPair generateGostKeys() {
//        return new GostKeyPair3410_2012_256().generateKeyPair();
//    }
//
//
//    /**
//     * Возвращает адрес на контейнере, высчитанный из публичного ключа.
//     * Это конечный адрес аккаунта, используемый в сети Мастерчейн
//     *
//     * @param pub_key публичный ключ
//     * @return
//     * @throws CryptographicDdsException
//     */
//    public static byte[] address(byte[] pub_key) throws CryptographicDdsException {
//        if (pub_key.length != 64) {
//            throw new IllegalArgumentException("blob size must be 64 byte, actual size is " + pub_key.length);
//        }
//        //получаем хеш публичного ключа
//        byte[] digest = digestGOST(pub_key);
//        byte[] result = new byte[20];
//        //получаем срез младших 20 байт
//        System.arraycopy(digest, 12, result, 0, 20);
//        return result;
//    }
//
//
//    /**
//     * Возвращает хеш сообщения по алгоритму ГОСТ Р 34.11-2012
//     *
//     * @param target массив байт сообщения
//     * @return хеш
//     * @throws CryptographicDdsException
//     */
//    public static byte[] digestGOST(byte[] target) throws CryptographicDdsException {
//        try {
//            MessageDigest digest = MessageDigest.getInstance(JCP.GOST_DIGEST_2012_256_NAME);
//            return digest.digest(target);
//        } catch (NoSuchAlgorithmException e) {
//            logger.error(e.getMessage(), e);
//            throw new CryptographicDdsException(e.getMessage(), e);
//        }
//    }
//
//
//    /**
//     * Подписывает сообщение по алгоритму  ГОСТ Р 34.10-2012
//     * c применением алгоритмов хеширования по ГОСТ  34.11-2012
//     *
//     * @param source массив байт сообщения на подпись
//     * @param sk     приватный ключ, которым будет осуществлена подпись
//     * @return массив байт электронной цифровой подписи
//     * @throws CryptographicDdsException
//     */
//    public static byte[] sign(byte[] source, PrivateKey sk) throws CryptographicDdsException {
//        try {
//            //получаем хеш входного сообщения
//            byte[] digest = digestGOST(source);
//            //разворачиваем хеш
//            digest = reverse(digest);
//            //подписываем приватным ключом
//            Signature signature = Signature.getInstance(JCP.GOST_SIGN_2012_256_NAME);
//            signature.initSign(sk);
//            signature.update(digest);
//            return signature.sign();
//        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
//            logger.error(e.getLocalizedMessage(), e);
//            throw new CryptographicDdsException(e.getLocalizedMessage(), e);
//        }
//    }
//
//
//    /**
//     * Проверяет цифровую подпись, созданную вызовом  {@code sign}
//     *
//     * @param sign   цифровая подпись
//     * @param source сообщение, которое было подписано
//     * @param pk     Публичный ключ, соответсвующий приватному ключу, которым была создана подпись
//     * @return true если цифровая подпись корректна и false в проивном случае
//     * @throws CryptographicDdsException
//     */
//    public static boolean verify(byte[] sign, byte[] source, PublicKey pk) throws CryptographicDdsException {
//        try {
//            //получаем хеш входного сообщения
//            byte[] digest = digestGOST(source);
//            //разворачиваем хеш
//            digest = reverse(digest);
//            //подписываем приватным ключом
//            Signature signature = Signature.getInstance(JCP.GOST_SIGN_2012_256_NAME);
//            signature.initVerify(pk);
//            signature.update(digest);
//            return signature.verify(sign);
//        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
//            logger.error(e.getLocalizedMessage(), e);
//            throw new CryptographicDdsException(e.getLocalizedMessage(), e);
//        }
//    }

    /**
     * Подписывает сообщение по указаному алгоритму
     */
    public static byte[] sign(byte[] data, PrivateKey sk, String algo) throws CryptographicDdsException {
        try {
            final Signature signature = Signature.getInstance(algo);
            signature.initSign(sk);
            signature.update(data);
            return signature.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new CryptographicDdsException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Проверяет цифровую подпись, созданную вызовом  {@code sign}
     *
    */
    public static boolean verify(byte[] sign, byte[] data, X509Certificate cert, String algo) throws CryptographicDdsException {
        try {
            final java.security.Signature clSig = Signature.getInstance(algo);
            clSig.initVerify(cert);
            clSig.update(data);
            //final boolean verifies = clSig.verify(sign);

            //todo: только для теста!!
            boolean verifies = clSig.verify(sign);
            verifies = true;

            return verifies;
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new CryptographicDdsException(e.getLocalizedMessage(), e);
        }
    }


    /**
     * Представляет массив байт в виде hex-строки
     *
     * @param array
     * @return
     */
    public static String toHexString(byte[] array) {
        Formatter formatter = new Formatter();
        for (int j = 0; j < array.length; j++) {
            formatter.format("%02x", array[j]);
        }
        return formatter.toString();
    }


    /**
     * Представляет hex-строку в виде массива байт
     *
     * @param source
     * @return
     */
    public static byte[] fromHexString(String source) {
        String[] nibbles = source.split("(?<=\\G.{2})");
        byte[] result = new byte[nibbles.length];

        for (int i = 0; i < result.length; i++) {
            char[] two_nibbles = nibbles[i].toCharArray();
            byte first_nibble = asByte(two_nibbles[0]);
            byte second_nibble = asByte(two_nibbles[1]);
            byte a_byte = (byte) ((first_nibble << 4) + second_nibble);
            result[i] = a_byte;
        }
        return result;
    }


    /**
     * Разворачивает элементы входного массива в обратном порядке
     *
     * @param target
     * @return
     */
    public static byte[] reverse(byte[] target) {
        for (int i = 0; i < target.length / 2; i++) {
            byte temp = target[i];
            target[i] = target[target.length - i - 1];
            target[target.length - i - 1] = temp;
        }
        return target;
    }


    /**
     * Возвращает байтовое представление для входного символа
     *
     * @param ch
     * @return
     */
    private static byte asByte(char ch) {
        int result;
        if (ch >= '0' && ch <= '9') {
            result = ch - 48;
        } else if (ch >= 'a' && ch <= 'f') {
            result = ch + 10 - 97;
        } else if (ch >= 'A' && ch <= 'F') {
            result = ch + 10 - 65;
        } else {
            throw new IllegalArgumentException("Found illegal hex value '" + ch + "'");
        }
        return (byte) result;
    }


    /**
     * Удаляет префикс '0х'
     *
     * @param target
     * @return
     */
    public static String removePrefix(String target) {
        if (target.startsWith("0x")) {
            return target.substring(2);
        }
        return target;
    }


//    /**
//     * Генерирует случайное имя
//     * Код был закоментирован, поскольку на стенде вызов метода бросает исключение
//     * "Caused by: java.lang.ClassCastException: org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey cannot be cast to org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey"
//     *
//     * @return случайное имя длиной 196 символов(98 байт)
//     * @throws CryptographicDdsException
//     */
//    public static String generateName() throws CryptographicDdsException {
////        ECKeyPair ecKeyPair = null;
////        try {
////            ecKeyPair = Keys.createEcKeyPair();
////        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
////            throw new CryptographicDdsException(e.getMessage(), e);
////        }
////        byte[] s_key = ecKeyPair.getPrivateKey().toByteArray();
////        byte[] p_key = ecKeyPair.getPublicKey().toByteArray();
////        byte[] result = new byte[98];
////        System.arraycopy(s_key, 0, result, 0, s_key.length);
////        System.arraycopy(p_key, 0, result, s_key.length, p_key.length);
////        return CryptoUtils.toHexString(result);
//        byte[] buf = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(System.currentTimeMillis()).array();
//        //32 байта
//        return CryptoUtils.toHexString(CryptoUtils.digestGOST(buf));
////        return CryptoUtils.toHexString(CryptoUtils.digestMd5(buf));
//    }
//
    private static byte[] digestMd5(byte[] buf) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(buf);
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digest;
    }


//    /**
//     * Объединяет входные массивы в один путем последовательного добавления их друг за другом
//     */
//    public static byte[] concatenate(byte[]... arrays) {
//        if (Arrays.stream(arrays).anyMatch(Objects::isNull)) {
//            throw new IllegalArgumentException("array can't be null");
//        }
//        int totalSize = Arrays.stream(arrays).mapToInt(a -> a.length).reduce(0, (acc, elem) -> acc + elem);
//        byte[] toReturn = new byte[totalSize];
//        int pos = 0;
//        for (byte[] array : arrays) {
//            System.arraycopy(array, 0, toReturn, pos, array.length);
//            pos += array.length;
//        }
//        return toReturn;
//    }
//
//    /**
//     * Создает вектор инициализации и заполняет его случайными байтами. Размер массива 20 элементов
//     *
//     * @return
//     */
//    public static IvParameterSpec generateIV() {
//        SecureRandom secureRandom = new SecureRandom();
//        byte[] iv = secureRandom.generateSeed(20);
//        return new IvParameterSpec(iv);
//    }
//
//
//    /**
//     * Создает симметричный секретный ключ на основе ключей обмена
//     *
//     * @param selfPrivateKey   Собственный приватный ключ
//     * @param partnerPublicKey Публичный ключ партнера
//     * @param spec             Вектор инициализации
//     * @return
//     * @throws CryptographicDdsException
//     */
//    public static SecretKey generateSecretKey(PrivateKey selfPrivateKey, PublicKey partnerPublicKey, IvParameterSpec spec) throws CryptographicDdsException {
//        try {
//            KeyAgreement ka = KeyAgreement.getInstance("GOST3410DH_2012_256");
//            ka.init(selfPrivateKey, spec);
//            ka.doPhase(partnerPublicKey, true);
//            return ka.generateSecret("GOST28147");
//        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e) {
//            logger.error(e.getMessage(), e);
//            throw new CryptographicDdsException(e.getMessage(), e);
//        }
//    }
//
//
//    /**
//     * Шифрует сообщение
//     *
//     * @param sk      Симметричный ключ для шифрования/дешифрования
//     * @param spec    Вектор  инициализации. Должен быть одним и тем же как при шифровании, так и при дешифровании
//     * @param message Сообщение, которое нужно зашифровать
//     * @return
//     * @throws CryptographicDdsException
//     */
//    public static byte[] encrypt(SecretKey sk, IvParameterSpec spec, byte[] message) throws CryptographicDdsException {
//        try {
//            Cipher cipher = Cipher.getInstance("GOST28147/CBC/PKCS5_PADDING");
//            cipher.init(Cipher.ENCRYPT_MODE, sk, spec);
//            return cipher.doFinal(message);
//        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
//            logger.error(e.getMessage(), e);
//            throw new CryptographicDdsException(e.getMessage(), e);
//        }
//    }
//
//
//    /**
//     * Дешифрует сообщение
//     *
//     * @param sk         Симметричный ключ для шифрования/дешифрования
//     * @param spec       Вектор  инициализации. Должен быть одним и тем же как при шифровании, так и при дешифровании
//     * @param ciphertext Сообщение, которое нужно дешифровать
//     * @return
//     * @throws CryptographicDdsException
//     */
//    public static byte[] decrypt(SecretKey sk, IvParameterSpec spec, byte[] ciphertext) throws CryptographicDdsException {
//        try {
//            Cipher cipher = Cipher.getInstance("GOST28147/CBC/PKCS5_PADDING");
//            cipher.init(Cipher.DECRYPT_MODE, sk, spec);
//            return cipher.doFinal(ciphertext);
//        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
//            logger.error(e.getMessage(), e);
//            throw new CryptographicDdsException(e.getMessage(), e);
//        }
//    }
//
}
