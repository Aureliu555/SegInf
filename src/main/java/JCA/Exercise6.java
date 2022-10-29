package JCA;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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

    public static void app() throws KeyStoreException, IOException, InvalidAlgorithmParameterException, UnrecoverableKeyException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, CertificateException {
        options();
        String r = new Scanner(System.in).nextLine();

        while (!Objects.equals(r, "exit")){
            String[] l = r.split(" ");

            if(l.length == 1 || l.length > 3 ) {
                System.out.println("Insert a valid command");
                options();
                r = new Scanner(System.in).nextLine();
                continue;
            }

            switch (l[l.length-1]) {
                case "-dec" -> {
                    KeyStore ks =  KeyStore.getInstance("PKCS12");
                    ks.load(new FileInputStream("src/main/files/"+ l[0]),"changeit".toCharArray());
                    decipher("message.txt", "key.txt", ks, "iv.txt");
                }
                case "-enc" -> {
                    encipher(l[0], "src/main/files/" + l[1]);
                }
                default -> System.out.println("Insert a valid command");
            }

            options();
            r = new Scanner(System.in).nextLine();
        }
    }
    public static void options() {
        System.out.println("\n<message><.cer>-enc\n<.pfx>-dec\nexit\n");
    }

    public static void encipher(String file, String cer) throws CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        //Certificate and Public Key
        FileInputStream in = new FileInputStream(cer);

        // Gera objeto para certificados X.509.
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Gera o certificado a partir do ficheiro.
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);

        // Verifica a validade do período do certificado.
        certificate.checkValidity();

        // Obtém a chave pública do certificado.
        PublicKey pk = certificate.getPublicKey();

        byte[] msg = Exercise5.messageFromPath("src/main/files/" + file);

        //hashCalculator(msg);

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");

        SecureRandom secRandom = new SecureRandom();

        keyGen.init(secRandom);

        SecretKey symKey = keyGen.generateKey();

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        cipher.init( Cipher.WRAP_MODE, pk);

        byte[] encodedKey = cipher.wrap( symKey );

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, symKey);

        byte[] bytes = cipher.doFinal(msg);

        //ciphered symmetric key
        FileOutputStream baseOut = new FileOutputStream("src/main/files/key.txt");
        Base64OutputStream out = new Base64OutputStream(baseOut);
        out.write(encodedKey);
        out.close();

        //ciphered message
        baseOut = new FileOutputStream("src/main/files/message.txt");
        out = new Base64OutputStream(baseOut);
        out.write(bytes);
        out.close();

        //IV generator
        baseOut = new FileOutputStream("src/main/files/iv.txt");
        out = new Base64OutputStream(baseOut);

        byte[] iv = cipher.getIV();
        out.write(iv);
        out.close();

    }

    public static void decipher(String textFile, String symKeyFile, KeyStore ks, String ivFile) throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        //Open the base64 files
        FileInputStream baseIn = new FileInputStream("src/main/files/"+textFile);
        Base64InputStream message = new Base64InputStream(baseIn);

        baseIn = new FileInputStream("src/main/files/"+symKeyFile);
        Base64InputStream symKey = new Base64InputStream(baseIn);
        byte[] symKey_bytes = symKey.readAllBytes();

        baseIn = new FileInputStream("src/main/files/"+ivFile);
        Base64InputStream iv = new Base64InputStream(baseIn);

        //Certificate creation
        Enumeration<String> entries = ks.aliases();
        String alias = entries.nextElement();

        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, "changeit".toCharArray());

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.UNWRAP_MODE, privateKey);

        Key key = cipher.unwrap( symKey_bytes, "AES", Cipher.SECRET_KEY);

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv.readAllBytes()));

        byte[] decoded = cipher.doFinal(message.readAllBytes());
        FileOutputStream baseOut = new FileOutputStream("src/main/files/deciphered_message.txt");
        baseOut.write(decoded);
        baseOut.close();

    }

}
