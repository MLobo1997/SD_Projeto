package Game_Information;

import Service_Threads.ServerThread;

import java.util.TreeSet;

/**
 * Informação relativa a um certo jogo
 */
public class MatchInfo {
    private TreeSet<MessageBuffer> messageBuffers;
    /** Conjunto dos serverThreads dos jogadores incluidos neste jogo */
    private TreeSet<ServerThread> players;
    /** Escolhas de herois da equipa 1 */
    private HeroSelection picksTeamOne;
    /** Escolhas de herois da equipa 2 */
    private HeroSelection picksTeamTwo;
    /** Número de jogadores */
    private int playerNum;

    /**
     * Construtor
     * @param players Coleção de jogadores a ingressar no jogo
     * @param size Número de jogadores
     */
    public MatchInfo(TreeSet<ServerThread> players,int size) {
        this.messageBuffers = messageBuffers;
        this.players = players;
        this.playerNum = size;
    }


    /**
     * Getter da equipa 1
     *
     * @return equipa 1 do jogo
     */
    public HeroSelection getPicksTeamOne() {
        return picksTeamOne;
    }

    /**
     * Getter da equipa 2
     * @return equipa 2 do jogo
     */
    public HeroSelection getPicksTeamTwo() {
        return picksTeamTwo;
    }

    /**
     * Getter do número de jogadores
     * @return número de jogadores que têm de estar presentes no jogo
     */
    public int getPlayerNum() {
        return playerNum;
    }

    /**
     * Getter da lista de jogadores
     *
     * @return TreeSet de todas as serverThreads que estão a servir os respetivos jogadores
     */
    public TreeSet<ServerThread> getPlayers() {
        return players;
    }
}
