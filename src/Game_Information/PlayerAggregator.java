package Game_Information;

import java.io.*;
import java.util.HashMap;

/**
 * Classe utilizada para registar todos o utilizadores existentes até ao momento.
 */
public class PlayerAggregator implements Serializable {
    /** Coleção de todos os jogadores registados no programa, chave é o nome do jogador */
    private HashMap<String, Player> players;

    /**
     * Construtor
     */
    public PlayerAggregator(String file){
        players = loadPlayers(file);
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
     * Carrega da base de dados a lista de utilizadores que se encontra na diretoria de trabalho
     *
     * @return O registo de jogadores guardados se existir, senão retorna um completamente novo
     */
    public HashMap<String,Player> loadPlayers(String file) {
        if (new File("../../" + file).exists()) {
            try {
                FileInputStream saveFile = new FileInputStream("players.sav");
                ObjectInputStream save = new ObjectInputStream(saveFile);
                return (((PlayerAggregator) save.readObject()).players);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<String,Player>();
    }

    /**
     * Credenciais existem no sistema?
     * @param username Nome que utilizador inseriu
     * @return Resposta: tuplo existe ou não na base de dados?
     */
    public synchronized boolean playerExists(String username) {
        return players.containsKey(username);
    }

    /** Verifica se uma instância de Game_Information.Player tem a password correta.
     *
     * @param username Game_Information.Player a ser verificado.
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
