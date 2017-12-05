import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** A classe principal do servidor.
 *
 */
public class Server {
    /** Registo de todas as contas. */
    private final PlayersRegister allPlayers;
    /** Equipas já formadas que estão em processo de início de jogo*/
    //private /*final*/ Lobbies currentLobbies;
    private final ServerSocket server;
    /** Estrutura com todos os jogadores que estão ligados ao servidor no momento. */
    private /*final*/ OnlinePlayers onlinePlayers;
    /** Jogadores que estão atualmente à procura de um jogo*/
    private /*final*/ MatchingPlayers matchingPlayers;

    public Server() throws IOException{
        this.allPlayers = new PlayersRegister();
        this.onlinePlayers = new OnlinePlayers();
        this.matchingPlayers = new MatchingPlayers();
       //lobby aqui
        this.server = new ServerSocket(9999);
    }

    public static void main(String [] args){
    }

    public void runServer (){
        Socket socket;

        while (true) {
            //CONNECT user
            try {
                socket = this.server.accept();
            }
            catch (IOException e) {
               e.printStackTrace();
            }
            //
        }
    }
}
