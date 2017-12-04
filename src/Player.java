import java.util.ArrayList;

/**
 * A classe player representa cada um dos utilizadores existentes e guarda toda a informação necessária dos mesmo.
 */
public class Player {

    /** Nome do utilizador */
    private String name;
    /** Passe de acesso do utilizador*/
    private String password;
    /** Os rankings, valores entre 0 e 9, de todos os jogos feitos pelo jogador até ao momento*/
    private ArrayList<Integer> previousRankings;
    /** Ranking médio do jogador, valor entre 0 e 9, derivado da média de classificações de todos os seus jogos já feitos*/
    private Double ranking;

    /** Método de contrução do objeto Player, utilizando quando um utilizador é inserido pela primeira vez no sistema.
     *
     * @param name Nome do utilizador.
     * @param password Passe de acesso.
     */
    public Player(String name, String password) {
        this.name = name;
        this.password = password;
        previousRankings = new ArrayList<>();
        ranking = 0.0;
    }

    public static void main(String [] args){
        Player p = new Player("João", "passe");

        System.out.println(p);

        p.addGame(4);
        p.addGame(2);
        p.addGame(9);
        p.addGame(9);
        p.addGame(1);
        p.addGame(1);

        System.out.println(p);
    }

    /** Adiciona um valor de ranking ao histórico de jogos do utilizador, atualizando o ranking geral do mesmo.
     *
     * @param rank Rank do jogo.
     * @throws IllegalArgumentException Exceção de ter sido dado como argumento um valor não contido em [0, 9].
     */
    public void addGame(Integer rank) throws IllegalArgumentException{
        if(rank >= 0 && rank <= 9) {
            previousRankings.add(rank);
            ranking = (previousRankings.stream().mapToInt(Integer::intValue).sum()) / (double) previousRankings.size();
        }
        else{
            throw new IllegalArgumentException("A função addGame foi evocada com um valor inferior a 0 ou superior a 9");
        }

    }

    /** Método de debug.
     *
     * @return Leitura do objeto em String.
     */
    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", previousRankings=" + previousRankings +
                ", ranking=" + ranking +
                '}';
    }
}
