import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** TODO: dar uma explicação desta classe
 *
 */
public class Barrier {
    /** Coleção de jogadores que estão atualmente à espera, registados numa estrutura tipo matricial, em
     * que as linhas representam o intervalo de valores (entre inteiros) de ranking em que os jogadores se encontram.
     * E.g.: Um jogador com uma média de ranking 4.33 estará contido na 4a entrada.
     */


    /** --------- Info do jogo ------- */
    private List<TreeSet<ServerThread>> playersWaiting;
    /** Estrutura que permite saber quantos jogadores se encontram em cada entrada de playersWaiting.*/
    private int[] playersEntering;
    /** Número de ranks */
    private int rankNum;
    /** Número de jogadores por jogo.*/
    private int size;
    /** Coleção de threads dedicadas a jogadores.*/
    private HashSet<ServerThread> players;


    /** --------- Info de controlo de concorrência ------- */

    /** Lock associado à condição do lobby i */
    private ReentrantLock[] lockLobbies;
    /** Condition i -> lobby i está disponível para entrar? */
    private Condition[] conditionLobbiesAvailable;
    /** Época atual do jogo */
    private int[] gameEpoch;

    /** Construtor de barreira.
     *
     */    /** --------- Info do jogo ------- */
    public Barrier() {
        size = 2; // TODO: Valor temporário, mudar
        rankNum = 10;
        playersEntering = new int[size];
        // indice 0: Pessoas de rank 0
        // indice 1: Pessoas de rank 0 até 1
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

    public void informThreadsOfAddedMatch(TreeSet<ServerThread> playerThreads,Match match) {
        for (ServerThread st : playerThreads) {
            st.associateMatch(match);
        }
    }

    /**
     * Manter thread de jogador a dormir até as condições necessárias ocorrerem para esta poder começar a jogar
     * @param st thread que presta serviços ao cliente
     */
    void waitGame(ServerThread st) {
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

        System.out.println(gameEpoch[lobbyIndex]);

        // Já posso começar o jogo?
        if (playersEntering[lobbyIndex] == size) {
            playersWaiting.get(lobbyIndex).add(st);
            Match match = new Match(playersWaiting.get(lobbyIndex),size,new ReentrantLock());
            informThreadsOfAddedMatch(playersWaiting.get(lobbyIndex),match);
            new Thread(match).start();
            gameEpoch[lobbyIndex]++;
            conditionLobbiesAvailable[lobbyIndex].signal();
            playersEntering[lobbyIndex] = 0;

        } else if (playersEntering[lobbyIndex] == 1) {
            // só um jogador novo, re-iniciar lista de espera
            playersWaiting.get(lobbyIndex).clear();
            playersWaiting.get(lobbyIndex).add(st);
        }

        System.out.println(playersWaiting);

        try {
            while (myEpoch == gameEpoch[lobbyIndex]) {
                conditionLobbiesAvailable[lobbyIndex].await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        lockLobbies[lobbyIndex].unlock();

    }
}
