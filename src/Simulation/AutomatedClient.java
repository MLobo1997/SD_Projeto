package Simulation;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

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
    /** Instante em que foi iniciado o cliente*/
    private LocalDateTime startTime;
    /**Identifica se o cliente se encontra no processo de matching.*/
    private boolean matchNotEnded;
    /**Identifica se o cliente se encontra num match*/
    private boolean inMatch;
    /**Thread do daemon*/
    private Thread daemon;

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
        this.matchNotEnded = false;
        this.inMatch = false;
    }

    /** Getter de username.
     *
     * @return Username.
     */
    public String getUsername() {
        return username;
    }

    /** Faz uma espera de tempo aleatório com limite.
     *
     * @param maxSeconds Limite, em segundos.
     */
    public void waitUntil (int maxSeconds) {
        int waitTime = (int) (ThreadLocalRandom.current().nextDouble(maxSeconds) * 1000);

        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            daemon.interrupt();
            addLineToLog("---Fui morto forçosamente---");
            printLog();
            System.exit(0);
        }
    }


    /** Modifica o cliente para um estado de fora de matching.
     *
     * @throws Exception No caso de se tentar usar o método quando não se encontra em matching.
     */
    void matchEnded() throws Exception{
        if (matchNotEnded) {
            matchNotEnded = false;
        }
        else {
            throw new Exception("Foi invocado o método notInMatch quando o cliente não estava em match");
        }
    }

    /** Identifica que o cliente se encontra atualmente num match.
     *
     * @throws Exception Se por algum motivo esta variável já se encontrar em true.
     */
    public void foundMatch() throws Exception{
        if (!inMatch) {
            inMatch = true;
        }
        else {
            throw new Exception("Foi invocado o método gameEnded quando o cliente não estava em match");
        }
    }

    /** Tenta realizar o registo de um jogador automatizado. TODO:Adicionar capacidade de no caso de já estar registado saltar para o login
     *
     */
    private void tryToRegister(){
        boolean alreadyExists = false;

        addLineToLog("---A tentar fazer registo.---");
        out.println(username); //dizer ao servidor o username
        out.println(password);//dizer ao servidor a password

        try {

            alreadyExists = in.readLine().equals("0");
            if(alreadyExists) {
                out.println("-1"); //comunica ao servidor que é para saltar para o login
                addLineToLog("O utilizador já existia.");
            }
            else {
                out.println("1"); //confirma ao servidor para finalizar o registo.
                addLineToLog("O utilizador foi registado com sucesso");
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
        addLineToLog("---A fazer login---");

        try {
            out.println(username); //dizer ao servidor o username
            out.println(password);//dizer ao servidor a password

            str = in.readLine();
            switch (str) {
                case "1":
                    addLineToLog("O utilizador fez o login com sucesso.");
                    break;
                case "0":
                    addLineToLog("O utilizador não fez o login pois não está registado.");
                    System.err.println("Tried to login the following user who doesn't exist: " + username);
                    break;
                case "-1":
                    addLineToLog("O utilizador não fez o login pois não a password estava incorreta.");
                    System.err.println("Tried to login the following whose password is incorrect: " + username);
                    break;
                case "-2":
                    addLineToLog("O utilizador não fez o login já se encontrava online.");
                    System.err.println("Tried to login the following who is already online: " + username);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**Método que regista e faz login dos users ao sistema.*/
    private void connectUser(){
        startTime = LocalDateTime.now();
        addLineToLog("-------- " + startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " --------");

        out.println("0"); //dizer ao servidor que é para fazer registo
        tryToRegister();
        loginUser();
    }

    /** Termina a sessão.
     *
     */
    private void disconnectUser (){
        addLineToLog("---Utilizador a desconectar---");
        out.println("0"); //para fazer logout
        printLog();
        try {
            in.close();
            out.close();
            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error closing connections");
        }
    }

    /** Escreve tudo o que se encontra na variável log num ficheiro.
     *
     */
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

    /** Notifica o cliente que tem de se desconectar.
     *
     */
    void signalToLeave() {
        leave = true;
    }

    /**
     *
     */
    private void findMatch() {
        addLineToLog("---Iniciou a procura de um jogo---");
        matchNotEnded = true;
        inMatch = false;
        int hero;

        while (matchNotEnded) {
            hero = ThreadLocalRandom.current().nextInt(30);
            waitUntil(15); //SE SE TIRAR ESTE MÉTODO DAQUI TEM QUE SE COLOCAR O QUE ESTÁ NO CATCH NO SEU INTERIOR
            if (inMatch) {
                out.println("/c" + hero);
            }
        }
    }

    /** Coloca o cliente num ciclo de jogos.
     *
     */
    private void play () {
        addLineToLog("---O utilizador entrou no ciclo de jogos---");

        while (true) {
            if (leave) {
                addLineToLog("A sair do modo de procura de jogo");
                out.println("0");
                break;
            }
            else {
                //Cria o daemon igual ao do cliente normal
                daemon = new Thread(new AutomatedClientDaemon(socket, this));
                daemon.start();

                out.println("1");
                findMatch();

                try {
                    daemon.join(); //Espera que morra (supostamente não necessário, mas está aqui para termos a certeza que acontece
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Coloca a mensagem no log.
     *
     * @param line Mensagem.
     */
    void addLineToLog (String line) {
        Duration dur = Duration.between(startTime, LocalDateTime.now());

        StringBuilder strbld = new StringBuilder();

        strbld.append("[");
        strbld.append(String.format("%03d", ((Long) dur.getSeconds())));
        strbld.append(" segundos");
        strbld.append("]: ");
        strbld.append(line);
        strbld.append('\n');

        log.append(strbld);
    }

    public void run() {
        connectUser();
        play();
        disconnectUser();
    }
}
