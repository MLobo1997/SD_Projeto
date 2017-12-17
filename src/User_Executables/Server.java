package User_Executables;

import Game_Information.PlayerAggregator;
import Game_Information.lobbyBarrier;
import Service_Threads.ServerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
        allPlayers      = loadPlayers();
        matchmaker      = new lobbyBarrier();
        server          = new ServerSocket(9999);
    }


    /**
     * Carrega da base de dados a lista de utilizadores que se encontra na diretoria de trabalho
     *
     * @return O registo de jogadores guardados se existir, senão retorna um completamente novo
     */
    public PlayerAggregator loadPlayers() {
        if (new File("../../players.sav").exists()) {
            try {
                FileInputStream saveFile = new FileInputStream("players.sav");
                ObjectInputStream save = new ObjectInputStream(saveFile);
                return ((PlayerAggregator) save.readObject());

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new PlayerAggregator();
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
                new Thread(new ServerThread(socket,allPlayers,matchmaker)).start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String [] args){
        // TODO: Iniciar implementação gráfica
        // TODO: Monitor de servidor (para permitir sair seguramente e só aí guardar registos)
        try {
            Server s = new Server();
            s.runServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
