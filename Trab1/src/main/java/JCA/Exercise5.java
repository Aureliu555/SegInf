package JCA;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Scanner;

public class Exercise5 {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        while(true){
            System.out.print("\n1. Calculate hash\n2. Exit\n");
            if (Objects.equals(new Scanner(System.in).nextLine(), "2"))
                System.exit(-1);
            hashCalculator();
        }
    }

    public static void hashCalculator() throws NoSuchAlgorithmException, IOException {
        //User interface
        System.out.print("File name: ");
        String fileName = new Scanner(System.in).nextLine();
        System.out.print("Algorithm: ");
        String algorithm = new Scanner(System.in).nextLine().toUpperCase();
        //System.out.println(Arrays.toString(msg));

        MessageDigest md = MessageDigest.getInstance(algorithm);

        byte[] msg = messageFromPath("src/main/files/"+fileName);
        md.update(msg);

        byte[] h = md.digest();
        prettyPrint(h);
    }

    public static byte[] messageFromPath(String dir) throws IOException {
        Path path = Paths.get(dir);
        return Files.readAllBytes(path);
    }

    private static void prettyPrint(byte[] h) {
        System.out.print("\nHash: ");
        for (byte b : h) {
            System.out.printf("%02x", b);
        }
        System.out.println();
    }
}
