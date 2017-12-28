package Service_Threads;

import Game_Information.MatchInfo;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Match implements Runnable {
    /**
     * ------ Informação de jogo
     */
    private MatchInfo matchInfo;
    /** ------ Controlo de concorrência */
    /**
     * Contador de quantas threads já foram acordadas
     */
    private int threadsAwoken;
    /**
     * Lock para sinalizar que a thread está acordada
     */
    private ReentrantLock matchLock;
    /**
     * Condição: Já todas as threads dos jogadores estão acordadas?
     */
    private Condition allPlayersReady;

    /**
     * Construtor
     *
     * @param players   Lista de jogadores a ingressar no jogo
     * @param size      Número de jogadores
     * @param matchLock Lock único para cada match
     */
    public Match(TreeSet<ServerThread> players, int size, ReentrantLock matchLock) {
        /** Clonar para não ficar com serverThreads de outro jogo quando for modificado */
        matchInfo = new MatchInfo(players, players.size());
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
    public MatchInfo getMatchInfo() {
        return matchInfo;
    }

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
    public void waitFor(int secs) {
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
        System.out.println("Waiting for " + threadsAwoken + " equal to " + matchInfo.getPlayerNum());
        try {
            while (threadsAwoken != matchInfo.getPlayerNum()) {
                try {
                    allPlayersReady.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            matchLock.unlock();
        }
    }

    /**
     * Escolha do herói
     *
     * @param s    jogador que escolheu o herói
     * @param hero indíce do herói escolhido
     * @return verdade se foi possível escolher o herói, falso caso contrário
     */
    public boolean chooseHero(ServerThread s, int hero) {
        return matchInfo.chooseHero(s, hero);
    }


    /**
     * Guardar informação do jogo numa log file para servir de histórico e
     * verificar se o sistema funciona
     */
    private void saveGameInfo() {
        FileWriter writer = null;
        try {
            writer = new FileWriter("matches.txt", true);
            writer.append(new SimpleDateFormat("HH:mm:ss").format(new Date()) + "\n");
            writer.append(matchInfo.getPlayersTeamOne() + "\n");
            writer.append(matchInfo.getPlayersTeamTwo() + "\n");
            writer.append("----\n");
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {


        waitForGameToStart();

        echoMessage("Equipa 1: " + matchInfo.getPlayersTeamOne());
        echoMessage("Equipa 2: " + matchInfo.getPlayersTeamTwo());
        echoMessage("O jogo começou!");

        for (int i = 0; i < 6; i++) {
            echoMessage(i * 5 + " segundos passaram.");
            waitFor(5);
        }

        echoMessage("&GAMEOVER&");
        echoMessage("Jogo acabou, escreva \"quit\" para voltar ao menu principal");

        saveGameInfo();


    }
}
