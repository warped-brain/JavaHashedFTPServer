import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.Scanner;

public class FTPSServer {
    public static void main(String[] args) {
        int port = 8080; // Change to the desired SSL port

        // User Inputs
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the directory to Share via Socket (Leave Empty & press ENTER for sharing current directory).");
        String d = sc.nextLine();
        if (d.isEmpty()) {
            d = System.getProperty("user.dir") + "/";
            System.out.println(d);
        }
        File directory = new File(d);
        while (!directory.isDirectory()) {
            System.out.println("Enter Valid Directory: ");
            directory = new File(sc.nextLine());
        }
        String fileNames = allFiles(d);

        // Handle the requests
        try {
            // Load the keystore
            char[] password = "password".toCharArray(); // Change to your keystore password
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("/home/dombdomb/College-Study/Cryptography/SHAFinal/src/serverkeystore.jks"), password);

            // Set up the key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password);

            // Set up the SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            // Create SSL server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            // Create server socket
            SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
            System.out.println("FTP server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Wait for a client to connect

                // Handle the client request in a separate thread
                ClientHandle c = new ClientHandle(clientSocket, fileNames, d);
                c.run();
            }

        } catch (IOException | java.security.GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public static String allFiles(String currentDirectory){
        StringBuilder fileNames = new StringBuilder();
        // Create a File object for the current directory
        File directory = new File(currentDirectory);

        // List all files in the current directory
        File[] files = directory.listFiles();

        if (files != null) {
            fileNames.append("Files in the directory:\n");
            for (File file : files) {
                if (file.isFile()) {
                    fileNames.append(file.getName()).append("\n");
                }
            }
        } else {
            fileNames.append("No files found in the directory.");
        }
        return fileNames.append("\nListEnd.").toString();
    }

    static class ClientHandle {
        private Socket clientSocket;

        private String fileList;
        private String directory;

        public ClientHandle(Socket clientSocket, String allFiles, String directory) {
            this.clientSocket = clientSocket;
            this.fileList = allFiles;
            this.directory = directory;
        }

        public void run() {
//            Get input and output streams
            try {
                OutputStream socketOutS = clientSocket.getOutputStream();
                InputStream socketInpS = clientSocket.getInputStream();

                PrintWriter out = new PrintWriter(socketOutS, true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socketInpS));



                out.println("Service Ready."); // Send initial response
                out.println(this.fileList);
//                out.println(File);
                String clientCommand;
//                Process the commands
                while ((clientCommand = in.readLine()) != null) {
                    System.out.println("Received command: " + clientCommand);

                    if (clientCommand.startsWith("RETR")) {
                        String fileName = clientCommand.substring(5).trim();
                        sendFile(this.directory+fileName, socketOutS, out);
                    } else if (clientCommand.startsWith("QUIT")) {
                        out.println("Quitting.");
                        break;
                    } else {
                        out.println("Invalid Command.");
                    }
                }

                // Close the client socket
//                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private void sendFile(String fileName, OutputStream socketOutS, PrintWriter out) throws IOException {
            File file = new File(fileName);
            //Handle file formats and availability
            if (!file.exists()) {
                System.out.println(fileName);
                sendResponse(out, "550 Requested File Not Found");
                return;
            }

//            if (!fileName.endsWith(".txt")) {
//                sendResponse(out, "550 Incorrect File Format");
//                return;
//            }
            String fileHash = fileSHAOptimised.calculateSHA256(file);
            try (FileInputStream fileInputStream = new FileInputStream(file)){
                sendResponse(out, "150"+ " hash "+ fileHash +" file-length " + file.length());
                byte[] buffer = new byte[4*1024];

                int bytesRead;


                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//                    System.out.println(bytesRead);
                    socketOutS.write(buffer, 0, bytesRead);
//                    System.out.println(new String(buffer));
                }
                socketOutS.flush();
                // After sending the file, send the final response.
            } catch (IOException e) {
                sendResponse(out, "550 Error sending the file");
            }
        }


        private void sendResponse(PrintWriter out, String response) {
            out.println(response);
        }


    }
}
