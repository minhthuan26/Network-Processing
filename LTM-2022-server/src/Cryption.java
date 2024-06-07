import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class Cryption {

    public static String RandomKey() {
        int keyLength = (int) (11 * Math.random() + 10);
        String alphabet = "abcdefghijklmnopqrstuvxyz";

        StringBuilder key = new StringBuilder(keyLength);
        for (int i = 0; i < keyLength; i++) {
            int index = (int) (alphabet.length() * Math.random());

            // add Character one by one in end of sb
            key.append(alphabet.charAt(index));
        }
        return key.toString();
    }

    public static class RSA {
        public static ArrayList<byte[]> KeyPairGenerate() {
            SecureRandom sr = new SecureRandom();
            //Thuật toán phát sinh khóa - Rivest Shamir Adleman (RSA)
            KeyPairGenerator kpg = null;
            try {
                kpg = KeyPairGenerator.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            kpg.initialize(2048, sr);
            //Phát sinh cặp khóa
            KeyPair kp = kpg.genKeyPair();
            //PublicKey
            PublicKey pubKey = kp.getPublic();
            //PrivateKey
            PrivateKey priKey = kp.getPrivate();
            ArrayList<byte[]> keyList = new ArrayList<>();
            keyList.add(pubKey.getEncoded());
            keyList.add(priKey.getEncoded());
            return keyList;
        }

        public static String Encryption(String message, byte[] key) {
            try {
                // Tạo public key
                X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
                KeyFactory factory = KeyFactory.getInstance("RSA");
                PublicKey pubKey = factory.generatePublic(spec);
                // Mã hoá dữ liệu
                Cipher c = Cipher.getInstance("RSA");
                c.init(Cipher.ENCRYPT_MODE, pubKey);
                byte[] encryptOut = c.doFinal(message.getBytes());
                return Base64.getEncoder().encodeToString(encryptOut);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        public static String Decryption(String message, byte[] key) {
            try {
                // Tạo private key
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
                KeyFactory factory = KeyFactory.getInstance("RSA");
                PrivateKey priKey = factory.generatePrivate(spec);
                // Giải mã dữ liệu
                Cipher c = Cipher.getInstance("RSA");
                c.init(Cipher.DECRYPT_MODE, priKey);
                byte[] decryptOut = c.doFinal(Base64.getDecoder().decode(message));
                return new String(decryptOut);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    public static class AES {
        public static String Encryption(String strToEncrypt, String myKey) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-1");
                byte[] key = myKey.getBytes("UTF-8");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16);
                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
            } catch (Exception error) {
                error.printStackTrace();
            }
            return null;
        }

        public static String Decryption(String strToDecrypt, String myKey) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-1");
                byte[] key = myKey.getBytes("UTF-8");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16);
                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
            } catch (Exception error) {
                error.printStackTrace();
            }
            return null;
        }

        public static Serializable EncryptionObject(Serializable object, String myKey) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-1");
                byte[] key = myKey.getBytes("UTF-8");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16);
                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return new SealedObject(object, cipher);
            } catch (Exception error) {
                error.printStackTrace();
            }
            return null;
        }

        public static Serializable DecryptionObject(SealedObject object, String myKey) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-1");
                byte[] key = myKey.getBytes("UTF-8");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16);
                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return (Serializable) object.getObject(cipher);
            } catch (Exception error) {
                error.printStackTrace();
            }
            return null;
        }
    }

    public static void main(String[] args) {
        String path = System.getProperty("user.dir");
        File test = new File("D:\\HK1-nam4\\lap_trinh_mang\\project\\LTM-2022-client\\src\\main\\resources\\com\\ltm2022client\\application\\images\\RED.png");
        SealedObject object = (SealedObject) AES.EncryptionObject(test, "testkey");
        File rs = (File) AES.DecryptionObject(object, "testkey");
        System.out.println(rs);
    }
}

