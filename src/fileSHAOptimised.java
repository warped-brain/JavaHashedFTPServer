import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class fileSHAOptimised {
    public static void main(String[] args) throws IOException {
        System.out.println(calculateSHA256(new File("/home/dombdomb/" +
                "2023-11-16 10-27-05.mkv")));
    }
    public static String calculateSHA256(File file) throws IOException {

        //Read from File as 64 Bytes at a time
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[64];
        int bytesRead;
        int chunkCount = 0;
        int[] hashes = Constants.getHashes();
        int [] K = Constants.getConstants();

        while ((bytesRead = fis.read(buffer)) != -1) {
            // Process the 512-bit chunk
            chunkCount++;
            if (bytesRead == 64) {
                int[] chunk = new int[16];
                for (int i = 0; i < 16; i++) {
                    chunk[i]  = bytesToInt(new byte[] {buffer[4*i] , buffer[(4*i)+1] , buffer[(4*i)+2] , buffer[(4*i)+3]});
                }
                sha256ProcessChunk(chunk,hashes,K);
            }

            //Last Chunk
            else{

                long fLength= (chunkCount - 1)* 512L + bytesRead* 8L;
                ArrayList<int[]> nChunks= createChunkStringFinal(buffer,bytesRead,fLength);
                for (int j = 0; j < nChunks.size();j++) {
                    sha256ProcessChunk(nChunks.get(j), hashes, K);
                }
            }
        }

        return finalHash(hashes);
    }

//    Convert 4 bytes concatenated to Int
    public static int bytesToInt(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | (bytes[i] & 0xFF);
        }

        return result;
    }
//    Final chuck that needs file length and different padding
    public static ArrayList<int[]> createChunkStringFinal(byte[] bArray, int bRead, long fLength){
        StringBuffer c = new StringBuffer(512);
        String fileLengthBin = Long.toBinaryString(fLength);
        for(int i = 0; i < bRead; i++){
//            System.out.println(String.format("%8s",Integer.toBinaryString(Byte.toUnsignedInt(bArray[i]))).replace(" ","0"));
            c.append(String.format("%8s",Integer.toBinaryString(Byte.toUnsignedInt(bArray[i]))).replace(" ","0"));
        }
        c.append("1");
        int pads = 511 - 8*bRead - 64;
        if (pads < 0) {
            pads = (512 - 1 - 8*bRead) + (512 - 64);
            for(int j = 0; j< pads; j++){
                c.append("0");
            }
        }
        else {
            for(int j = 0; j< pads; j++){
                c.append("0");
            }
        }

        String fileLBin = String.format("%64s", fileLengthBin).replace(" ","0");
        c.append(fileLBin);

        ArrayList<int[]> res = new ArrayList<>();

            for (int i = 0; i < c.length()/512; i++){
                int[] bufferArray = new int[16];

                int k = i*512;
                for (int j = 0; j < 16; j++) {
                    bufferArray[j] = Integer.parseUnsignedInt(c.substring(k+(j*32),k+((j+1)*32)),2);
                }
                res.add(bufferArray);
            }
            return res;
    }

//    Hash values updation -> Message Schedule, Compression
    public static void sha256ProcessChunk(int[] messageChunk, int[] hashes,int[] K) {
        int[] messageSchedule = createMessageSchedule(messageChunk);
        int a = hashes[0];
        int b = hashes[1];
        int c = hashes[2];
        int d = hashes[3];
        int e = hashes[4];
        int f = hashes[5];
        int g = hashes[6];
        int h = hashes[7];
        // MessageSchedule/ Compression Loop
        for (int t = 0; t < 64; t++) {
            int S1 = sOne(e);
            int ch = ch(e, f, g);
            int temp1 = h + S1 + ch + K[t] + messageSchedule[t];
            int S0 = sZero(a);
            int maj = maj(a, b, c);
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
        return;
    }




//Create Message Schedule
    public static int[] createMessageSchedule(int[] chunk){
        int[] W = new int[64];
        for (int i = 0; i < 16; i++) {
            W[i]  = chunk[i];
        }

        for (int i = 16; i < 64; i++) {

            int s0 = sigmaZero(W[i-15]);
            int s1 = sigmaOne(W[i-2]);

            W[i] = binaryAdditionModulo(new int[] {W[i-16],s0,W[i-7],s1});

        }
        return W;
    }
    public static int ch(int e,int f, int g){
        return (e & f) ^ (~e & g);
    }
    public static int maj(int e,int f, int g){
        return (e & f) ^ (e & g) ^ (f & g);
    }
    public static int sigmaZero (int k) {

        return Integer.rotateRight(k,7) ^ Integer.rotateRight(k,18) ^ (k >>> 3);
    }
    public static int sigmaOne (int k) {

        return Integer.rotateRight(k,17) ^ Integer.rotateRight(k,19) ^ (k >>> 10);
    }
    public static int sOne(int k){

        return Integer.rotateRight(k,6) ^ Integer.rotateRight(k,11) ^ Integer.rotateRight(k,25);
    }
    public static int sZero(int k){

        return Integer.rotateRight(k,2) ^ Integer.rotateRight(k,13) ^ Integer.rotateRight(k,22);
    }


//    Addition modulo 2^32
    public static int binaryAdditionModulo(int[] words) {
        int result = 0;
        for (int w : words) {
            result = result+w;

        }
        return result;
    }

//    Concatenating the Hashes
    public static String finalHash(int[] hashes){
        StringBuffer hash = new StringBuffer(256);
        for (int h: hashes) {
            hash.append(String.format("%08x", h));
//            System.out.println(String.format("%32s" ,Integer.toBinaryString(h)).replace(" ", "0"));
        }
        return hash.toString();
    }


}
