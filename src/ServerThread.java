import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**Classe de threads geradas pelo servidor dedicadas a tratar de cada jogador individualmente
 *
 */
public class ServerThread extends Thread implements Comparable {
    /**  ----- Connection info ----- */
    /** Socket que liga cliente a ServerThread */
    private Socket socket;
    /** Input stream retirado da socket */
    private BufferedReader in;
    /** Output stream retirado da socket */
    private PrintWriter out;

    /**  ----- Game info  ----- */
    /**  ----- Player ----- */
    /** Registo de todos os jogadores - recurso partilhado do servidor principal */
    private PlayersRegister allPlayers;
    /** Jogador atual que a serverThread está a servir , null caso nenhum */
    private Player player;
    /** Nome do jogador em formato de prefixo chat: username -> [username]: */
    private String wrappedUsername;
    /**  ----- Match ----- */
    /** Jogo atual ao qual jogador está alocado , null caso em nenhum */
    private Match currentMatch;
    /** Serviço de matchmaking, encarregue de bloquear threads até um jogo nas condições certas ser encontrado */
    private Barrier matchmaker;

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
    public void cleanup() {
        try {
            in.close();
            out.close();
            socket.close();
            if (player != null) {
                player.goOffline();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Usar outputstream da serverthread por terceiros para imprimir algo
     * @param line linha a imprimir
     */
    public void printToOutput(String line) {
        out.println(line);
    }

    /**
     * Associar um jogo à thread que serve um cliente
     * @param m jogo a associar
     */
    public void associateMatch(Match m) {
        currentMatch = m;
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
                do {
                    if(username != null)
                        out.println("0");
                    username = in.readLine();
                    System.out.println(username);
                } while (allPlayers.playerExists(username));
                out.println("1");

                password = in.readLine();
                isRegistered = in.readLine().equals("1");
            }

            allPlayers.addPlayer(new Player(allPlayers.size() + 1,username,password));


        } catch (IOException e) {
            System.out.println("Client left");
        }
    }

    /**
     * Recebe input do utilizador e verifica se está na base de dados
     */
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
                Player foundPlayer = allPlayers.getPlayer(username);
                // Garantir que jogador existe e, caso exista, que não tem outro cliente a usa-lo atualmente
                if ( (foundPlayer != null) && (!foundPlayer.isOnline()) ) {
                    isLogged = true;
                } else {
                    out.println("Login failed (account doesn't exist or already being used)");
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        // Login funcionou: atualizar a thread para ter agora referencia ao jogador
        player = allPlayers.getPlayer(username);
        player.goOnline();
        // Atualizar nome da thread para servir de identificador de chat
        wrappedUsername = "[" + player.getUsername() + "]: ";
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
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void commandMode() {
        String str = null;

        while (true) {
            try {
                str = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (str.equals("play")) {
                break;
            }
        }
    }

    /**
     * Protocolo de especificação de intenções do cliente pré-jogo (registar ou fazer login)
     * @throws IOException No caso de não conseguir ler do buffer do cliente.
     */
    public void connectUser() throws IOException {
        boolean canPlay = false;
        String str;
        // TODO: Deixar a qualquer momento alternar entre modos (com keywords reservadas como por exemplo <REGISTER>

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
    }
    /**
     * Enviar mensagem para toda a gente no jogo em que o jogador que a thread serve está (implementação de chat)
     * @param line
     */
    public void echoMessage(String line) {
        for (ServerThread st : currentMatch.getPlayers()) {
            st.printToOutput(line);
        }
    }


    public void initGame() {

        try {
            // Mensagem de input
            String str = in.readLine();

            // Timestamp de quando a mensagem foi enviada
            String timestamp;
            while(!str.equals("quit")) {
                timestamp = (new SimpleDateFormat("HH:mm:ss").format(new Date())) + " ";
                echoMessage(timestamp + wrappedUsername + str);
                str = in.readLine();
            }
        } catch (IOException |NullPointerException e) {
            cleanup();
        }
    }

    public void run() {

        // Protocolo: primeira mensagem: modo (registar(0) ou login(1))
        try {
            // Deixar cliente fazer login / registo
            connectUser();

            // Fornecer possibilidades de menu principal (jogar, ver estatisticas( TODO ))
            commandMode();

            // Procurar jogo e ficar inoperável até encontrar
            matchmaker.waitGame(this);

            // Iniciar protocolo de jogo, isto acontece porque:
            // Possuimos um Player
            // Possuimos um Match
            initGame();

            cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(Object o) {
        int difference = (int) (player.getRanking() - ((ServerThread) o).player.getRanking());
        if (difference == 0) {
            return 1; // Para permitir chaves iguais
        } else {
            return difference;
        }
    }
}
