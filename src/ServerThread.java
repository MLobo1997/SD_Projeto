import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**Classe de threads geradas pelo servidor dedicadas a tratar de cada jogador individualmente
 *
 */
public class ServerThread extends Thread {
    // Connection info
    /** Buffer de leitura de comunições do cliente*/
    private BufferedReader in;
    /** Buffer de escrita de comunições para o cliente*/
    private PrintWriter out;
    /** Socket estabelecido entre o servidor e o cliente */
    private Socket socket;

    // Game info
    /** Barreira dinâmica que aloca jogadores e faz correspondentes threads esperar até match ser encontrado */
    private Barrier matchmaker;
    /** Registo de todas as contas. */
    private PlayersRegister allPlayers;
    /** Jogador ao qual esta thread está dedicada*/
    private Player player;

    /** Construtor de server thread, em que estabelece já os buffers de comunicação.
     *
     * @param s Socket.
     * @param pl Registo de todos os jogadores.
     * @param mmaker Barreira.
     */
    public ServerThread (Socket s,PlayersRegister pl,Barrier mmaker) {
        player     = null; // Updated quando login for feito
        socket     = s;
        allPlayers = pl;
        matchmaker = mmaker;
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retornar jogador associado da thread
     * @return informação do jogador cujo cliente a ServerThread está a servir
     */
    public Player getPlayer() {
       return player;
    }

    /**
     * Fechar todos os canais de comunicação
     */
    public void cleanup () {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recebe input do utilizador e regista na base de dados
     */
    public void registerPlayer() {
        try {
            // Protocolo: primeira mensagem: username, segunda mensagem: password, terceira mensagem: confirmação(0 ou 1)
            String username = null;
            String password = null;
            boolean isRegistered = false;

            while (!isRegistered) {
                out.println("Username:");
                username = in.readLine();
                out.println("Password:");
                password = in.readLine();
                out.println("Are you sure? [1]:");
                isRegistered = in.readLine().equals("1");

                out.println("Register worked? " + isRegistered);
            }

            allPlayers.addPlayer(new Player(allPlayers.size() + 1,username,password));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recebe input do utilizador e verifica se está na base de dados
     */
    // TODO: Nao aceitar um jogador logged in se já estiver (adicionar booleano algures)
    public void loginPlayer() {
        // Protocolo: primeira mensagem: username, segunda mensagem: password. Repetir até válido
            String username  = null;
            String password  = null;
            boolean isLogged = false;

        try {
            while (!isLogged) {
                out.println("Username:");
                username = in.readLine();
                out.println("Password:");
                password = in.readLine();
                if (allPlayers.playerExists(username,password)) { //TODO:não permitir logins atualmente online
                    isLogged = true;
                }
                out.println("Login worked? " + isLogged);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Login funcionou: atualizar a thread para ter agora referencia ao jogador
        player = allPlayers.getPlayer(username,password);
    }

    /**
     * Função de teste de feedback
     */
    public void echoLoop() {
        String str;

        try {
            while(!(str = in.readLine()).equals("quit")) {
                out.println(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("Client left, shutting down its thread..");
            cleanup();
        }
    }

    /** Faz o login ou registo do cliente.
     *
     * @throws IOException No caso de não conseguir ler do buffer do cliente.
     */
    public void connectUser() throws IOException{
        boolean canPlay = false;
        String str;

        while (!canPlay) {
            out.println("Register [0] or login [1]?");
            str = in.readLine();

            switch (str) {
                case "0":
                    registerPlayer();
                    loginPlayer();
                    canPlay = true;
                    break;
                case "1":
                    loginPlayer();
                    canPlay = true;
                    break;
                default:
                    break;
            }
        }
    }

    public void run(){

        // Protocolo: primeira mensagem: modo (registar(0) ou login(1))
        try {
            connectUser();
            // look for match
            matchmaker.waitGame(this);

            // TODO: Jogo começa aqui

            // test
            out.println("Did it work boy?");

            cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
