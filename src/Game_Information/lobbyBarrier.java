package Game_Information;

import Service_Threads.Match;
import Service_Threads.ServerThread;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Barreira encarregue de fazer matchmaking (associar a thread a jogo com jogadores de rank semelhante e
 * fazer a thread esperar até haver condições para começar um jogo).
 */
public class lobbyBarrier {
    /** Contentor de todas as threads à espera de começar a jogar */
    private List<TreeSet<ServerThread>> playersWaiting;
    /** Estrutura que permite saber quantos jogadores se encontram em cada entrada de playersWaiting.*/
    private int[] playersEntering;
    /** Época atual do jogo */ //TODO explicar melhor isto que o lobo não percebe
    private int[] gameEpoch;
    /** Número de ranks */
    private static final int rankNum = 10;
    /** Número de jogadores por jogo.*/
    private static final int size = 2; // TODO: Valor temporário, mudar
    /** Coleção de threads dedicadas a jogadores.*/
    private HashSet<ServerThread> players;
    /** Lock associado à condição do lobby i */
    private ReentrantLock[] lockLobbies;
    /** Condition i -> lobby i está disponível para entrar? */
    private Condition[] conditionLobbiesAvailable;


    /**
     * Construtor
     */
    public lobbyBarrier() {
        playersEntering = new int[size];

        /* indice 0: Pessoas de rank 0
         * indice 1: Pessoas de rank 1
         * etc*/
        playersWaiting = new ArrayList<>();

        lockLobbies = new ReentrantLock[rankNum];
        conditionLobbiesAvailable = new Condition[rankNum];
        gameEpoch = new int[rankNum];


        // inicializar todos os TreeSets da lista
        for (int i = 0; i < size; i++) {
            playersWaiting.add(i,new TreeSet<>());
            lockLobbies[i] = new ReentrantLock();
            conditionLobbiesAvailable[i] = lockLobbies[i].newCondition();
            gameEpoch[i] = 0;
            playersEntering[i] = 0;
        }
        players = new HashSet<>();
    }

    /**
     * Adicionar a todos os jogadores à espera de um certo jogo o objecto Match, para terem agora associado
     * um jogo concreto
     *
     * @param playerThreads Lobby de nrPlayers jogadores
     * @param match Objecto que será variável partilhada de todas as threads
     */
    public void informThreadsOfAddedMatch(TreeSet<ServerThread> playerThreads, Match match) {
        for (ServerThread st : playerThreads) {
            st.associateMatch(match);
        }
    }

    public void printPlayersInLobby(TreeSet<ServerThread> playerThreads) {
        for (ServerThread st : playerThreads) {
            System.out.println(st.getPlayer());
        }
    }

    /**
     * Manter thread de jogador a dormir até as condições necessárias ocorrerem para esta poder começar a jogar //TODO este método atualmente faz mt mais que isto
     * @param st thread que presta serviços ao cliente
     */
    public void waitGame(ServerThread st) {
            System.out.println("Entrou para a lista de espera o jogador: " + st.getPlayer().getUsername());
            // TODO: Implementar distribuição normal pelas salas disponíveis talvez para reduzir espera?
            Player player = st.getPlayer();

            int lobbyIndex = player.getRank();

        try {
            // Lock acedido quando são escritas variáveis partilhadas
            lockLobbies[lobbyIndex].lock();

            // Em que instância de um certo lobby vou estar?
            int myEpoch = gameEpoch[lobbyIndex];

            //Anuncia que entrou mais um jogador
            playersEntering[lobbyIndex]++; //TODO: Como se vai mudar a implementação para ir buscar também aos vizinhos

            // Já posso começar o jogo?
            if (playersEntering[lobbyIndex] == size) {  //TODO: Colocar isto tudo dentro de uma função e com comentários a explicar
                startMatch(st);
            }

            else if (playersEntering[lobbyIndex] == 1) { //TODO Porque não fazer isto no final de startMatch e evitar este else if?
                // só um jogador novo, re-iniciar lista de espera
                playersWaiting.get(lobbyIndex).clear();
                playersWaiting.get(lobbyIndex).add(st);
            }

            printPlayersInLobby(playersWaiting.get(lobbyIndex));

            try {
                while (myEpoch == gameEpoch[lobbyIndex]) {
                    conditionLobbiesAvailable[lobbyIndex].await();
                }
            } catch (InterruptedException e) {
                st.cleanup();
            }

        }
        finally {
            lockLobbies[lobbyIndex].unlock(); //TODO: Talvez seja necessario mudar isto para antes do condition await?
        }
    }

    private void startMatch(ServerThread st){

        Player player = st.getPlayer();

        int lobbyIndex = player.getRank();

        playersWaiting.get(lobbyIndex).add(st);

        Match match = new Match((TreeSet<ServerThread>) playersWaiting.get(lobbyIndex).clone(), size, new ReentrantLock());
        informThreadsOfAddedMatch(playersWaiting.get(lobbyIndex), match);

        new Thread(match).start();

        gameEpoch[lobbyIndex]++;
        conditionLobbiesAvailable[lobbyIndex].signal();
        playersEntering[lobbyIndex] = 0;
    }
}
