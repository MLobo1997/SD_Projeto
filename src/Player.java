import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.Socket;

/**
 * A classe player representa cada um dos utilizadores existentes e guarda toda a informação necessária dos mesmo.
 */
public class Player implements Serializable, Comparable {
    /** Número de identificação de um jogador. */
    private Integer ID;
    /** Nome do utilizador. */
    private String name;
    /** Passe de acesso do utilizador. */
    private String password;
    /** Número de jogos feitos até agora */
    private Integer nrOfGames;
    /** Ranking médio do jogador, valor entre 0 e 9, derivado da média de classificações de todos os seus jogos já feitos. */
    private Double ranking;
    /** Socket de comunicação com o servidor, null no caso de não estar conectado*/
    private Socket socket;

    /** Método de contrução do objeto Player, utilizando quando um utilizador é inserido pela primeira vez no sistema.
     *
     * @param name Nome do utilizador.
     * @param password Passe de acesso.
     */
    public Player(Integer ID, String name, String password) {
        this.ID = ID;
        this.name = name;
        this.password = password;
        nrOfGames = 0;
        ranking = 0.0;
        socket = null;
    }

    public static void main(String [] args){
    }

    public String getUsername() { return name; }

    public String getPassword() { return password; }

    /** Getter do ID do jogador.
     *
     * @return ID.
     */
    public Integer getID() {
        return ID;
    }

    /** Getter do ranking médio do jogador
     *
     * @return Ranking
     */
    public Double getRanking() {
        return ranking;
    }

    /** Adiciona um valor de ranking ao histórico de jogos do utilizador, atualizando o ranking geral do mesmo.
     *
     * @param rank Rank do jogo.
     * @throws IllegalArgumentException Exceção de ter sido dado como argumento um valor não contido em [0, 9].
     */
    public void addGame(Integer rank) throws IllegalArgumentException{
        if(rank >= 0 && rank <= 9) {
            ranking = (ranking * nrOfGames + rank) / ((double) nrOfGames + 1);
            nrOfGames++;
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
                "ID=" + ID +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", nrOfGames=" + nrOfGames +
                ", ranking=" + ranking +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        return ranking.compareTo(((Player) o).getRanking());
    }
}
