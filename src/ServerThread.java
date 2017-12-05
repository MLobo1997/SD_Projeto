import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;

    public ServerThread (Socket s) {
        socket=s;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out= new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void cleanup () throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    public void run(){
        String str;
        try {
            while ((str = in.readLine()) != null) {
               out.println("Echo: " + str);
            }

            cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
