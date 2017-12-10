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
    //TODO: Justifica-se colocar Player?

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

        Player player = client.connectUser(); //TODO: Justifica-se colocar Player?

        // Criar daemon thread que faz redireciona qualquer mensagem deste cliente para o seu input (listener)
        new ClientDaemon(client.socket).start();

        client.findMatch();

    }

    /** Faz o registo e/ou login de um utilizador ao sistema.
     *
     * @return Informação do jogador.
     */
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
                p = loginPlayer();
            }
            else if(tmp.equals("1")){
                p = loginPlayer();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return p;
    }

    /** Faz o registo de um jogador no sistema.
     *
     */
    private void registerPlayer() {
        String username = null , password, tmp;
        Boolean check = false, repeat = false;

        try {
            while (!check) {
                System.out.println("----Registe o jogador---");
                do { // while até o user ainda não ter sido inserido
                    //begin username
                    if (!repeat && username != null)
                        System.out.println("O user " + username + " já existe!");
                    repeat = false;
                    System.out.println("Username:");
                    username = scanner.readLine();
                    os.println(username);

                    //begin password
                    System.out.println("Password:");
                    password = scanner.readLine();
                    os.println(password);
                } while ((tmp = is.readLine()).equals("0"));

                if (tmp.equals("1")) {
                    do {
                        System.out.println("Confirma [y/n]");
                        tmp = scanner.readLine();
                        if (tmp.equals("y")) {
                            check = true;
                            os.println("1"); //transmite o 'yes'
                        } else if (tmp.equals("n")) {
                            os.println("0"); //transmite o 'no'
                            repeat = true;
                        }
                    } while (!tmp.equals("y") && !tmp.equals("n")); //repete o confirma no caso de não ter recebido 'y' nem 'n'
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Registado com sucesso!");
    }

    /** Faz o login dum cliente no sistema.
     *
     * @return Informação do cliente.
     */
    public Player loginPlayer(){
        String username, password, tmp = null, response = null;
        Boolean check = false;
        Player p = null;

        try {
            while(!check){
                System.out.println("---Login---");

                System.out.println("Username:");
                username = scanner.readLine();
                os.println(username);

                System.out.println("Password:");
                password = scanner.readLine();
                os.println(password);

                response = is.readLine();
                if(response.equals("0")){
                    System.out.println("O utilizador não existe!!");
                }
                else if(response.equals("-1")){
                    System.out.println("A password está errada!");
                }
                else if(response.equals("-2")){
                    System.out.println("O utilizador já se encontra online!");
                }
                else if(response.equals("1")){
                    System.out.println("Login sucedido!");
                    check = true;
                }

                p = new Player(username, password);
                p.goOnline();
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

}

