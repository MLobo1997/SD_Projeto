import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

public class Barrier {
    List<TreeSet<ServerThread>> playersWaiting;
    int[] playersEntering;
    int size;
    HashSet<ServerThread> players;

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

    public void showPlayersInLobby(int lobbyIndex) {
        System.out.print("[");
        for (ServerThread s : playersWaiting.get(lobbyIndex)) {
            System.out.print(s.getPlayer().getUsername() + ",");
        }
        System.out.println("]");
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
            playersWaiting.get(lobbyIndex).add(st);
            notifyAll();

            Match m = new Match(playersWaiting.get(lobbyIndex));
            m.run();
            try {
                m.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (playersEntering[lobbyIndex] % size == 1) {
            // só um jogador novo, re-iniciar lista de espera
            playersWaiting.get(lobbyIndex).clear();
            playersWaiting.get(lobbyIndex).add(st);
        }

        showPlayersInLobby(lobbyIndex);

        try {
            while (playersEntering[lobbyIndex] % size != 0) {
                wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
