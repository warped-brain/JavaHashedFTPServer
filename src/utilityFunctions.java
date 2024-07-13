//import java.io.*;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
public class utilityFunctions {
    //    ConvertString to Binary
    public static String stringToBinary(String input) {
        StringBuilder binaryStringBuilder = new StringBuilder();

        for (char c : input.toCharArray()) {
            String binaryChar = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            binaryStringBuilder.append(binaryChar);
        }

        return binaryStringBuilder.append("1").toString().trim();
    }
}
//    static String calculateSHA256BuiltIn(File file) throws NoSuchAlgorithmException, IOException {
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            FileInputStream fis = new FileInputStream(file);
//            byte[] dataBytes = new byte[1024];
//            int bytesRead;
//
//            while ((bytesRead = fis.read(dataBytes)) != -1) {
//                md.update(dataBytes, 0, bytesRead);
//            }
//
//            byte[] hashBytes = md.digest();
//
//            // Convert the byte array to a hexadecimal string
//            StringBuilder sb = new StringBuilder();
//            for (byte hashByte : hashBytes) {
//                sb.append(String.format("%02x", hashByte));
//            }
//
//            return sb.toString();
//    }
//}
