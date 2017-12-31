package Service_Threads;

import Game_Information.MessageBuffer;
import Game_Information.lobbyBarrier;
import Game_Information.Player;
import Game_Information.PlayerAggregator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * Classe de threads geradas pelo servidor dedicadas a tratar de cada jogador individualmente
 */
public class ServerThread implements Comparable, Runnable, Observer {
    /*  ------ Connection info ----- */
    /** Socket que liga cliente a Service_Threads.ServerThread */
    private Socket socket;
    /** Input stream retirado da socket */
    private BufferedReader in;
    /** Output stream retirado da socket */
    private PrintWriter out;

    /*  ----- Game info  ----- */
    /*  ----- Game_Information.Player ----- */
    /** Registo de todos os jogadores - recurso partilhado do servidor principal */
    private PlayerAggregator allPlayers;
    /** Jogador atual que a serverThread está a servir , null caso nenhum */
    private Player player;
    /** Nome do jogador em formato de prefixo chat: username -> [username]: */
    private String wrappedUsername;
    /*  ----- Service_Threads.Match ----- */
    /** Jogo atual ao qual jogador está alocado , null caso em nenhum */
    private Match currentMatch;
    /** Serviço de matchmaking, encarregue de bloquear threads até um jogo nas condições certas ser encontrado */
    private lobbyBarrier matchmaker;
    /** Lista de mensagens que têm de ser enviadas para o cliente */
    private MessageBuffer messageBuffer;

    /** Construtor de server thread, em que estabelece já os buffers de comunicação.
     *
     * @param s Socket.
     * @param pl Registo de todos os jogadores.
     * @param mmaker Barreira.
     */
    public ServerThread (Socket s, PlayerAggregator pl, lobbyBarrier mmaker) {
        player        = null; // Updated quando login for feito
        socket        = s;
        allPlayers    = pl;
        matchmaker    = mmaker;
        messageBuffer = new MessageBuffer();
        messageBuffer.addObserver(this); // Quando houver uma mensagem nova, avisa-me para enviar ao cliente

        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retornar jogador associado da thread
     * @return informação do jogador cujo cliente a Service_Threads.ServerThread está a servir
     */
    public Player getPlayer() {
        return player;
    }

    public MessageBuffer getMessageBuffer() {
        return messageBuffer;
    }

    /**
     * Fechar todos os canais de comunicação
     */
    public void cleanup() {
        try {
            if (player != null) {
                System.out.println(player.getUsername() + " a ser desconectado");
            } else {
                System.out.println("Cliente desconectado");
            }

            in.close();
            out.close();
            socket.close();
            if (player != null) {
                player.goOffline();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        allPlayers.savePlayersInfo();
        Thread.currentThread().stop();
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

        } catch (NullPointerException | IOException e) {
            System.out.println("Client left");
            cleanup();
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
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            cleanup();
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

    /**
     * Método chamado após o login do jogador com o ituito de comunicar  com o mesmo e responder às suas decisões (como jogar, sair, etc.)
     */
    private void commandMode() {
        String cmd = null;

        try {

            do {
                cmd = in.readLine();

                if (cmd.equals("1")){ //O jogador que jogar
                    // Procurar jogo e ficar inoperável até encontrar
                    matchmaker.waitGame(this);

                    // Iniciar protocolo de jogo, isto acontece porque:
                    // Possuimos um Game_Information.Player
                    // Possuimos um Service_Threads.Match
                    initGame();
                    allPlayers.savePlayersInfo(); //Atualiza os xp's em memória

                }
            } while (!cmd.equals("0")); // cmd = 0 quando quer fazer logout
            cleanup();

        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            cleanup();
        }
    }

    /**
     * Protocolo de especificação de intenções do cliente pré-jogo (registar ou fazer login)
     */
    public void connectUser() {
        boolean toReg = true; //true se for para fazer registo false se for para fazer login
        boolean loggedIn = false;
        boolean canPlay = false;
        String str = null;

        while (!loggedIn) {
            try {
                str = in.readLine();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                cleanup();
            }

            if (str == null) {
                cleanup();
            }

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
                    loggedIn = loginPlayer();
                    if(!loggedIn){
                        toReg = true;
                    }
                }
            }
        }
    }

    /**
     * Enviar mensagem para todos as threads a servir o cliente. Não é enviado diretamente para o cliente, mas para
     * o buffer da thread. Esta estará encarregue de esvaziar o buffer e enviar as mensagens para o cliente.
     *
     * @param line Mensagem a ser enviada.
     */
    public void echoMessage(String line) {
        for (ServerThread st : currentMatch.getMatchInfo().getPlayers()) {
            st.getMessageBuffer().publishMessage(line);
        }
    }

    /**
     * Antes de inicializar o protocolo de jogo, é necessário esperar primeiro que as restantes 9 threads
     * estejam prontas a jogar, este método bloqueia a thread até isso acontecer.
     */
    public void signalReady() {

        int playerNum = currentMatch.getMatchInfo().getPlayerNum();

        currentMatch.getMatchLock().lock();

        try {
            currentMatch.incrementPlayersReady();

            if (currentMatch.getThreadsAwoken() == playerNum) {
                currentMatch.getAllPlayersReadyCondition().signal();
            }
        }
        finally {
            currentMatch.getMatchLock().unlock();
        }

    }

    /**
     * Cria timestamp da altura em que o método foi chamado
     * @return timestamp a retornar
     */
    public String generateTimeStamp() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    /**
     * Inicializar o protocolo de comunicação para jogo. Neste método, todas as linhas lidas do jogador
     * são interpretadas como mensagens de chat ou comandos de jogo
     */
    private void initGame() {

        signalReady();

        try {
            // Mensagem de input
            String str = in.readLine();

            while((str != null) && !str.equals("&GAMEOVER 1&") && !str.equals("&GAMEOVER 0&")) {
                if(str.matches("&CHOOSE [12]?[0-9]&")){
                    int hero = Integer.parseInt(str.replaceAll("[\\D]", ""));
                    boolean res = currentMatch.chooseHero(this, hero);
                    if(res == true) {
                        String strclone = str;
                        String heroNum = strclone.substring(0,str.length()-1).split(" ")[1];
                        echoMessage(generateTimeStamp() + " Jogador " + player.getUsername() + " escolheu heroi nr " + heroNum);
                    }
                    else out.println("O Herói já foi selecionado! Tente outro!");
                }
                else {
                    echoMessage(generateTimeStamp() + " " + wrappedUsername + str);
                }

                str = in.readLine();
            }

        } catch (IOException | NullPointerException e) {
            cleanup();
        }
    }

    public void run() {
        // Protocolo: primeira mensagem: modo (registar(0) ou login(1))

        // Deixar cliente fazer login / registo
        connectUser();

        // Fornecer possibilidades de menu principal (jogar, ver estatisticas( TODO ))
        commandMode();

        cleanup();
    }

    @Override
    public int compareTo(Object o) {
        int difference = (int) (player.getRank() - ((ServerThread) o).player.getRank());
        if (difference == 0) {
            return 1; // Para permitir chaves iguais
        } else {
            return difference;
        }
    }

    @Override
    public boolean equals(Object o){
        return o == this;
    }

    /**
     * Quando for notificado, sabe que há mensagens novas no messageBuffer para enviar para o cliente.
     */
    @Override
    public void update(Observable o, Object arg) {

        String line;

        while((line = messageBuffer.getMessage()) != null) {
            out.println(line);
        }

    }
}
