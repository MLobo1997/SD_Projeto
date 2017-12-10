import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    // Connection info
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

    public static void main(String args[]){
        Client client = new Client();

        client.connectUser();

        // Criar daemon thread que faz redireciona qualquer mensagem deste cliente para o seu input (listener)
        new ClientDaemon(client.socket).start();

        client.findMatch();

    }

    private Player connectUser(){
        Player p = null;
        String tmp;

        try {
            do {
                System.out.println("Register [0] or login [1]?");
                tmp = scanner.readLine();
            } while (!tmp.equals("0") && !tmp.equals("1"));
            os.println(tmp);

            if(tmp.equals("0")){
                registerPlayer();

                //TODO:login
            }
            else if(tmp.equals("1")){
                //TODO:login
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return p;
    }
    /**
     * Ler informação do cliente necessária para inicializar jogo
     */
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

    private void registerPlayer() {
        String username = null , password = null, tmp = null;
        Boolean check = false;

        try {
            while (!check) {
                do {
                    if (username != null)
                        System.out.println("O user " + username + " já existe!");
                    System.out.println("Username:");
                    username = scanner.readLine();
                    os.println(username);

                } while ((tmp = is.readLine()).equals("0"));

                if (tmp.equals("1")) {

                    System.out.println("Password:");
                    password = scanner.readLine();
                    os.println(password);

                    System.out.println("Are you sure? [y/n]");
                    tmp = scanner.readLine();
                    if(tmp.equals("y")) {
                        check = true;
                        os.println("1");
                    }

                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Registado com sucesso!");
    }
}

