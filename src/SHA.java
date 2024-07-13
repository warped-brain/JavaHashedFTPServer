import javax.management.remote.JMXServerErrorException;
import java.util.Arrays;

public class SHA {

    public static void main(String[] args) {
        String[] messages = {"hello world", "SHA256", "Cryptography" , "CEN"};

        for (String message: messages) {
            System.out.println("Message: "+ message);
            String hash = sha256(message);
            System.out.println("SHA-256 Hash: " + hash);
        }

    }
//    Convert String to Binary and Pad with zeros and length
    public static String preProcessString(String message){
        String binMessage = utilityFunctions.stringToBinary(message);
        String paddedMessage = padWithZeros(binMessage);
        return paddedMessage;
    }
    private static String padWithZeros(String binMessage){
        int lengthOfZeros;
        String binaryChar = Integer.toBinaryString(binMessage.length()-1);

//        Calculate no. of Zeroes
        int contentLength = 512 - ((binMessage.length() % 512)) - binaryChar.length();
        if (contentLength > 0) {
            lengthOfZeros = (512 - (binMessage.length() % 512)) - binaryChar.length();
        }
        else {
            lengthOfZeros = 1024 - contentLength;
        }
        StringBuilder newString = new StringBuilder();

        newString.append(binMessage);

        for (int i = 0; i < lengthOfZeros;i++) newString.append("0");

        return newString.append(binaryChar).toString();
    }

    public static String sha256(String message) {
        String paddedMessage = preProcessString(message);
        int nChunks = paddedMessage.length() / 512;

        int[] hashes = Constants.getHashes();
        int [] K = Constants.getConstants();

        //Chunk Loop
        for (int i = 0; i < nChunks; i++){

            String[] messageSchedule = createMessageSchedule(paddedMessage.substring(i*512, (i+1)* 512));
//            System.out.println(Arrays.toString(messageSchedule));
            int a = hashes[0];
            int b = hashes[1];
            int c = hashes[2];
            int d = hashes[3];
            int e = hashes[4];
            int f = hashes[5];
            int g = hashes[6];
            int h = hashes[7];
            // MessageSchedule/ Compression Loop
            for (int t = 0; t < 64; t++){
                int S1 = sOne(e);
                int ch = ch(e,f,g);
                int temp1 = h + S1 + ch + K[t] + Integer.parseUnsignedInt(messageSchedule[t], 2);
                int S0 = sZero(a);
                int maj = maj(a,b,c);
                int temp2 = maj + S0;
                h = g;
                g = f;
                f = e;
                e = d + temp1;
                d = c;
                c = b;
                b = a;
                a = temp1 + temp2;

            }

            //  Compute H(n) - Update hashes array
            hashes[0] = hashes[0] + a;
            hashes[1] = hashes[1] + b;
            hashes[2] = hashes[2] + c;
            hashes[3] = hashes[3] + d;
            hashes[4] = hashes[4] + e;
            hashes[5] = hashes[5] + f;
            hashes[6] = hashes[6] + g;
            hashes[7] = hashes[7] + h;

        }

        return finalHash(hashes);
    }




//    Create message schedule
    public static String[] createMessageSchedule(String paddedMessage){
        String[] W = new String[64];
        for (int i = 0; i < 16; i++) {
            W[i]  = paddedMessage.substring(i*32,i*32+32);
        }
        for (int i = 16; i < 64; i++) {

            String s0 = sigmaZero(W[i-15]);
            String s1 = sigmaOne(W[i-2]);
//            System.out.println(Arrays.toString(new String[] {W[i-16],s0,W[i-7],s1}));
            W[i] = binaryAdditionModulo(new String[] {W[i-16],s0,W[i-7],s1});

        }
        return W;
    }
    public static int ch(int e,int f, int g){
        return (e & f) ^ (~e & g);
    }
    public static int maj(int e,int f, int g){
        return (e & f) ^ (e & g) ^ (f & g);
    }
    public static String sigmaZero (String k) {
        String r1 = rightRotate(k,7);
        String r2 = rightRotate(k,18);
        String r3 = rightShift(k,3);

        String t1 = XOR(r1,r2);

        return XOR(t1,r3);
    }
    public static String sigmaOne (String k) {
        String r1 = rightRotate(k,17);
        String r2 = rightRotate(k,19);
        String r3 = rightShift(k,10);
//        System.out.println("s0 = " + Arrays.toString(new String[]{r1,r2,r3}));

        String t1 = XOR(r1,r2);
        return XOR(t1,r3);
    }
    public static int sOne(int e){
        String k = String.format("%32s", Integer.toBinaryString(e)).replace(' ', '0');
        String k1 = rightRotate(k,6);
        String k2 = rightRotate(k,11);
        String k3 = rightRotate(k,25);
        String t1 = XOR(k1,k2);
        String res = XOR(k3,t1);
        return Integer.parseUnsignedInt(res,2);
    }
    public static int sZero(int e){
        String k = String.format("%32s", Integer.toBinaryString(e)).replace(' ', '0');
        String k1 = rightRotate(k,2);
        String k2 = rightRotate(k,13);
        String k3 = rightRotate(k,22);
        String t1 = XOR(k1,k2);
        String res = XOR(k3,t1);
        return Integer.parseUnsignedInt(res,2);
    }


    public static String rightRotate(String s, int k) {
        int n = s.length();
        k = k % n;

        String rotatedSubstring = s.substring(n - k) + s.substring(0, n - k);

        return rotatedSubstring;
    }

    public static String XOR(String a, String b) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            if(a.charAt(i) == b.charAt(i)){
                result.append("0");
            }
            else {
                result.append("1");
            }
        }
        return result.toString();
    }
    public static String rightShift(String s, int k) {
        int n = s.length();
        k = k % n;
        StringBuilder zeroString = new StringBuilder();
        for (int i = 0; i < k; i++) {
            zeroString.append("0");
        }
        String shiftedSubstring = zeroString.toString() + s.substring(0, n - k);

        return shiftedSubstring;
    }


    public static String binaryAdditionModulo(String[] binaryStrings) {
        int result = 0;
        for (String binaryStr : binaryStrings) {
            result = result + Integer.parseUnsignedInt(binaryStr,2);
        }
        String binaryResult = String.format("%32s", Integer.toBinaryString(result)).replace(' ', '0');
        return binaryResult;
    }

//    Concatenate the hashes
    public static String finalHash(int[] hashes){
        StringBuffer hash = new StringBuffer(256);
        for (int h: hashes) {
            hash.append(Integer.toHexString(h));
//            System.out.println(String.format("%32s" ,Integer.toBinaryString(h)).replace(" ", "0"));
        }
        return hash.toString();
    }



}

