package client;

import common.StatusCode;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 34522;
    private static final String DATA_FOLDER = System.getProperty("user.dir") + "/File Server/task/src/client/data/";
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        createDataFolderIfNotExists();

        System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): ");
        String code = scanner.nextLine();
        Method method = Method.getMethodByClientCode(code);
        if (method == null) {
            System.out.println("Unsupported action");
        } else {
            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                switch (method) {
                    case EXIT -> {
                        output.writeUTF("exit");
                        System.out.println("The request was sent.");
                    }
                    case GET -> handleGet(input, output);
                    case PUT -> handlePut(input, output);
                    case DELETE -> handleDelete(input, output);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleGet(DataInputStream input, DataOutputStream output) throws IOException {
        String receivedMsg = handleGetAndDelete("get", input, output);
        if (receivedMsg.equals(StatusCode.NOT_FOUND.toString())) {
            System.out.println("The response says that this file is not found!");
        } else if (receivedMsg.substring(0, 3).equals(StatusCode.OK.toString())) {
            System.out.println("The file was downloaded! Specify a name for it: ");
            String newName = scanner.nextLine();
            int length = input.readInt();
            byte[] data = new byte[length];
            input.readFully(data, 0, length);
            File file = new File(DATA_FOLDER + newName);
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(data);
                System.out.println("File saved on the hard drive!");
            } catch (IOException e) {
                System.out.println("Error! File wasn't saved on the hard drive!");
            }
        }
    }

    private static void handlePut(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.println("Enter name of the file: ");
        String fileName = scanner.nextLine();
        System.out.println("Enter name of the file to be saved on server: ");
        String newName = scanner.nextLine();

        File file = new File(DATA_FOLDER + fileName);
        if (file.exists()) {
            byte[] data = Files.readAllBytes(Paths.get(file.toURI()));
            output.writeUTF("put");
            output.writeUTF(newName);
            output.writeInt(data.length); // write length of the message
            output.write(data);
            System.out.println("The request was sent.");
            String receivedMsg = input.readUTF();
            if (receivedMsg.equals(StatusCode.FORBIDDEN.toString())) {
                System.out.println("The response says that saving the file was forbidden!");
            } else if (receivedMsg.substring(0, 3).equals(StatusCode.OK.toString())) {
                System.out.println("Response says that file is saved! ID = " + receivedMsg.substring(4));
            }
        } else {
            System.out.println("File not found!");
        }
    }

    private static void handleDelete(DataInputStream input, DataOutputStream output) throws IOException {
        String receivedMsg = handleGetAndDelete("delete", input, output);
        if (receivedMsg.equals(StatusCode.NOT_FOUND.toString())) {
            System.out.println("The response says that this file is not found!");
        } else if (receivedMsg.equals(StatusCode.OK.toString())) {
            System.out.println("The response says that this file was deleted successfully!");
        }
    }

    private static String handleGetAndDelete(String operation, DataInputStream input, DataOutputStream output) throws IOException {
        System.out.println("Do you want to " + operation + " the file by name or by id (1 - name, 2 - id): ");
        String mode = scanner.nextLine();
        if (mode.equals("2")) {
            System.out.println("Enter id: ");
            mode = "BY_ID";
        } else {
            System.out.println("Enter name of the file: ");
            mode = "BY_NAME";
        }
        String param = scanner.nextLine();
        output.writeUTF(operation);
        output.writeUTF(mode);
        output.writeUTF(param);
        System.out.println("The request was sent.");
        return input.readUTF();
    }

    private static void createDataFolderIfNotExists() {
        try {
            Files.createDirectories(Paths.get(DATA_FOLDER));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}