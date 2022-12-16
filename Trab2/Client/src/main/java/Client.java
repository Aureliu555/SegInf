import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;



public class Client {

    public static String path = "src/main/resources/";

    public static void app() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException {
        System.out.println("Associated keystore:");
        String keyStore = new Scanner(System.in).nextLine();
        System.out.println("Password:");
        String pass = new Scanner(System.in).nextLine();

        SSLSocketFactory sslFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        // print cipher suites avaliable at the client
        String[] cipherSuites = sslFactory.getSupportedCipherSuites();
        for (int i=0; i<cipherSuites.length; ++i) {
            System.out.println("option " + i + " " + cipherSuites[i]);
        }


        KeyStore ks =  KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(path + keyStore), pass.toCharArray() );
        TrustManagerFactory trustMgrFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustMgrFactory.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustMgrFactory.getTrustManagers(), new SecureRandom());

        SSLSocketFactory sslSocketFactory = context.getSocketFactory();

        SSLSocket client = (SSLSocket) sslSocketFactory.createSocket("www.secure-server.edu",4433);
        client.startHandshake();
        SSLSession session = client.getSession();
        System.out.println("Cipher suite: " + session.getCipherSuite());
        System.out.println("Protocol version: " + session.getProtocol());
        System.out.println(session.getPeerCertificates()[0]);


    }







    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException {
        app();


    }
}