package JCA;

import javax.crypto.Cipher;
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
import java.security.KeyFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.NoSuchAlgorithmException;

public class App {
    public static void encipher(String file, String cer) throws FileNotFoundException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
        //----------------------------------------- Certificate and Public Key ------------------------------------------------------------
        FileInputStream in = new FileInputStream(cer);

        // Gera objeto para certificados X.509.
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Gera o certificado a partir do ficheiro.
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);

        // Verifica a validade do período do certificado.
        certificate.checkValidity();
        System.out.println("Certificado válido (período de validade)");

        // Obtém a chave pública do certificado.
        PublicKey pk = certificate.getPublicKey();

        //---------------------------------------------- Key Encipher Process with Public Key --------------------------------------------------------

        // Gera os bytes para o vetor de bytes correspondente a chave
        byte[] keyBytes = {0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef, 0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef};

        // Gera chave a partir do vetor de bytes (valor fixo, não aleatório)
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        System.out.println("key:" + key);

        // Gera o objeto da cifra simetrica
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        // Associa a chave key a cifra
        cipher.init(cipher.ENCRYPT_MODE, key);


        //------------------------------------------------ File Encipher Process -----------------------------------------------------
        //file encipher process


        //----------------------------------------------- Generate Ehe Enciphered Files ----------------------------------------------
        // Converte o objeto pk para RSAPublicKey.
        KeyFactory factory = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec pkRSA = factory.getKeySpec(pk, RSAPublicKeySpec.class);

        // Mostra informações da chave pública:
        System.out.println("Algoritmo da chave pública: " + pk.getAlgorithm());
        System.out.print("Chave pública: ");
        prettyPrint(pk.getEncoded());
        System.out.println("Expoente (BigInt): " + pkRSA.getPublicExponent());
        System.out.println("Modulus (BigInt): " + pkRSA.getModulus());

        // Alguns exemplos de acesso aos campos do certificado:
        System.out.println("Tipo: " + certificate.getType());
        System.out.println("Versão: " + certificate.getVersion());
        System.out.println("Algoritmo de assinatura: " + certificate.getSigAlgName());
        System.out.println("Período: " + certificate.getNotBefore() + " a " + certificate.getNotAfter());
        System.out.print("Assinatura: ");
        prettyPrint(certificate.getSignature());

        // Verifica a validade do período do certificado.
        certificate.checkValidity();
        System.out.println("Certificado válido (período de validade)");
    }

    //public static void enc()
    public static void decipher(){
        System.out.println("Decipher");
    }
    public static void main(String[] args) throws FileNotFoundException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
        String r = new Scanner(System.in).nextLine();

        while (!Objects.equals(r, "exit")){
            String[] l = r.split("");

            if(l.length == 1) {
                System.out.println("Invalid arguments");
                break;
            }

            switch (l[1]) {
                case "-dec" -> decipher();
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
