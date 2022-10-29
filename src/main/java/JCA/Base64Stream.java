package JCA;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Base64Stream {
    public static void main(String[] args) throws IOException {
        FileOutputStream baseOut = new FileOutputStream("src/main/files/test_file.txt");
        Base64OutputStream out = new Base64OutputStream(baseOut);
        out.write(new byte[]{1,2,3});
        out.write(new byte[]{45,9,2,23,34,14,4,34,34,11});
        out.close();

        FileInputStream baseIn = new FileInputStream("src/main/files/test_file.txt");
        Base64InputStream in = new Base64InputStream(baseIn);
        int value;
        while ((value = in.read()) != -1) {
            System.out.println(value);
        }
        in.close();
    }
}
