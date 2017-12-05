import java.net.Socket;
import java.util.HashMap;

/** Classe utilizada para controlo e exploração dos jogadores atualmente online */
public class OnlinePlayers {
    /** Estrutura com os ID's dos jogadores atualmente onlines*/
    private HashMap<Integer, Socket> players;
    /** Número de jogadores atualmente online*/
    private Integer nr; //pensei nisto para depois fazer uma feature do género, estão 1500 jogadores online. Mas entretanto lembrei me que já esta implementado no HashMap
}
