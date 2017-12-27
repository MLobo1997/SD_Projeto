package User_Executables;

import Game_Information.PlayerAggregator;
import Game_Information.lobbyBarrier;
import Service_Threads.ServerThread;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A classe principal do servidor.
 */
public class Server {
    /** Registo de todas as contas. */
    private final PlayerAggregator allPlayers;
    /** Equipas já formadas que estão em processo de início de jogo*/
    private final ServerSocket server;
    /** Barreira dinâmica que aloca jogadores e faz correspondentes threads esperar até match ser encontrado */
    private lobbyBarrier matchmaker;

    /** Construtor do servidor.
     *
     * @throws IOException No método de contrução dum ServerSocket.
     */
    public Server() throws IOException{
        allPlayers      = new PlayerAggregator("players.sav");
        matchmaker      = new lobbyBarrier();
        server          = new ServerSocket(9999);
    }

    /**
     * Loop continuo que aceita clientes que se ligam ao servidor e aloca-os os devidos recursos
     */
    public void runServer (){
        Socket socket;

        while (true) {
            // conectar utilizador
            try {
                socket = server.accept();
                /* Iniciar novo prestador de serviços para cliente */
                ServerThread serverThread = new ServerThread(socket,allPlayers,matchmaker);
                new Thread(serverThread).start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String [] args){
        try {
            Server s = new Server();
            System.out.print("[");
            s.allPlayers.getPlayers().forEach((key,value) -> System.out.print("(Player:"+value.getUsername()+",pswrd:"+value.getPassword()+",rank:"+value.getRank()+")"));
            System.out.println("]");
            s.runServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
