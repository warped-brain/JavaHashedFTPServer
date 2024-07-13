import java.util.*;
import java.io.*;


public class fileDeduplication {
    public static void main(String[] args) throws IOException {
        // Directory to scan for duplicate files
        String directoryPath = "/home/dombdomb/College-Study/Cryptography";

        duplicateFinder(directoryPath);

    }

    public static void duplicateFinder(String directoryPath) throws IOException {

        // Create a map to store file hashes and their paths
        int[] fileCount = {0};
//        hash -> Arrays[Paths]
        Map<String, ArrayList<String>> fileHashes = new HashMap<>();
//        FileName -> hashes
        Map<String,String> duplicates = new HashMap<>();

//        Recursively scan Files
        fileCrawler(directoryPath,fileHashes,duplicates, fileCount);

//        Print Files and ask for deletion
        System.out.println("Duplicate Files: ");
        int count = 0;
        for (Map.Entry<String, String> entry : duplicates.entrySet()) {
            System.out.println("File: " + entry.getKey() );
            count++;
            pathPrinterAndDeleter(fileHashes.get(entry.getValue()));
        }
        if (count == 0) System.out.println("\t\t\t None");
        System.out.println("There were duplicates of " + count + " file(s) out of " + fileCount[0]+ " total files.");
    }

//    Scan files recursively and update HashTables
    public static void fileCrawler(String directoryPath,Map<String,ArrayList<String>> fileHashes, Map<String,String> duplicates, int[] count) throws IOException {
    File directory = new File(directoryPath);
    File[] files = directory.listFiles();

    if (files != null) {
        for (File file : files) {
            if (file.isFile()) {
                count[0] = count[0] + 1;
//                System.out.println(file.getName());

                // Calculate the SHA-256 hash of a file
                String hash = fileSHAOptimised.calculateSHA256(file);
                if (fileHashes.containsKey(hash)) {
                    fileHashes.get(hash).add(file.getAbsolutePath());

                    // Duplicate file found
                    if (!duplicates.containsValue(hash)) duplicates.put(file.getName(), hash);

                } else {

                    ArrayList<String> x = new ArrayList<String>(1);
                    x.add(file.getAbsolutePath());
                    fileHashes.put(hash, x);
                }
            }
//            Recursion
            else if (file.isDirectory()){
                fileCrawler(file.getAbsolutePath(),fileHashes,duplicates,count);
            }
        }
    }
}


    private static void pathPrinterAndDeleter(ArrayList<String> x) {
        int f = 1;
        for (String s: x) {
            System.out.println("\t "+f+". " + s);
            f++;
        }
        System.out.println("Type the Serial No. of files to delete separated by space or 0 if None: ");
        int[] sNos = inputParser(x.size());
        if (sNos.length == 0) return;
        System.out.println("Deleting ...");
        for (int s: sNos) {
            File fD = new File(x.get(s-1));
            fD.delete();
            System.out.println("Deleted "+ x.get(s-1));
        }

    }

//    Get input From user and Parse
    private static int[] inputParser(int nF) {
        Scanner sc = new Scanner(System.in);
        String[] files = sc.nextLine().split(" ");
//        System.out.println(Arrays.toString(files));
        if (files[0].equals("0")){
            System.out.println("No file deleted.");
            return new int[]{};
        }
        int[] sNos = new int[files.length];
        boolean valid  = validInput(nF, sNos, files);
        if (valid) return sNos;
        else return inputParser(nF);
    }
//   Input Validation
    private static boolean validInput(int nF, int[] sNos, String[] files) {
        for (int i = 0; i < files.length; i++) {
            int j;
            try {
                j = Integer.parseInt(files[i]);
            } catch (NumberFormatException x) {
                System.out.println("Enter only valid Serial Numbers.");
                return false;
            }

            if (j <= nF) sNos[i] = j;
            else {
                System.out.println("Enter only valid Serial Numbers.");
                return false;
            }
        }
        return true;
    }
}
