import java.io.*;
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
        this.allPlayers = loadPlayers();
        this.onlinePlayers = new OnlinePlayers();
        this.matchingPlayers = new MatchingPlayers();
        //lobby aqui
        this.server = new ServerSocket(9999);
    }

    public static void main(String [] args){
        try {
            Server s = new Server();
            s.runServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayersRegister loadPlayers() {
        if (new File("players.sav").exists()) {
            try {
                FileInputStream saveFile = new FileInputStream("players.sav");
                ObjectInputStream save = new ObjectInputStream(saveFile);
                return ((PlayersRegister) save.readObject());

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new PlayersRegister();
    }

    public void runServer (){
        Socket socket;

        while (true) {
            //CONNECT user
            try {
                socket = server.accept();
                /* Iniciar novo prestador de serviços para cliente */
                new ServerThread(socket,allPlayers).start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
