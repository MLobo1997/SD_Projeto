import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

/** Classe utilizada para registar todos o utilizadores existentes até ao momento.
 *
 */
public class PlayersRegister implements Serializable {
    /** Todas as contas*/
    private HashMap<String, Player> players;

    /** Construtor da classe a utilizar na primeira inicialização do servidor*/
    public PlayersRegister(){
        players = new HashMap<>();
    }

    /** Adicionar jogador à base de dados */
    public synchronized void addPlayer(Player p) {
        if(p.isOnline()){
            System.err.println("Ocorreu o erro de guardar um jogador com info online"); //TODO corrigir este erro (sem ser por por goOffline aqui de preferencia)
        }
        players.put(p.getUsername(),p);
        savePlayersInfo();
    }

    /**
     * Saber qual o jogador associado a dadas credenciais
     * @param username Nome do jogador
     * @return Apontador para o jogador, null se não encontrado
     */
    public synchronized Player getPlayer(String username) {
        return players.get(username);
    }

    /**
     * Numero de jogadores registados
     * @return Número de entradas na hashtable
     */
    public synchronized int size() {
        return players.size();
    }

    /**
     * Guarda informação de todos os jogadores
     */
    public void savePlayersInfo() {
        try  {
            FileOutputStream saveFile = new FileOutputStream("players.sav");
            try (ObjectOutputStream save = new ObjectOutputStream(saveFile)) {
                save.writeObject(this);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Credenciais existem no sistema?
     * @param username Nome que utilizador inseriu
     * @return Resposta: tuplo existe ou não na base de dados?
     */
    public synchronized boolean playerExists(String username) {
        return players.containsKey(username);
    }

    /** Verifica se uma instância de Player tem a password correta.
     *
     * @param username Player a ser verificado.
     * @param pass Password a ser verificada.
     * @return Booleano de verificação.
     */
    public boolean correctPassword(String username, String pass){
        Player p = players.get(username);

        if (p != null) {
            return p.passwordEquals(pass);
        }
        else {
           throw new IllegalArgumentException("O jogador não existe no sistema!!");
        }
    }
}
