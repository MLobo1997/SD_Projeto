import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

public class Barrier {
    List<TreeSet<Player>> playersWaiting;
    int[] playersEntering;
    int size;
    HashSet<ServerThread> players;

    public Barrier() {
        size            = 2; // TODO: Valor temporário, mudar
        playersEntering = new int[size];

        // Inicializar lista de TreeSets
        playersWaiting  = new ArrayList<>();
        // inicializar todos os TreeSets da lista
        for (int i = 0; i < size; i++) {
            playersWaiting.add(i,new TreeSet<>());
        }
    }
    synchronized void waitGame(ServerThread st) {
        Player player = st.getPlayer();
        int rankCap = (int) Math.floor(player.getRanking());
        // Como para jogador irá para o indice rankJogador - 1, lidar com o caso de excessão rank = 0;
        int lobbyIndex = (rankCap == 0) ? 0 : (rankCap - 1);

        // Lida com caso de exceção em que se fores de rank 0 serias associado ao indice -1
        playersEntering[lobbyIndex]++;

        TreeSet<Player> selectedLobby = rankCap != 0
                ? playersWaiting.get(lobbyIndex)
                : playersWaiting.get(0);

        // Já posso começar o jogo?
        if (playersEntering[lobbyIndex] % size == 0) {
            selectedLobby.add(player);
            notifyAll();
        }
        if (playersEntering[lobbyIndex] % size == 1) {
            // só um jogador novo, re-iniciar lista de espera
            playersWaiting.add(lobbyIndex,new TreeSet<>());
            selectedLobby.add(player);
        }

        try {

            while (playersEntering[lobbyIndex] % size != 0) {
                wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
