import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Scanner;

public class FTPSClient {
    public static void main(String[] args) throws IOException, KeyStoreException, KeyManagementException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        String serverAddress = "localhost"; // IP address or hostname of the FTP server
        int serverPort = 8080; // Port number of the SSL/TLS FTP server

        // Load the truststore
        char[] truststorePassword = "password".toCharArray(); // Replace with your truststore password
        KeyStore truststore = KeyStore.getInstance("JKS");
        truststore.load(new FileInputStream("/home/dombdomb/College-Study/Cryptography/SHAFinal/src/clienttruststore.jks"), truststorePassword);

        // Set up the trust manager factory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(truststore);
        // Load the keystore for the server
        char[] keyStorePassword = "password".toCharArray(); // Replace with your keystore password
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream("/home/dombdomb/College-Study/Cryptography/SHAFinal/src/clientkeystore.jks"), keyStorePassword);

// Set up the key manager factory
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyStorePassword);

// Set up the SSL context with both key and trust managers
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);


        // Create an SSLSocket using the SSL context
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(serverAddress, serverPort);

        // Input and output streams for communication with the FTP server
        OutputStream socketOutS = socket.getOutputStream();
        InputStream socketInpS = socket.getInputStream();
        PrintWriter out = new PrintWriter(socketOutS, true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socketInpS));

        // Handle the request over the secure connection
        handleRequest(socket, socketOutS, socketInpS, out, in);

        // Close the SSLSocket
        socket.close();
    }


    public static void handleRequest(Socket socket, OutputStream socketOutS, InputStream socketInpS, PrintWriter out, BufferedReader in) throws IOException {
        // Create input and output streams for communication with the FTP server



        String serverStatus = in.readLine();
        StringBuilder filesInServer = new StringBuilder();
        String temp= in.readLine();
        while(!temp.equals("ListEnd.")){
            filesInServer.append(temp).append("\n");
            temp = in.readLine();
        };
        System.out.println(serverStatus);
        System.out.println(filesInServer);
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter EXIT to Exit: ");
        System.out.println("Enter which file to download: ");
        String requestedFile = sc.nextLine();
        if(requestedFile.equals("EXIT")) return;
        System.out.println("Enter path (with file name) to save (Eg: /home/src/<filename>.txt) LEAVE EMPTY to save in current folder:"  );
        String saveLocation = sc.nextLine();
        if(saveLocation.equals("EXIT")) return;

        if (saveLocation.isEmpty()) saveLocation = System.getProperty("user.dir")+"/"+requestedFile;


        // Sends the RETR command to request the file
        out.println("RETR " + requestedFile);

        // Read and print the response from the server
        String response;
        boolean isReceivingFile = false;
        long fLength = 0;
        String serverHash = "";
        File receivedFile = new File(saveLocation);
        if(receivedFile.exists()) receivedFile.delete();

        while ((response = in.readLine()) != null) {
//            System.out.println(response);
            FileOutputStream fileOutputStream;
            String finishResp = null;

            // Check for an error response (550)
            if (response.startsWith("550 ")) {
                System.out.println("Error: " + response);
                break;
            }

            // If the response indicates that the file transfer is starting (150), start receiving the file
            if (response.startsWith("150 ")) {
//                System.out.println(response);
                isReceivingFile = true;
                serverHash = response.substring(9,74).trim();
//                System.out.println(serverHash);
                fLength = Long.parseLong(response.substring(86).trim());
//                System.out.println(fLength);

                receivedFile.createNewFile(); // Create the local file
//                continue;
            }


            // If the file transfer is in progress, write the content to the local file
            if (isReceivingFile) {
                System.out.println("Receiving the File");


                if(fLength == 0) {
                    System.out.println("Empty File");
                    break;
                }
//                System.out.println(response);
                fileOutputStream = new FileOutputStream(receivedFile, false);
                try  {
                    int i = 0;
                    long num_Segments = fLength/4096;
                    int SIZE = 4*1024;
                    byte[] buffer = new byte[SIZE];
                    int bytesRead = 0;
                    long fRecieved = 0;
                    while ((bytesRead =socketInpS.read(buffer)) != -1) {
//                        System.out.print(".");
                        fRecieved += bytesRead;
//                        System.out.println(new String(buffer));
                        fileOutputStream.write(buffer, 0, bytesRead);
                        if (fRecieved == fLength){
                            System.out.println("\n226 File transfer completed");
                            break;
                        }
//                        fileOutputStream.close();

                    }
                    fileOutputStream.close();
                    break;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else System.out.println(response);

        }
        if (!serverHash.isEmpty()) {
            System.out.println("Checking Integrity using SHA 256: ");
            String localHash = fileSHAOptimised.calculateSHA256(receivedFile);
            System.out.println("Server Hash: "+ serverHash);
            System.out.println("Local Hash: "+ localHash);
            if (serverHash.equals(localHash)){
                System.out.println("The files are the same.");
            }
            else {
                System.out.println("The file has been tampered with.");
            }
        }
    }
}