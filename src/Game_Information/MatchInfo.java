package Game_Information;

import Service_Threads.ServerThread;
import User_Executables.Server;
import org.omg.PortableServer.ServantRetentionPolicyValue;

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
    /** ServerThreads dos jogadores da equipa 1 */
    private TreeSet<ServerThread> teamOne;
    /** ServerThreads dos jogadores da equipa 2 */
    private TreeSet<ServerThread> teamTwo;

    /**
     * Construtor
     * @param players Coleção de jogadores a ingressar no jogo
     * @param size Número de jogadores
     */
    public MatchInfo(TreeSet<ServerThread> players, int size) {
        this.messageBuffers = new TreeSet<>();
        this.players = players;
        this.playerNum = size;
        this.teamOne = new TreeSet<>();
        this.teamTwo = new TreeSet<>();
        int i = 0;
        for(ServerThread s: players){
            if(i % 2 == 1) teamOne.add(s);
            else teamTwo.add(s);
            i++;
        }
        this.picksTeamOne = new HeroSelection();
        this.picksTeamTwo = new HeroSelection();
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
     * @return TreeSet de todas as serverThreads que estão a servir os respetivos jogadores
     */
    public TreeSet<ServerThread> getPlayers() {
        return players;
    }

    /**
     * Demonstra nomes dos jogadores na equipa 1
     * @return String que identifica jogadores da equipa 1
     */
    public String getPlayersTeamOne() {
        StringBuilder sb = new StringBuilder();
        sb.append("| ");
        teamOne.forEach(x -> sb.append("name:" + x.getPlayer().getUsername() + ",rank:" + x.getPlayer().getRank()
                +",hero:" + (this.getHeroPick(x) == -1 ? "n/a" : this.getHeroPick(x)) + " | "));

        return sb.toString();
    }

    /**
     * Demonstra nomes dos jogadores na equipa 2
     * @return String que identifica jogadores da equipa 2
     */
    public String getPlayersTeamTwo() {
        StringBuilder sb = new StringBuilder();
        sb.append("| ");
        teamTwo.forEach(x -> sb.append("name:" + x.getPlayer().getUsername() + ",rank:" + x.getPlayer().getRank()
                +",hero:" + (this.getHeroPick(x) == -1 ? "n/a" : this.getHeroPick(x)) + " | "));

        return sb.toString();
    }

    private int getHeroPick(ServerThread st) {

        int team = -1;

        if(teamOne.stream().filter(t -> t == st).count() == 1){
            team = 1;
        }
        if(teamTwo.stream().filter(t -> t == st).count() == 1){
            team = 2;
        }

        if (team == 1) {
            return picksTeamOne.getHero(st.getPlayer().getUsername());
        } else if (team == 2) {
            return picksTeamTwo.getHero(st.getPlayer().getUsername());

        }
        return -1;
    }

    /**
     * Escolha do herói
     * @param s jogador que escolheu o herói
     * @param hero indíce do herói escolhido
     * @return verdade se foi possível escolher o herói, falso caso contrário
     */
    public boolean chooseHero(ServerThread s, int hero){
        boolean res = false;

        if(teamOne.stream().filter(t -> t == s).count() == 1){
            res = picksTeamOne.chooseHero(s.getPlayer().getUsername(),hero);
        }
        if(teamTwo.stream().filter(t -> t == s).count() == 1){
            res = picksTeamTwo.chooseHero(s.getPlayer().getUsername(),hero);
        }

        return res;
    }
}
