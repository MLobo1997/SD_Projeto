package Game_Information;


import java.io.Serializable;

/**
 * A classe player representa cada um dos utilizadores existentes e guarda toda a informação necessária dos mesmo.
 */
public class Player implements Serializable, Cloneable {
    /** Nome do utilizador. */
    private String name;
    /** Passe de acesso do utilizador. */
    private String password;
    /** Identifica se um utilizador já está associado a um cliente (prevenir vários clientes a associarem-se à mesma conta */
    private boolean online;
    /** Rank do jogador, valor entre 0 e 9, derivado da soma das classificações de todos os seus jogos já feitos.
     * Rank = 0 -> xp =  [0 , 9[
     * Rank = 1 -> xp =  [9 , 27[
     * Rank = 2 -> xp =  [27 , 54[
     * Rank = 3 -> xp =  [54 , 90[
     * Rank = 4 -> xp =  [90 , 135[
     * Rank = 5 -> xp =  [135 , 189[
     * Rank = 6 -> xp =  [189 , 252[
     * Rank = 7 -> xp =  [252 , 324[
     * Rank = 8 -> xp =  [324 , 405[
     * Rank = 9 -> xp >= 405
     */
    private Integer rank;
    /** Experience points do jogador, utilizados para contabilizar a soma de todas as classificações obtidas pelo jogador, o que determina o rank. */
    private Integer xp;

    /** Método de contrução do objeto Game_Information.Player, utilizando quando um utilizador é inserido pela primeira vez no sistema.
     *
     * @param name Nome do utilizador.
     * @param password Passe de acesso.
     */
    public Player(String name, String password) {
        this.name     = name;
        this.password = password;
        online        = false;
        rank          = 0;
        xp            = 0;
    }

    /**
     * Construtor
     *
     * @param p Instância de jogador a ser copiada.
     */
    public Player(Player p){
        this.name = p.name;
        this.password = p.password;
        this.online = p.online;
        this.rank = p.rank;
        this.xp      = p.xp;
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
    public String getPassword() { return password; }

    /** Getter do xp.
     *
     * @return xp.
     */
    public Integer getXP() {
        return xp;
    }

    /**
     * Sinalizar jogador como online
     */
    public void goOnline() {
        online = true;
    }

    /**
     * Sinalizar jogador como offline
     * @return O jogador offline.
     */
    public Player goOffline() {
        online = false;
        return this;
    }

    /**
     * Sinalizar jogador como online
     */
    public boolean isOnline() {
        return online;
    }

    /** Getter do rank do jogador
     *
     * @return rank
     */
    public Integer getRank() {
        return rank;
    }

    /** Adiciona um valor de ranking ao histórico de jogos do utilizador, atualizando o rank do mesmo.
     *
     * @param gameRank Rank do jogo (0-9).
     * @throws IllegalArgumentException Exceção de ter sido dado como argumento um valor não contido em [0, 9].
     * @return True caso tenha subido de rank.
     */
    public boolean addGame(Integer gameRank) throws IllegalArgumentException{
        boolean upgraded = false;

        if(gameRank >= 0 && gameRank <= 9) {
            xp += gameRank;
            upgraded = updateRank();
        }
        else{
            throw new IllegalArgumentException("A função addGame foi evocada com um valor inferior a 0 ou superior a 9");
        }

        return upgraded;
    }


    /** Verifica se um jogador já tem xp suficiente para avançar para o rank suficiente e, se sim, promove-o.
     *
     * @return True caso tiver subido de rank.
     */
    private boolean updateRank (){
        int xpMin = 0;
        boolean upgraded = false;

        if (rank < 9) {
            for (int i = 0; i <= rank + 1; i++) {
                xpMin += 9 * i;   //Este sumatório identifica o xp mínimo do rank seguinte
            }

            if (xp >= xpMin) {
                rank++;
                upgraded = true;
            }
        }

        return upgraded;
    }

    /**Verifica se as passes entre as duas instâncias de jogador correspondem.
     *
     * @param password Outro jogador.
     * @return True se correspondem.
     */
    public boolean passwordEquals(String password){
        return (this.password.equals(password));
    }

    /** Método de clonagem de um objeto.
     *
     * @return Cópia do objeto.
     */
    public Player clone(){
        return new Player(this);
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
                ", rank=" + rank +
                ", xp=" + xp +
                ", online=" + online +
                '}';
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
