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
import java.security.cert.*;
import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;


public class Exercise6 {
    public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, KeyStoreException, InvalidAlgorithmParameterException, UnrecoverableKeyException, CertPathValidatorException {
        app();
    }

    public static String pass = "changeit";
    public static String mainPath = "src/main/files/";

    public static void app() throws KeyStoreException, IOException, InvalidAlgorithmParameterException, UnrecoverableKeyException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, CertificateException, CertPathValidatorException {
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
                    ks.load(new FileInputStream(mainPath + l[0]), pass.toCharArray());
                    decipher("ciphered_message.txt", "ciphered_key.txt", ks, "ciphered_iv.txt");
                    System.out.println("Message successfully decrypted");
                }
                case "-enc" -> {
                    encipher(l[0], mainPath + l[1]);
                    System.out.println("Message and Key successfully encrypted");
                }
                default -> System.out.println("Insert a valid command");
            }

            r = printAndReadOption();
        }
    }

    public static String printAndReadOption() {
        System.out.println("\nEncipher: <file> <.cer> -enc\nDecipher: <.pfx> -dec\nExit App: exit\n");
        return new Scanner(System.in).nextLine();
    }

    public static void encipher(String file, String cer) throws CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, CertPathValidatorException {
        //Certificate and Public Key
        String[] certificates = { cer, "CA1-int.cer", "CA1.cer" };

        X509Certificate certificate = validateCertificate(certificates);

        // Verifica a validade do período do certificado.
        certificate.checkValidity();

        PublicKey publicKey = certificate.getPublicKey();

        byte[] msg = Exercise5.messageFromPath(mainPath + file);

        SecretKey symKey = getSecretKey();

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        cipher.init( Cipher.WRAP_MODE, publicKey);

        byte[] encodedKey = cipher.wrap( symKey );

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, symKey);

        byte[] bytes = cipher.doFinal(msg);
        byte[] iv = cipher.getIV();

        createFile("ciphered_key.txt", encodedKey, true);
        createFile("ciphered_message.txt", bytes, true);
        createFile("ciphered_iv.txt", iv, true);
    }

    public static X509Certificate validateCertificate(String[] certificates) throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, InvalidAlgorithmParameterException, CertPathValidatorException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        FileInputStream in = new FileInputStream(certificates[0]);

        X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);

        //Certificate validation
        List<X509Certificate> myCertificates = new ArrayList<>();
        myCertificates.add(certificate);

        FileInputStream inInt = new FileInputStream("src/main/certificates-and-keys/cert-int/"+ certificates[1]);
        FileInputStream inAnchor = new FileInputStream("src/main/certificates-and-keys/trust-anchors/"+ certificates[2]);

        X509Certificate certInt = (X509Certificate) cf.generateCertificate(inInt);
        X509Certificate trust = (X509Certificate) cf.generateCertificate(inAnchor);

        myCertificates.add(certInt);

        CertPath cp = cf.generateCertPath(myCertificates);
        //Trust anchor validation
        TrustAnchor anchor = new TrustAnchor(trust, null);
        PKIXParameters parameters = new PKIXParameters(Collections.singleton(anchor));
        parameters.setRevocationEnabled(false);
        CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
        PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) cpv.validate(cp, parameters);
        System.out.println("The certificate was successfully verified");

        return certificate;
    }

    public static SecretKey getSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom secRandom = new SecureRandom();
        keyGen.init(secRandom);

        return keyGen.generateKey();
    }

    public static <Base64InputStream> void decipher(String textFile, String symKeyFile, KeyStore ks, String ivFile) throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

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
        FileInputStream baseIn = new FileInputStream(mainPath + fileName);
        return new Base64InputStream(baseIn);
    }

    public static void createFile(String fileName, byte[] data, Boolean base64) throws IOException {
        FileOutputStream baseOut = new FileOutputStream(mainPath + fileName);
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
