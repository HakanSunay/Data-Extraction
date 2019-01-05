package DataExtraction;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {
    private Socket s;

    private final static int BUFFER_SIZE = 1024;

    public Client() {
        this.s = new Socket();
    }

    public void connect(String host, int port) throws IOException {
        SocketAddress address = new InetSocketAddress(host, port);
        this.s.connect(address);

        System.out.println("You have connected to the Data Extraction server!");

        try (OutputStream os = s.getOutputStream();
             DataOutputStream dos = new DataOutputStream(os);
             Scanner sc = new Scanner(System.in);
             DataInputStream clientData = new DataInputStream(s.getInputStream())) {

            File file = enterValidFileName(sc);
            if (file == null) {
                return;
            }
            float minsup = enterValidMinsup(sc);

            sendFile(file, minsup, os, dos);

            receiveFile(clientData, file.getName());

        } finally {
            closeConnection();
        }
    }

    private void closeConnection() throws IOException {
        if (this.s != null) {
            this.s.close();
        }
    }

    private float enterValidMinsup(Scanner sc) {
        int maxAttempts = 5;
        do {
            System.out.print("Enter the minsup. For best results, please enter a number ~ 0.0001: ");
            try {
                float minsup = sc.nextFloat();
                if (minsup < 0.0 || minsup > 1.0) {
                    throw new InputMismatchException();
                }
                return minsup;
            } catch (InputMismatchException e) {
                System.out.println("The minsup must be a floating number between 0,0 and 1,0");
                sc.next();
            }
        } while (--maxAttempts > 0);
        System.out.println("The default value of 1,0 will be assigned to the minsup.");
        return 0.0001f;
    }

    private File enterValidFileName(Scanner sc) throws IOException {
        int maxAttempts = 5;
        do {
            System.out.print("Enter the name of the input file: ");
            String filename = sc.nextLine();
            File file = new File(filename);
            if (file.exists() && Files.probeContentType(file.toPath()).equals("text/plain")) {
                return file;
            }
            System.out.println("There is no text file with that name!");
        } while (--maxAttempts > 0);
        System.out.println("You have reached the maximum number of attempts.");
        return null;
    }

    private void sendFile(File file, float minsup, OutputStream os, DataOutputStream dos) {
        byte[] mybytearray = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             FileInputStream in = new FileInputStream(file))
        {
            bis.read(mybytearray, 0, mybytearray.length);

            dos.writeFloat(minsup);
            dos.writeUTF(file.getName());
            dos.writeLong(mybytearray.length);
            dos.flush();

            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
        } catch (IOException e) {
            System.out.println("Error occurred while sending the input file " + file.getName());
        }
    }

    private void receiveFile(DataInputStream clientData, String fileName) {
        int bytesRead;
        try {
            String resultFileName = "CLIENT_RESULT_" + fileName;
            long size = clientData.readLong();
            byte[] buffer = new byte[BUFFER_SIZE];
            try (OutputStream output = new FileOutputStream(resultFileName)) {
                while (size > 0
                        && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                    output.write(buffer, 0, bytesRead);
                    size -= bytesRead;
                }
            }
            System.out.println("\nThe result file is received: " + resultFileName);
        } catch (IOException e) {
            System.out.println("Error occurred while receiving the result file result_" + fileName);
        }
    }

    public static void main(String[] args) {
    /*    //For manual IDE testing
        try {
                Client c = new Client();
                c.connect("127.0.0.1", 5000);

        } catch (IOException e) {
                System.out.println("Error occurred.");
        }
    */

      if (args.length != 2) {
            System.out.println("Expected: <hostname> <port>");
            return;
      }

      try {
            Client c = new Client();

            String host = args[0];
            int port = Integer.parseInt(args[1]);
            c.connect(host, port);

      } catch (IOException e) {
            System.out.println("Error occurred.");
      }
    }
}
