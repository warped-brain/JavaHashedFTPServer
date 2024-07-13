import java.io.File;
import java.security.*;
import javax.crypto.Cipher;
import java.util.Base64;

public class digitalSignature {

        public static void main(String[] args) throws Exception {
                // Generate a key pair
                KeyPair keyPair = generateKeyPair();

                File sentFile = new File("/home/dombdomb/Documents/t.txt");
                // Original message
                String hash = fileSHAOptimised.calculateSHA256(sentFile);

                // Sign using senders Private Key
                String signedHash = signWithPrivateKey(hash, keyPair.getPrivate());

                System.out.println("Original hash of the file: " + hash);
                System.out.println("Signed Hash: " + signedHash);

                // Decrypt the received sign using the sender's public key
                String decryptedHash = decryptWithPublicKey(signedHash, keyPair.getPublic());

                // Hashing the received file

                File receivedFile = new File("/home/dombdomb/Documents/t2.txt");

                // Original message
                String recipientHash = fileSHAOptimised.calculateSHA256(receivedFile);
                System.out.println("Received file's hash: "+ recipientHash);
                System.out.println("Decrypted Hash: " + decryptedHash);
//                Checking Hash Values
                if(recipientHash.equals(decryptedHash)) {
                    System.out.println("\nThe hashes are the same and the identity is verified.");
                }
                else{
                    System.out.println("\nThe file is of unknown origin or the file has been tampered.");
                }


        }

//        Key Generation
        public static KeyPair generateKeyPair() throws Exception {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // You can adjust the key size
            return keyPairGenerator.generateKeyPair();
        }

//        create Signature
        public static String signWithPrivateKey(String hash, PrivateKey privateKey) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptedBytes = cipher.doFinal(hash.getBytes());
            String signedHash = Base64.getEncoder().encodeToString(encryptedBytes);
            return signedHash;
        }

//        Decrypt Signature
        public static String decryptWithPublicKey(String signedHash, PublicKey publicKey) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(signedHash);
            byte[] hash = cipher.doFinal(encryptedBytes);
            return new String(hash);
        }
    }


