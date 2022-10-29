package JCA;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.*;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Scanner;
import java.io.FileInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;


public class Exercise6 {
    public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, KeyStoreException, InvalidAlgorithmParameterException, UnrecoverableKeyException {
        app();
    }

    public static String pass = "changeit";
    public static String rootPath = "src/main/files/";

    public static void app() throws KeyStoreException, IOException, InvalidAlgorithmParameterException, UnrecoverableKeyException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, CertificateException {
        String r = printAndReadOption();

        while (!Objects.equals(r, "exit")){
            String[] l = r.split(" ");

            if(l.length == 1 || l.length > 3 ) {
                System.out.println("Insert a valid command");
                r = printAndReadOption();
                continue;
            }

            switch (l[l.length-1]) {
                case "-dec" -> {
                    KeyStore ks =  KeyStore.getInstance("PKCS12");
                    ks.load(new FileInputStream(rootPath + l[0]), pass.toCharArray());
                    decipher("message.txt", "key.txt", ks, "iv.txt");
                    System.out.println("Message successfully decrypted");
                }
                case "-enc" -> {
                    encipher(l[0], rootPath + l[1]);
                    System.out.println("Message and Key successfully encrypted");
                }
                default -> System.out.println("Insert a valid command");
            }

            r = printAndReadOption();
        }
    }

    public static String printAndReadOption() {
        System.out.println("\n<message> <.cer> -enc\n<.pfx> -dec\nexit\n");
        return new Scanner(System.in).nextLine();
    }

    public static void encipher(String file, String cer) throws CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        //Certificate and Public Key
        FileInputStream in = new FileInputStream(cer);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);

        // Verifica a validade do período do certificado.
        certificate.checkValidity();

        PublicKey publicKey = certificate.getPublicKey();

        byte[] msg = Exercise5.messageFromPath(rootPath + file);

        SecretKey symKey = getSecretKey();

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        cipher.init( Cipher.WRAP_MODE, publicKey);

        byte[] encodedKey = cipher.wrap( symKey );

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, symKey);

        byte[] bytes = cipher.doFinal(msg);
        byte[] iv = cipher.getIV();

        createFile("key.txt", encodedKey, true);
        createFile("message.txt", bytes, true);
        createFile("iv.txt", iv, true);
    }

    public static SecretKey getSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom secRandom = new SecureRandom();
        keyGen.init(secRandom);

        return keyGen.generateKey();
    }

    public static void decipher(String textFile, String symKeyFile, KeyStore ks, String ivFile) throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        Base64InputStream message = getInputStreamFromFile(textFile);

        Base64InputStream symKey = getInputStreamFromFile(symKeyFile);
        byte[] symKey_bytes = symKey.readAllBytes();

        Base64InputStream iv = getInputStreamFromFile(ivFile);

        PrivateKey privateKey = getPrivateKey(ks);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.UNWRAP_MODE, privateKey);

        Key key = cipher.unwrap( symKey_bytes, "AES", Cipher.SECRET_KEY);

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv.readAllBytes()));

        byte[] decoded = cipher.doFinal(message.readAllBytes());
        createFile("deciphered_message.txt", decoded, false);
    }

    public static PrivateKey getPrivateKey(KeyStore ks) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        Enumeration<String> entries = ks.aliases();
        String alias = entries.nextElement();

        return (PrivateKey) ks.getKey(alias, pass.toCharArray());
    }

    public static Base64InputStream getInputStreamFromFile(String fileName) throws FileNotFoundException {
        FileInputStream baseIn = new FileInputStream(rootPath + fileName);
        return new Base64InputStream(baseIn);
    }

    public static void createFile(String fileName, byte[] data, Boolean base64) throws IOException {
        FileOutputStream baseOut = new FileOutputStream(rootPath + fileName);
        if (base64) {
            Base64OutputStream out = new Base64OutputStream(baseOut);
            out.write(data);
            out.close();
        } else {
            baseOut.write(data);
            baseOut.close();
        }
    }
}
