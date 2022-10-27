package JCA;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.util.Objects;
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.spec.InvalidKeySpecException;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import JCA.Exercise5.*;


public class App {
    public static void encipher(String file, String cer) throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        //----------------------------------------- Certificate and Public Key ------------------------------------------------------------
        FileInputStream in = new FileInputStream(cer);
        System.out.print(in);

        // Gera objeto para certificados X.509.
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Gera o certificado a partir do ficheiro.
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);

        // Verifica a validade do período do certificado.
        certificate.checkValidity();
        System.out.println("Certificado válido (período de validade)");

        // Obtém a chave pública do certificado.
        PublicKey pk = certificate.getPublicKey();

               
        byte[] msg = Exercise5.messageFromPath("src/main/files/"+file);

        hashCalculator(msg);

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");

        SecureRandom secRandom = new SecureRandom();
		
		keyGen.init(secRandom);

		SecretKey symKey = keyGen.generateKey();   

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
  
        cipher.init( Cipher.WRAP_MODE, pk );

        byte[] encodedKey = cipher.wrap( symKey );

        byte[] bytes = cipher.doFinal(msg);

        Path path = Paths.get("src/main/files");

        Files.write(path, encodedKey);
        Files.write(path, bytes);

    }


    public static void hashCalculator(byte[] file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("AES");
        md.update(file);
        byte[] h = md.digest();
        prettyPrint(h);
    }

    public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        String r = new Scanner(System.in).nextLine();

        while (!Objects.equals(r, "exit")){
            String[] l = r.split("");

            if(l.length == 1) {
                System.out.println("Invalid arguments");
                break;
            }

            switch (l[1]) {
                case "-dec" -> System.out.print("fail");//decipher();
                case "-enc" -> encipher("", "");
                default -> System.out.println("Insert a valid command.");
            }

            r = new Scanner(System.in).nextLine();
        }

    }
    private static void prettyPrint(byte[] tag) {
        for (byte b: tag) {
            System.out.printf("%02x", b);
        }
        System.out.println();
    }
}
