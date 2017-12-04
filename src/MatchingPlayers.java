import java.util.LinkedList;

/** Onde é controlado toda a gestão de matching
 *
 */
public class MatchingPlayers {
    /** Lista de jogadores sempre ordenada por ranking. Escolhi LinkedList por ser mais eficiente em remoção e inserção ordenada.*/
    private final LinkedList<Player> players = new LinkedList<>();

    public static void main(String [] args){
        Player p = new Player(1,"josue", "segredo");
        p.addGame(4);
        p.addGame(2);
        p.addGame(9);
        p.addGame(9);
        p.addGame(1);
        p.addGame(1);
        Player p1 = new Player(2,"mano", "sexo");
        p1.addGame(2);
        p1.addGame(3);
        p1.addGame(1);
        p1.addGame(4);
        p1.addGame(0);
        p1.addGame(2);
        Player p2 = new Player(3,"check", "mate");
        p2.addGame(1);
        p2.addGame(2);
        p2.addGame(3);
        p2.addGame(8);
        p2.addGame(6);
        p2.addGame(5);

        MatchingPlayers players = new MatchingPlayers();

        players.addPlayer(p2);
        players.addPlayer(p1);
        players.addPlayer(p);

        System.out.println(players);
    }


    /** Adiciona um jogador de forma ordenada por média de ranking.
     *
     * @param p Jogador a ser adicionado.
     * @throws IllegalArgumentException
     */
    public void addPlayer(Player p) throws IllegalArgumentException{
        int i;

        for(i = 0 ; i < players.size() ; i++) {
            if(p.getID() != players.get(i).getID()) {
                if(p.getRanking() < players.get(i).getRanking()){
                    break;
                }
            }
            else{
                throw new IllegalArgumentException("Foi colocado um jogador em matchmaking que já se encontrava em matchmaking");
            }
        }

        players.add(i, p);
    }

    /** Método de debug.
     *
     * @return Leitura do objeto em String.
     */
    @Override
    public String toString() {
        return "MatchingPlayers{" +
                "players=" + players +
                '}';
    }
}
