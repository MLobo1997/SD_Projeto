import java.io.Serializable;

/**
 * A classe player representa cada um dos utilizadores existentes e guarda toda a informação necessária dos mesmo.
 */
public class Player implements Serializable, Comparable {
    /** Nome do utilizador. */
    private String name;
    /** Passe de acesso do utilizador. */
    private String password;
    /** Número de jogos feitos até agora */
    private Integer nrOfGames;
    /** Ranking médio do jogador, valor entre 0 e 9, derivado da média de classificações de todos os seus jogos já feitos. */
    private Double ranking;
    /** Identifica se um utilizador já está associado a um cliente (prevenir vários clientes a associarem-se à mesma conta */
    private boolean online;

    /** Método de contrução do objeto Player, utilizando quando um utilizador é inserido pela primeira vez no sistema.
     *
     * @param name Nome do utilizador.
     * @param password Passe de acesso.
     */
    public Player(String name, String password) {
        this.name     = name;
        this.password = password;
        online        = false;
        nrOfGames     = 0;
        ranking       = 0.0;
    }

    public static void main(String [] args){
    }

    /** Getter do username do jogador.
     *
     * @return username.
     */
    public String getUsername() { return name; }

    /** Getter da pass do jogador.
     *
     * @return pass.
     */
    private String getPassword() { return password; }

    /** Getter do ranking médio do jogador
     *
     * @return Ranking
     */
    public Double getRanking() {
        return ranking;
    }

    public void setRank(double rank) {
        this.ranking = rank;
    }

    /**
     * Sinalizar jogador como online
     */
    public void goOnline() {
        online = true;
    }

    /**
     * Sinalizar jogador como offline
     */
    public void goOffline() {
        online = false;
    }

    /**
     * Sinalizar jogador como online
     */
    public boolean isOnline() {
        return online;
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

    /**Verifica se as passes entre as duas instâncias de jogador correspondem.
     *
     * @param password Outro jogador.
     * @return True se correspondem.
     */
    public boolean passwordEquals(String password){
        return (this.password.equals(password));
    }

    /** Método de debug.
     *
     * @return Leitura do objeto em String.
     */
    @Override
    public String toString() {
        return "Player{" +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", nrOfGames=" + nrOfGames +
                ", ranking=" + ranking +
                '}';
    }
}
