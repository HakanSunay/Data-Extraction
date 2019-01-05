package DataExtraction;

import ca.pfv.spmf.algorithms.frequentpatterns.lcm.AlgoLCMFreq;
import ca.pfv.spmf.algorithms.frequentpatterns.lcm.Dataset;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static java.lang.Integer.parseInt;

public class Server {
    private ServerSocket serverSocket;
    private boolean running;
    private InputStream in;
    private OutputStream out;
    private String fileName = "";
    private float minsup;
    private final static int BUFFER_SIZE = 1024;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void startServer(){
        running = true;
    }

    public void stopServer(){
        running = false;
    }

    public void listen() {
            try {
                while(running) {
                    Socket clientSocket = serverSocket.accept();
                    in = clientSocket.getInputStream();
                    out = clientSocket.getOutputStream();

                    new Thread(()->{
                        try (DataInputStream clientData = new DataInputStream(in);
                             DataOutputStream dos = new DataOutputStream(out)) {

                            receiveFile(clientData);
                            runLCMFreqAlgo(fileName, dos);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void runLCMFreqAlgo(String fileName,DataOutputStream dos) {
        HashMap<String, Integer> h = preparedHashForAlgo(fileName);

        try {
            Dataset dataset = new Dataset("TO_THE_ALGO_" + fileName);
            AlgoLCMFreq algo = new AlgoLCMFreq();
            algo.setMaximumPatternLength(7);
            algo.runAlgorithm(minsup, dataset, "TO_THE_MAP_" + fileName);
            algo.printStats();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Map everything back to String
        reverseMap(h,fileName);

        sendResultFile(out, dos,"READY_TO_BE_SENT_" + fileName);

        // Delete temporary files
        cleanServerJunk(fileName);

        System.out.println("--------SUCCESS----------");
    }

    private void cleanServerJunk(String fileName) {
        new File("READY_TO_BE_SENT_"+fileName).delete();
        new File("SERVER_"+fileName).delete();
        new File("TO_THE_ALGO_"+fileName).delete();
        new File("TO_THE_MAP_"+fileName).delete();
    }

    private void reverseMap(HashMap<String,Integer> h, String fileName) {
        FileInputStream algoReady = null;
        try {
            algoReady = new FileInputStream(new File("TO_THE_MAP_" + fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader newBr = new BufferedReader(new InputStreamReader(algoReady));
        String newLine;

        HashMap<Integer, String> integerStringHashMap = new HashMap<>();

        for (Map.Entry<String, Integer> res: h.entrySet()){
            integerStringHashMap.put(res.getValue(),res.getKey());
        }

        FileOutputStream newFo = null;
        try {
            newFo = new FileOutputStream(new File("READY_TO_BE_SENT_" + fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedWriter newBw = new BufferedWriter(new OutputStreamWriter(newFo));

        boolean scoreAhead = false;
        try {
            while ((newLine = newBr.readLine()) != null) {
                String[] holder = newLine.split(" ");
                for (String s : holder) {
                    if (scoreAhead) {
                        newBw.write("occurred " + s + " times");
                    } else if (s.equals("#SUP:")) {
                        scoreAhead = true;
                    } else {
                        newBw.write(integerStringHashMap.get(parseInt(s)) + " ");
                    }
                }
                scoreAhead = false;
                newBw.newLine();
            }

            newBw.flush();
            newBw.close();
            newFo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Integer> preparedHashForAlgo(String fileName) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File("SERVER_"+fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert fileInputStream != null;
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
        String line = null;
        HashMap<String, Integer> h = new HashMap<>();
        int counter = 1;
        ArrayList<String> xa = new ArrayList<>();
        try {
            line = br.readLine();
            line = "";
            while ((line = br.readLine())!=null) {
                String[] holder = line.split(",(?! )");
                ArrayList<String> arrayList = new ArrayList<>();
                try {
                    arrayList.add(holder[4]);
                    arrayList.add(holder[6]);
                } catch (ArrayIndexOutOfBoundsException ignored) {}

                for (String s : arrayList) {
                    if(!h.containsKey(s)){
                        h.put(s,counter);
                        counter++;
                    }
                }

                arrayList.sort((s, t1) -> {
                    if ((Integer)h.get(s) > (Integer)h.get(t1)){
                        return 1;
                    } else if ((Integer)h.get(s) < (Integer)h.get(t1)){
                        return -1;
                    }
                    return 0;
                });

                for(String s: arrayList) xa.add(s);
                xa.add("\\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(new File("TO_THE_ALGO_" + fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fo));
        for(String s: xa){
            if(s.equals("\\n")) {
                try {
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    bw.write(h.get(s).toString());
                    bw.write(" ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            bw.flush();
            bw.close();
            fo.close();
        } catch (IOException e) {e.printStackTrace();}
        return h;
    }

    private void receiveFile(DataInputStream clientData) {
        int bytesRead;
        try {
            minsup = clientData.readFloat();
            fileName = clientData.readUTF();
            long size = clientData.readLong();
            byte[] buffer = new byte[BUFFER_SIZE];
            try (OutputStream output = new FileOutputStream("SERVER_" + fileName)) {
                while (size > 0
                        && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                    output.write(buffer, 0, bytesRead);
                    size -= bytesRead;
                }
            }
            System.out.println("The server received file " + fileName + " for processing.");
        } catch (IOException e) {
            System.out.println("Error occurred while receiving the file " + fileName);
        }
    }

    private void sendResultFile(OutputStream os, DataOutputStream dos, String resultFileName) {
        System.out.println("Sending result file READY_TO_BE_SENT_" + fileName);
        File file = new File(resultFileName);
        byte[] mybytearray = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            bis.read(mybytearray, 0, mybytearray.length);

            dos.writeLong(mybytearray.length);
            dos.flush();

            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
        } catch (IOException e) {
            System.out.println("Error occurred while sending the result file READY_TO_BE_SENT_" + fileName);
        }
    }

    public static void main(String[] args){
        if (args.length != 1) {
            System.out.println("Port is expected as an input.");
            return;
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (InputMismatchException e) {
            System.out.println("The port must be positive integer.");
        }

        Server myServer = null;
        try {
            myServer = new Server(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert myServer != null;
        myServer.startServer();
        myServer.listen();
        myServer.stopServer();
    }
}
