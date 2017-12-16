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
                p = loginPlayer(); //TODO fazer o user poder voltar para register em caso de engano
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

    /** Desconecta o utilizador localmente.
     *
     */
    public void disconnectUser (){
        System.out.println("Desligando do sistema.");

        try {
            is.close();
            os.close();
            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error closing connections");
        }
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

    /** Inicia o menu do jogo após o login e permite ao jogador escolher o que quer fazer a seguir (e.g.: jogar, sair do programa, outras cenas que podemos inventar)
     *
     * @return Valor que identifica a opção do jogador (-1 -> ocorreu um erro ; 0 -> desistir; 1 -> jogar)
     */
    public int startMenu(){
        String cmd;

        try {

            do {
                System.out.println("O que vosse mecê deseja fazer?");
                System.out.println("Jogar [p], sair [q]?:");

                cmd = scanner.readLine();

                if (cmd.equals("q")){
                    os.println("0");
                    return 0;
                }
                else if (cmd.equals("p")){
                    os.println("1");
                    return 1;
                }

            } while (!cmd.equals("p") && !cmd.equals("q")); // se o valor inserido não for válido, repete o processo

        } catch (IOException e) {
            e.printStackTrace();
        }

        os.println("-1"); //Avisa o servidor do erro.
        return -1; //Se chegar aqui é porque algo correu mal.
    }

    /** Método que faz o processamento após o login.
     *
     */
    public void init(){
        Thread t = new ClientDaemon(socket);
        int cmd;
        do {
            cmd = startMenu();

            if (cmd == 1){
                // Criar daemon thread que faz redireciona qualquer mensagem deste cliente para o seu input (listener)
                t.start();
                findMatch();
                t.interrupt();
            }
        } while (cmd != 0);
        disconnectUser();
    }

    public static void main(String args[]){
        int cmd;

        Client client = new Client();

        Player player = client.connectUser();

        client.init();
    }
}
