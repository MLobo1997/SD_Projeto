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
    /** Época atual do jogo */
    private int[] gameEpoch;
    /** Número de ranks */
    private int rankNum;
    /** Número de jogadores por jogo.*/
    private int size;
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
        size = 2; // TODO: Valor temporário, mudar
        rankNum = 10;
        playersEntering = new int[size];

        /** indice 0: Pessoas de rank 0
         * indice 1: Pessoas de rank 0 até 1 */
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
     * Manter thread de jogador a dormir até as condições necessárias ocorrerem para esta poder começar a jogar
     * @param st thread que presta serviços ao cliente
     */
    public void waitGame(ServerThread st) {
        System.out.println("Entrou " +  st.getPlayer().getUsername());
        // TODO: Implementar distribuição normal pelas salas disponíveis talvez para reduzir espera?
        Player player = st.getPlayer();

        // Usado para determinar para qual dos lobbies jogador irá
        int rankCap = (int) Math.ceil(player.getRanking());

        // Como jogador irá para o indice rankJogador - 1, lidar com o caso de excessão rank = 0;
        int lobbyIndex = (rankCap == 0) ? 0 : (rankCap - 1);

        // Lock acedido quando são escritas variáveis partilhadas
        lockLobbies[lobbyIndex].lock();

        // Em que instância de um certo lobby vou estar?
        int myEpoch = gameEpoch[lobbyIndex];

        // Lida com caso de exceção em que se fores de rank 0 serias associado ao indice -1
        playersEntering[lobbyIndex]++;

        // Já posso começar o jogo?
        if (playersEntering[lobbyIndex] == size) {
            playersWaiting.get(lobbyIndex).add(st);
            Match match = new Match((TreeSet<ServerThread>)playersWaiting.get(lobbyIndex).clone(),size,new ReentrantLock());
            informThreadsOfAddedMatch(playersWaiting.get(lobbyIndex),match);
            new Thread(match).start();
            gameEpoch[lobbyIndex]++;
            conditionLobbiesAvailable[lobbyIndex].signal();
            playersEntering[lobbyIndex] = 0;

        } else if (playersEntering[lobbyIndex] == 1) {
            // só um jogador novo, re-iniciar lista de espera
            playersWaiting.get(lobbyIndex).clear();
            playersWaiting.get(lobbyIndex).add(st);
        } else {
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

        lockLobbies[lobbyIndex].unlock();
    }
}
