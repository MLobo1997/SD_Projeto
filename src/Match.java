import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Match implements Runnable {
    /** ------ Informação de jogo */
    /** Conjunto dos serverThreads dos jogadores incluidos neste jogo */
    private TreeSet<ServerThread> players;
    /** Escolhas de herois da equipa 1 */
    private pickedHeroes picksTeamOne;
    /** Escolhas de herois da equipa 2 */
    private pickedHeroes picksTeamTwo;
    /** Número de jogadores */
    private int size;

    /** ------ Controlo de concorrência */
    /** Contador de quantas threads já foram acordadas */
    private int threadsAwoken;
    /** Lock para sinalizar que a thread está acordada */
    private ReentrantLock matchLock;
    /** Condição: Já todas as threads dos jogadores estão acordadas? */
    private Condition allPlayersReady;

    Match (TreeSet<ServerThread> players,int size,ReentrantLock matchLock) {
        /** Clonar para não ficar com serverThreads de outro jogo quando for modificado */
        this.players = (TreeSet<ServerThread>) players.clone();
        this.size = size;
        this.matchLock = matchLock;
        allPlayersReady = this.matchLock.newCondition();
        threadsAwoken = 0;
    }

    public pickedHeroes getPicksTeamOne() {
        return picksTeamOne;
    }

    public pickedHeroes getPicksTeamTwo() {
        return picksTeamTwo;
    }

    public int getThreadsAwoken() {
        return threadsAwoken;
    }

    public ReentrantLock getMatchLock() {
        return matchLock;
    }

    public Condition getAllPlayersReadyCondition() {
        return allPlayersReady;
    }

    public void incrementPlayersReady() {
        threadsAwoken++;
    }

    public int getPlayerNum() {
        return size;
    }


    public TreeSet<ServerThread> getPlayers() {
        return players;
    }

    public void waitFor (int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void printBroadcast(String line) {
        for (ServerThread st : players) {
            st.printToOutput(line);
        }
    }

    public void waitForGameToStart() {
        printBroadcast("WAIT DO MATCH");


        matchLock.lock();


        while (threadsAwoken != size) {
            try {
                allPlayersReady.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        matchLock.unlock();
    }

    public void run() {

        waitForGameToStart();

        printBroadcast("Welcome to the match!");

        for (int i = 0; i < 2; i++) {
            printBroadcast(i * 5 + " seconds passed.");
            waitFor(5);
        }

        printBroadcast("$GAMEOVER$");
        printBroadcast("Game is over");

    }

    public class pickedHeroes {
        /** Associar a cada indice um jogador, onde escreve um nr de 1 a 30 (representa os herois) */
        private int[] escolhas;
        private int size;

        public pickedHeroes() {
            escolhas = new int[size]; // TODO: MUDAR ESTA VARIAVEL TEMPORARIA
            size = 2;
        }

        synchronized public boolean chooseHero(int indexJogador,int indexHeroi) {
            for (int i = 0; i < size; i++) {
                if (escolhas[i] == indexHeroi) {
                    return false;
                }
            }

            escolhas[indexJogador] = indexHeroi;

            return true;
        }
    }

}
