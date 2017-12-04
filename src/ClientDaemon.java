import java.io.*;
import java.net.Socket;

public class ClientDaemon extends Thread {
    private BufferedReader is = null;
    private Socket socket = null;

    public ClientDaemon(Socket s) {
        this.socket = s;
    }

    @Override
    public  void run() {

        try {
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String line;
        try {
            do {
                line = is.readLine();
                if (line == null) {
                    System.out.println("Unable to reach server (possible shutdown).");
                    this.socket.close();
                    this.is.close();
                    break;
                } else {
                    System.out.println(line);
                }
            } while(true);
        }
        catch (IOException e) {}
        catch (NullPointerException e2) {
            System.out.println("Unable to reach server (possible shutdown).");
        }
    }
}

