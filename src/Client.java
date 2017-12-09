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
                System.out.println("TA FETCHO");

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

    public Player login(){
        String username = null , password = null, tmp = null, response = null;
        Boolean check = false;
        Player p = null;

        try {
            while(!check){
                System.out.println("---Login---");
                do {
                    //begin de processo de username
                    if ("0".equals(response)) //tem que ser assim pq se o this for response dará nullPointerException
                        System.out.println("O jogador não existe");
                    else if ("-1".equals(response))
                        System.out.println("O jogador já está online");
                    System.out.println("Username:");
                    username = scanner.readLine();
                    os.println(username);
                    response = is.readLine();
                    //end de processo de username
                } while(response.equals("0") || response.equals("-1"));

                if (response.equals("1")){
                    check = true;
                }
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

