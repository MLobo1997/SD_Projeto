import java.util.TreeMap;

/** Classe utilizada para registar todos o utilizadores existentes até ao momento.
 *
 */
public class PlayersRegister {
    /** Número de contas. */
    private Integer nr;
    /** Todas as contas*/
    private TreeMap<Integer, Player> players;

   /** Construtor da classe a utilizar na primeira inicialização do servidor*/
   public PlayersRegister(){
       nr = 0;
       players = new TreeMap<>();
   }
}
