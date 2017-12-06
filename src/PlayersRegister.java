import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** Classe utilizada para registar todos o utilizadores existentes até ao momento.
 *
 */
public class PlayersRegister implements Serializable {
    /** Todas as contas*/
    private HashMap<Integer, Player> players;

    /** Construtor da classe a utilizar na primeira inicialização do servidor*/
    public PlayersRegister(){
        players = new HashMap<>();
    }

    /** Adicionar jogador à base de dados */
    public void addPlayer(Player p) {
        players.put(p.getID(),p);
        savePlayersInfo();
    }

    /**
     * Saber qual o jogador associado a dadas credenciais
     * @param username Nome do jogador
     * @param password Password do jogador
     * @return Apontador para o jogador, null se não encontrado
     */
    public Player getPlayer(String username,String password) {
        Player currPlayer; // procura atual
        for (Map.Entry<Integer,Player> entry : players.entrySet()) {
            currPlayer =  entry.getValue();
            if ( (currPlayer.getUsername().equals(username)) && (currPlayer.getPassword().equals(password)) ) {
               return currPlayer;
            }
        }
        // Procura falhou, nada encontrado
        return null;

    }

    /** Número de jogadores registados */
    public int size() {
        return players.size();
    }

    /** Guardar informação de todos os utilizadores */
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

    /** Credenciais candidatas existem no sistema? */
    public boolean playerExists(String username,String password) {
        Player currPlayer; // procura atual
        for (Map.Entry<Integer,Player> entry : players.entrySet()) {
            currPlayer = entry.getValue();
            if ( (currPlayer.getUsername().equals(username)) && (currPlayer.getPassword().equals(password)) ) {
                return true;
            }
        }
        // Falhou encontrar um utilizador com esses valores
        return false;
    }
}
