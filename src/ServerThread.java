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
            System.out.println(player.getUsername() + " a ser desconectado");

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
    private void registerPlayer() {
        try {
            // Protocolo: primeira mensagem: username, segunda mensagem: password, terceira mensagem: confirmação(y ou n)
            String username = null;
            String password = null;
            boolean isRegistered = false;
            boolean repeat = true;
            boolean skip = false;


            while (repeat && !skip) {
                do {

                    username = in.readLine();
                    password = in.readLine();

                    isRegistered = allPlayers.playerExists(username);
                    if(isRegistered){
                        out.println("0"); //diz que o jogador já existia
                        skip = in.readLine().equals("-1"); //verifica se o cliente comunicou que é para avançar para login
                    }
                    else {
                        out.println("1"); //avisa ainda não existia.
                    }
                } while (isRegistered && !skip);

                if(!skip) { //Se saiu do ciclo sem ser por ocorrerem erros
                    repeat = in.readLine().equals("0"); //lê se o utilizador quer tentar outra vez o registo ou não
                }

                if(!repeat) {
                    allPlayers.addPlayer(new Player(username, password));
                    System.out.println("Foi registado o utilizador " + username);
                }
                else if (!skip) {
                    skip = in.readLine().equals("-1");
                }
            }

        } catch (IOException e) {
            System.out.println("Client left");
        }
    }

    /**
     * Recebe input do utilizador e verifica se está na base de dados
     */
    private boolean loginPlayer() {
        // Protocolo: primeira mensagem: username, segunda mensagem: password. Repetir até válido. Erro 0 se não existir, -1 se a passe estiver errada, -2 se já estiver online
        String username  = null;
        String password  = null;
        boolean skip = false;
        Player p = null;
        boolean check = false;

        try {
            while(!check && !skip) {
                username = in.readLine();
                password = in.readLine();

                p = allPlayers.getPlayer(username);

                if(p == null){ //se o jogador não existe, enviar erro 0
                    out.println("0");
                }
                else if(!p.passwordEquals(password)){ //se a pass estiver errada, envia erro -1
                    out.println("-1");
                }
                else if(p.isOnline()){ //se o utilizador ja estiver online, envia erro -2
                    out.println("-2");
                }
                else {
                    out.println("1");
                    check = true;
                }

                if(!check){
                    skip = in.readLine().equals("-1");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(check) {
            // Login funcionou: atualizar a thread para ter agora referencia ao jogador
            player = p;
            player.goOnline();
            System.out.println("O user " + username + " está agora online!");
            // Atualizar nome da thread para servir de identificador de chat
            wrappedUsername = "[" + player.getUsername() + "]: ";
        }

        return check;
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

    /** Método chamado após o login do jogador com o ituito de comunicar  com o mesmo e responder às suas decisões (como jogar, sair, etc.)
     *
     */
    public void commandMode() {
        String cmd = null;

        try {

            do {
                cmd = in.readLine();

                if (cmd.equals("1")){
                    System.out.println(player.getUsername() + " wants to play a game");
                    // Procurar jogo e ficar inoperável até encontrar
                    matchmaker.waitGame(this);

                    // Iniciar protocolo de jogo, isto acontece porque:
                    // Possuimos um Player
                    // Possuimos um Match
                    initGame();
                }
            } while (!cmd.equals("0"));
            cleanup();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Protocolo de especificação de intenções do cliente pré-jogo (registar ou fazer login)
     * @throws IOException No caso de não conseguir ler do buffer do cliente.
     */
    public void connectUser() throws IOException {
        boolean toReg = true; //true se for para fazer registo false se for para fazer login
        boolean loggedIn = false;
        boolean canPlay = false;
        String str;
        // TODO: Deixar a qualquer momento alternar entre modos (com keywords reservadas como por exemplo <REGISTER>

        while (!loggedIn) {
            str = in.readLine();

            switch (str) {
                case "0":
                    toReg = true;
                    break;
                case "1":
                    toReg = false;
                    break;
            }

            while (!loggedIn) {
                if (toReg) {
                    registerPlayer();
                    loggedIn = loginPlayer();
                } else {
                    loggedIn = loginPlayer(); //TODO fazer o user poder voltar para register em caso de engano
                    if(!loggedIn){
                        toReg = true;
                    }
                }
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
