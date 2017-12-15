import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/** Classe utilizada para a thread de Cliente que funciona como um listener dos dados que lhe são enviados
 *
 */
public class ClientDaemon extends Thread {
    private BufferedReader is = null;
    private Socket socket = null;

    public ClientDaemon(Socket s) {
        this.socket = s;

        try {
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public  void run() {
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
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e2) {
            System.out.println("Unable to reach server (possible shutdown).");
        }
    }
}

