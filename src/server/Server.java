package server;

import common.SearchMode;
import common.StatusCode;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final String dataFolder;
    private final String utilFolder;
    private IdMap idMap;
    private final String idMapPath;
    private ExecutorService executor;
    private ServerSocket serverSocket;

    public Server(int port, String dataFolder, String utilFolder) {
        this.port = port;
        this.dataFolder = dataFolder;
        this.utilFolder = utilFolder;
        idMapPath = utilFolder + "idmap";
        try {
            this.idMap = (IdMap) SerializationUtils.deserialize(idMapPath);
        } catch (IOException | ClassNotFoundException e) {
            this.idMap = new IdMap();
        }
        executor = Executors.newFixedThreadPool(4);
    }

    public void launch() {
        System.out.println("Server started!");
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                executor.submit(() -> requestHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestHandler(Socket socket) {
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            String method = input.readUTF();
            switch (method) {
                case "exit" :
                    stopExecute();
                    break;
                case "get" :
                    String str = input.readUTF();
                    SearchMode searchMode = str.equals("BY_ID") ? SearchMode.BY_ID : SearchMode.BY_NAME;
                    str = input.readUTF();
                    doGet(searchMode, str, output);
                    break;
                case "put" :
                    String nameOnServer = input.readUTF();
                    int length = input.readInt();
                    doPut(nameOnServer, length, input, output);
                    break;
                case "delete" :
                    str = input.readUTF();
                    searchMode = str.equals("BY_ID") ? SearchMode.BY_ID : SearchMode.BY_NAME;
                    str = input.readUTF();
                    doDelete(searchMode, str, output);
                    break;
                default:
                    System.out.println("Unknown method");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doPut(String fileName, int length, DataInputStream input, DataOutputStream output) throws IOException {
        if (fileName.isEmpty()) {
            fileName = generateRandomString();
        }
        byte[] data = new byte[length];
        input.readFully(data, 0, length);
        File file = new File(dataFolder + fileName);
        if (file.exists()) {
            output.writeUTF(StatusCode.FORBIDDEN.toString());
        } else {
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(data);
                output.writeUTF(StatusCode.OK.toString() + " " + idMap.addName(fileName));
            } catch (IOException e) {
                output.writeUTF(StatusCode.FORBIDDEN.toString());
            }
        }
    }

    private void doGet(SearchMode searchMode, String param, DataOutputStream output) throws IOException {
        String fileName = param;
        if (searchMode == SearchMode.BY_ID) {
            int id = Integer.parseInt(param);
            fileName = idMap.getName(id);
        }
        File file = new File(dataFolder + fileName);
        if (file.exists()) {
            output.writeUTF(StatusCode.OK.toString());
            byte[] data = Files.readAllBytes(Paths.get(file.toURI()));
            output.writeInt(data.length);
            output.write(data);
        } else {
            output.writeUTF(StatusCode.NOT_FOUND.toString());
        }
    }

    private void doDelete(SearchMode searchMode, String param, DataOutputStream output) throws IOException {
        String fileName = param;
        if (searchMode == SearchMode.BY_ID) {
            int id = Integer.parseInt(param);
            fileName = idMap.getName(id);
            idMap.deleteId(id);
        } else {
            idMap.deleteName(fileName);
        }
        File file = new File(dataFolder + fileName);
        if (file.exists()) {
            file.delete();
            output.writeUTF(StatusCode.OK.toString());
        } else {
            output.writeUTF(StatusCode.NOT_FOUND.toString());
        }
    }

    void stopExecute() {

        executor.shutdownNow();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            SerializationUtils.serialize(idMap, idMapPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.exit(0);
    }

    private String generateRandomString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
