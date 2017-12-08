import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

/** TODO: dar uma explicação desta classe
 *
 */
public class Barrier {
    /**Coleção de jogadores que estão atualmente à espera, registados numa estrutura tipo matricial, em
     * que as linhas representam o intervalo de valores (entre inteiros) de ranking em que os jogadores se encontram.
     * E.g.: Um jogador com uma média de ranking 4.33 estará contido na 4a entrada.
     */
    private List<TreeSet<Player>> playersWaiting;
    /**Estrutura que permite saber quantos jogadores se encontram em cada entrada de playersWaiting.*/
    private int[] playersEntering;
    /**Número de jogadores por jogada.*/
    private int size;
    /**Coleção de threads dedicadas a jogadores.*/
    private HashSet<ServerThread> players;

    /** Construtor de barreira.
     *
     */
    public Barrier() {
        size            = 2; // TODO: Valor temporário, mudar
        playersEntering = new int[size];

        // Inicializar lista de TreeSets:
        // indice 0: Pessoas de rank 0
        // indice 1: Pessoas de rank 0 até 1
        playersWaiting  = new ArrayList<>();

        // inicializar todos os TreeSets da lista
        for (int i = 0; i < size; i++) {
            playersWaiting.add(i,new TreeSet<>());
        }
    }

    /**
     * Manter thread de jogador a dormir até as condições necessárias ocorrerem para esta poder começar a jogar
      * @param st thread que presta serviços ao cliente
     */
    synchronized void waitGame(ServerThread st) {
        System.out.println("Entrou " +  st.getPlayer().getUsername());
        // TODO: Implementar distribuição normal pelas salas disponíveis talvez para reduzir espera?
        Player player = st.getPlayer();

        // Usado para determinar para qual dos lobbies jogador irá
        int rankCap = (int) Math.floor(player.getRanking());

        // Como jogador irá para o indice rankJogador - 1, lidar com o caso de excessão rank = 0;
        int lobbyIndex = (rankCap == 0) ? 0 : (rankCap - 1);

        // Lida com caso de exceção em que se fores de rank 0 serias associado ao indice -1
        playersEntering[lobbyIndex]++;

        // Já posso começar o jogo?
        if (playersEntering[lobbyIndex] % size == 0) {
            playersWaiting.get(lobbyIndex).add(player);
            notifyAll();
        }

        if (playersEntering[lobbyIndex] % size == 1) {
            // só um jogador novo, re-iniciar lista de espera
            playersWaiting.get(lobbyIndex).clear();
            playersWaiting.get(lobbyIndex).add(player);
        }

        System.out.println(playersWaiting);

        try {
            while (playersEntering[lobbyIndex] % size != 0) {
                wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
