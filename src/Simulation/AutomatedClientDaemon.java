package Simulation;

import Game_Information.lobbyBarrier;
import User_Executables.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AutomatedClientDaemon implements Runnable{
    /**
     * Classe utilizada para a thread de Cliente automatizado que funciona como um listener dos dados que lhe são enviados
     * e re-direciona para o terminal do utilizador
     */
    private BufferedReader is = null;
    private PrintWriter os = null;
    private Socket socket = null;
    /**Para poder manipular o cliente*/
    private AutomatedClient client  = null;

    /**
     * Constructor
     * @param s Socket que o cliente utiliza para comunicar com o servidor
     */
    public AutomatedClientDaemon(Socket s, AutomatedClient c) {
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
            client.addLineToLog("Ficaste em " + (lobbyBarrier.size - xp) + "º lugar. XP: " + xp);
        }
        else {
            throw new Exception("Não foi recebido o código regex suposto 1");
        }

        upgraded = is.readLine().equals("1"); //verifica se houve um upgrade de rank
        if(upgraded) {
            client.addLineToLog("Subiste de rank!");
        }

        rank = Integer.parseInt(is.readLine());
        client.addLineToLog("Rank atual: " + rank);
        xp = Integer.parseInt(is.readLine());
        client.addLineToLog("XP atual: " + xp);
        //END
    }

    @Override
    public  void run() {
        String line = null;

        try {
            while (true){
                line = is.readLine();
                if (line == null) {
                    try {
                        client.addLineToLog("Ligação com o servidor perdida.");
                        client.matchEnded();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                // TODO: LIdar com GAMEOVER 0
                else if (line.equals("&GAMEOVER 1&")) {
                    try {
                        xpHandler(); //Recebe e processa informações de xp do jogo

                        os.println(line);
                        client.matchEnded();
                        client.addLineToLog("O jogo terminou. Escrever \"quit\" para voltar ao menu.");
                        break;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        client.addLineToLog("Não foi recebido um integer como era suposto");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (line.equals("&FOUNDGAME&")) {
                    try {
                        client.foundMatch();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    client.addLineToLog(line);
                }
            }
        }
        catch (IOException | NullPointerException e) {
            client.addLineToLog("Ligação com o servidor perdida.");
        }
        client.addLineToLog("A daemon morreu.");
    }
}
