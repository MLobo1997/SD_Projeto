package Service_Threads;

import Game_Information.MatchInfo;

import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Match implements Runnable {
    /** ------ Informação de jogo */
    private MatchInfo matchInfo;
    /** ------ Controlo de concorrência */
    /** Contador de quantas threads já foram acordadas */
    private int threadsAwoken;
    /** Lock para sinalizar que a thread está acordada */
    private ReentrantLock matchLock;
    /** Condição: Já todas as threads dos jogadores estão acordadas? */
    private Condition allPlayersReady;

    /**
     * Construtor
     * @param players Lista de jogadores a ingressar no jogo
     * @param size Número de jogadores (TODO: Sacar só players.size()?
     * @param matchLock Lock único para cada match
     */
    public Match (TreeSet<ServerThread> players,int size,ReentrantLock matchLock) {
        /** Clonar para não ficar com serverThreads de outro jogo quando for modificado */
        matchInfo = new MatchInfo(players,size);
        this.matchLock = matchLock;
        allPlayersReady = this.matchLock.newCondition();
        threadsAwoken = 0;
    }

    /**
     * Getter de threads acordadas
     *
     * @return Número de threads acordadas
     */
    public int getThreadsAwoken() {
        return threadsAwoken;
    }

    /**
     * Getter de lock do jogo
     *
     * @return Lock do jogo
     */
    public ReentrantLock getMatchLock() {
        return matchLock;
    }

    /**
     * Getter da condição de espera
     *
     * @return Condição de espera
     */
    public Condition getAllPlayersReadyCondition() {
        return allPlayersReady;
    }

    /**
     * Getter de informação do jogo
     *
     * @return Agregador de toda a informação importante do jogo associado
     */
    public MatchInfo getMatchInfo() { return matchInfo; }

    /**
     * Incrementa o número de threads prontas a jogar
     */
    public void incrementPlayersReady() {
        threadsAwoken++;
    }

    /**
     * Esperar durante um certo tempo
     *
     * @param secs tempo a esperar
     */
    public void waitFor (int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Atualizar o buffer das threads que servem os jogadores associados ao jogo
     * (para implementar funcionalidades de chat p.e.)
     *
     * @param line Linha a ser escrita em broadcast
     */
    public void echoMessage(String line) {
        for (ServerThread st : matchInfo.getPlayers()) {
            st.getMessageBuffer().publishMessage(line);
        }
    }

    /**
     * Esperar até todos os jogadores estarem prontos (previne o alarme começar antes de toda a gente estar pronta)
     */
    public void waitForGameToStart() {
        matchLock.lock();

        while (threadsAwoken != matchInfo.getPlayerNum()) {
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

        echoMessage("Welcome to the match!");

        for (int i = 0; i < 2; i++) {
            echoMessage(i * 5 + " seconds passed.");
            waitFor(5);
        }

        echoMessage("$GAMEOVER$");
        echoMessage("Game is over");

    }


}
