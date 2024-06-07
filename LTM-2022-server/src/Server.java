import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static ServerSocket server = null;
    private static Socket socket = null;
    public static Thread searchFilmThread = null;
    public static Thread imageProcessingThread = null;

    public void start() {
        try {
            server = new ServerSocket(3000);
            System.out.println("Server is listening on port 3000...");
            while (!server.isClosed()) {
                socket = server.accept();
                System.out.println("Client " + socket.getInetAddress() + " connected...");
                SearchFilmHandler searchFilmClient = new SearchFilmHandler(socket);
                searchFilmThread = new Thread(searchFilmClient);
                searchFilmThread.start();
            }
        } catch (Exception error) {
            closeConnect();
        }
    }

    public void closeConnect() {
        try {
            if (socket != null)
                socket.close();
            if (server != null)
                server.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
