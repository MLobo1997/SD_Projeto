import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {
    // Connection info
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;

    // Game info
    private Barrier matchmaker;
    private PlayersRegister allPlayers;
    private Player player;

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

    public Player getPlayer() {
       return player;
    }

    /** Fechar todos os canais de comunicação */
    public void cleanup () {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Recebe input do utilizador e regista na base de dados */
    public void registerPlayer() {
        try {
            // Protocolo: primeira mensagem: username, segunda mensagem: password, terceira mensagem: confirmação(0 ou 1)
            String username = null;
            String password = null;
            boolean isRegistered = false;

            while (!isRegistered) {
                username = in.readLine();
                password = in.readLine();
                isRegistered = in.readLine().equals("1");

                out.println("Register worked? " + isRegistered);
            }

            allPlayers.addPlayer(new Player(allPlayers.size() + 1,username,password));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Recebe input do utilizador e verifica se está na base de dados */
    public void loginPlayer() {
        // Protocolo: primeira mensagem: username, segunda mensagem: password. Repetir até válido
            String username  = null;
            String password  = null;
            boolean isLogged = false;

        try {
            while (!isLogged) {
                username = in.readLine();
                password = in.readLine();
                if (allPlayers.playerExists(username,password)) {
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

    /** Função de teste de feedback */
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

    public void run(){

        // Protocolo: primeira mensagem: modo (registar(0) ou login(1))
        try {
            boolean canPlay = false;
            String str;

            while (!canPlay) {
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

            // look for match
            matchmaker.waitGame(this);

            // test
            out.println("Did it work boy?");

            cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
