package Simulation;

import java.io.*;
import java.net.Socket;

/**
 * Classe runnable dos cliente gerados automáticamente.
 */
public class AutomatedClient implements Runnable {
    /** Username com que o cliente vai fazer login*/
    private String username;
    /** Password com que o cliente vai fazer login*/
    private String password;
    /** Output stream de comunição com o servidor*/
    private PrintWriter out;
    /** Input stream de comunição com o servidor*/
    private BufferedReader in;
    /** Socket de comunicação com o servidor*/
    private Socket socket;
    /** Variável que define se é suposto o cliente fazer log out.*/
    private Boolean leave;
    /**Utilizado para gerar a string que no final da execução será registada em ficheiro para demonstrar o output do client automatizado*/
    private StringBuilder log;

    /** Construtor do cliente.
     *
     * @param username Variável username.
     * @param password Variável password.
     */
    public AutomatedClient(String username, String password){
        this.username = username;
        this.password = password;
        try {
            this.socket = new Socket("127.0.0.1", 9999);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.leave = false;
        this.log = new StringBuilder();
    }

    /** Tenta realizar o registo de um jogador automatizado. TODO:Adicionar capacidade de no caso de já estar registado saltar para o login
     *
     */
    private void tryToRegister(){
        boolean alreadyExists = false;

        log.append("A tentar fazer registo.\n");
        out.println(username); //dizer ao servidor o username
        out.println(password);//dizer ao servidor a password

        try {

            alreadyExists = in.readLine().equals("0");
            if(alreadyExists) {
                out.println("-1"); //comunica ao servidor que é para saltar para o login
                log.append("O utilizador já existia.\n");
            }
            else {
                out.println("1"); //confirma ao servidor para finalizar o registo.
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Faz login do cliente.
     *
     */
    private void loginUser(){
        String str;

        try {
            out.println(username); //dizer ao servidor o username
            out.println(password);//dizer ao servidor a password

            str = in.readLine();
            switch (str) {
                case "1":
                    break;
                case "0":
                    System.err.println("Tried to login the following user who doesn't exist: " + username);
                    break;
                case "-1":
                    System.err.println("Tried to login the following whose password is incorrect: " + username);
                    break;
                case "-2":
                    System.err.println("Tried to login the following who is already online: " + username);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**Método que regista e faz login dos users ao sistema.*/
    private void connectUser(){
        out.println("0"); //dizer ao servidor que é para fazer registo
        tryToRegister();
        loginUser();
    }

    /** Desconecta o utilizador localmente.
     *
     */
    private void disconnectUser (){
        try {
            in.close();
            out.close();
            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error closing connections");
        }
    }

    private void printLog () {
        FileWriter fw;
        File dir = new File("botLogs");

        try {
            dir.mkdir(); //cria a diretoria

            fw = new FileWriter("botLogs/" + username + ".log");
            fw.write(log.toString());
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        connectUser();
        out.println("0"); //para fazer logout PROVISORIO
        printLog();
        disconnectUser();
    }
}
