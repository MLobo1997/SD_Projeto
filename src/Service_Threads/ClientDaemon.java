package Service_Threads;

import Game_Information.lobbyBarrier;
import User_Executables.Client;
import com.sun.tools.corba.se.idl.StringGen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Classe utilizada para a thread de Cliente que funciona como um listener dos dados que lhe são enviados
 * e re-direciona para o terminal do utilizador
 */
public class ClientDaemon implements Runnable {
    private BufferedReader is = null;
    private PrintWriter os = null;
    private Socket socket = null;
    /**Para poder manipular o cliente*/
    private Client client = null;

    /**
     * Constructor
     * @param s Socket que o cliente utiliza para comunicar com o servidor
     */
    public ClientDaemon(Socket s, Client c) {
        socket = s;

        try {
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        client = c;
    }

    /** Recebe e processa informações de xp do jogo
     *
     * @throws Exception Para o readline
     */
    private void xpHandler() throws Exception {
        String xpString;
        Integer xp, rank;
        Boolean upgraded;

        //BEGIN receção de xp
        xpString = is.readLine();
        if (xpString.matches("&xp:\\d&")){
            xp = Integer.parseInt(xpString.substring(4, 5));
            System.out.println("Ficaste em " + (lobbyBarrier.size - xp) + "º lugar. XP: " + xp);
        }
        else {
            throw new Exception("Não foi recebido o código regex suposto 1");
        }

        upgraded = is.readLine().equals("1"); //verifica se houve um upgrade de rank
        if(upgraded) {
            System.out.println("Subiste de rank!");
        }

        rank = Integer.parseInt(is.readLine());
        System.out.println("Rank atual: " + rank);
        xp = Integer.parseInt(is.readLine());
        System.out.println("XP atual: " + xp);
        //END
    }

    @Override
    public  void run() {
        String line = null;

        try {
            while (true){
                line = is.readLine();
                if (line == null) {
                    System.out.println("Ligação com o servidor perdida.");
                    is.close();
                    socket.close();
                    break;
                }
                else if (line.equals("&GAMEOVER&")) {
                    try {
                        xpHandler(); //Recebe e processa informações de xp do jogo

                        os.println(line);
                        client.notInMatch();
                        System.out.println("O jogo terminou. Escrever \"quit\" para voltar ao menu.");
                        break;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        System.err.println("Não foi recebido um integer como era suposto");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println(line);
                }
            }
        }
        catch (IOException | NullPointerException e) {
            System.out.println("Ligação com o servidor perdida.");
        }
    }
}

