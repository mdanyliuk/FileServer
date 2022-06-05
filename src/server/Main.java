package server;

public class Main {

    private static final int PORT = 34522;
    private static final String DATA_FOLDER = System.getProperty("user.dir") + "/File Server/task/src/server/data/";
    private static final String UTIL_FOLDER = System.getProperty("user.dir") + "/File Server/task/src/server/util/";

    public static void main(String[] args) {
        Server server = new Server(PORT, DATA_FOLDER, UTIL_FOLDER);
        server.launch();
    }
}