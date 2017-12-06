import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket          = null;
    private BufferedReader scanner = null;
    private BufferedReader is      = null;
    private PrintWriter os         = null;

    public Client() {
        try {
            scanner = new BufferedReader(new InputStreamReader(System.in));
            socket  = new Socket("127.0.0.1",9999);
            is      = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os      = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findMatch() {
        try {
            String lineInput = scanner.readLine();
            while (!lineInput.equals("quit")) {
                os.println(lineInput);
                lineInput = scanner.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while communicating with server");
        }
        finally {
            try {
                is.close();
                os.close();
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error closing connections");
            }
        }
    }

    public static void main(String args[]){
        Client client = new Client();

        // Criar daemon thread que faz redireciona qualquer mensagem deste cliente para o seu input
        new ClientDaemon(client.socket).start();

        client.findMatch();

    }

}

