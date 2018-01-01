package User_Executables;

import Service_Threads.ClientDaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    /** Socket a ser usada para a conecção */
    private Socket socket          = null;
    /** Scanner que lê do teclado */
    private BufferedReader scanner = null;
    /** Inputstream gerado da socket */
    private BufferedReader is      = null;
    /** PrintWriter gerado do socket */
    private PrintWriter os         = null;
    /**Identifica se o cliente se encontra no processo de matching.*/
    private boolean matchNotEnded;
    /**Identifica se o cliente se encontra num match*/
    private boolean inMatch;

    /**
     * Constructor
     */
    private Client() {
        try {
            scanner = new BufferedReader(new InputStreamReader(System.in));
            socket  = new Socket("127.0.0.1",9999);
            is      = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os      = new PrintWriter(socket.getOutputStream(),true);
            matchNotEnded = false;
            inMatch = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Faz o registo e/ou login de um utilizador ao sistema.
     *
     */
    private void connectUser(){
        boolean loggedIn = false;
        boolean toReg = true; //true se for para fazer registo false se for para fazer login
        String tmp;

        try {
            do {
                System.out.println("Register [0] or login [1]?");
                tmp = scanner.readLine();
            } while (!tmp.equals("0") && !tmp.equals("1"));
            os.println(tmp); //dizer ao servidor que é para fazer registo

            switch (tmp) {
                case "0":
                    toReg = true;
                    break;
                case "1":
                    toReg = false;
                    break;
            }

            while (!loggedIn) {
                if (toReg) {
                    registerPlayer();
                    loggedIn = loginPlayer();
                } else {
                    loggedIn = loginPlayer();
                    if(!loggedIn){
                        toReg = true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica se num registo é para avançar para o login e comunica essa decisão para o servidor.
     *
     * @return Booleano com resultado, true se for para avançar.
     * @throws IOException
     */
    private boolean checkIfSkip() throws IOException{
        String tmp = null;
        boolean skip = false;

        do {
            if (tmp != null)
                System.out.println("[y/n]");
            tmp = scanner.readLine();
            switch (tmp) {
                case "y":
                    skip = true;
                    os.println("-1"); //comunica ao servidor que é para saltar. TODO substituir para 1

                    break;
                case "n":
                    os.println("0"); //comunica ao servidor que é para tentar outra vez
                    break;
            }
        } while (!tmp.equals("y") && !tmp.equals("n")); //repete o confirma no caso de não ter recebido 'y' nem 'n'

        return skip;
    }

    /**
     * Faz o registo de um jogador no sistema.
     */
    private void registerPlayer() {
        String username = null , password, tmp;
        Boolean check = false, skip = false, alreadyRegistered ;

        try {
            while (!check && !skip) {
                System.out.println("----Registe o jogador---");
                do { // while até ser utilizado um username que ainda não foi registado
                    //begin username
                    System.out.println("Username:");
                    username = scanner.readLine();
                    os.println(username);

                    //begin password
                    System.out.println("Password:");
                    password = scanner.readLine();
                    os.println(password);

                    tmp = is.readLine();
                    alreadyRegistered = tmp.equals("0"); //verifica se já não existe

                    if(alreadyRegistered){ //se já existe, pergunta se quer avançar para o login
                        System.out.println("O username " + username + " já se encontra registado. Deseja mudar para login? [y/n]");
                        skip = checkIfSkip();
                    }
                } while (alreadyRegistered && !skip);

                if (tmp.equals("1")) { //No caso de tudo funcionar direito
                    do {
                        System.out.println("Confirma [y/n]");
                        tmp = scanner.readLine();
                        if (tmp.equals("y")) {
                            check = true;
                            os.println("1"); //transmite o 'yes' TODO UTILIZAR CheckIfSkip
                        } else if (tmp.equals("n")) {
                            os.println("0"); //transmite o 'no'
                        }
                    } while (!tmp.equals("y") && !tmp.equals("n")); //repete o confirma no caso de não ter recebido 'y' nem 'n'
                }

                if(!check && !skip){ //No caso de algo tiver dado para o torto, pergunta se não quer avançar para o login.
                    System.out.println("Deseja mudar para login? [y/n]");
                    skip = checkIfSkip();
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        if (check) {
            System.out.println("Registado com sucesso!");
        }
        else if(skip){
            System.out.println("A avançar para o login");
        }
    }

    /**
     * Faz o login dum cliente no sistema.
     *
     * @return Informação do cliente.
     */
    private boolean loginPlayer(){
        String username, password, tmp = null, response = null;
        boolean check = false, skip = false;

        try {
            while(!check && !skip){
                System.out.println("---Login---");

                System.out.println("Username:");
                username = scanner.readLine();
                os.println(username);

                System.out.println("Password:");
                password = scanner.readLine();
                os.println(password);

                response = is.readLine();
                switch (response) {
                    case "0":
                        System.out.println("O utilizador não existe!!");
                        break;
                    case "-1":
                        System.out.println("A password está errada!");
                        break;
                    case "-2":
                        System.out.println("O utilizador já se encontra online!");
                        break;
                    case "1":
                        System.out.println("Login sucedido!");
                        check = true;
                        break;
                }

                if(!check){
                    System.out.println("Queres fazer um registo? [y/n]");
                    skip = checkIfSkip();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return check;
    }

    /**
     * Desconecta o utilizador localmente.
     */
    private void disconnectUser (){

        System.out.println("Desligando do sistema.");

        try {
            is.close();
            os.close();
            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error closing connections");
        }
    }

    /**
     * Ler informação do cliente necessária para inicializar jogo
     */
    private void findMatch() {
        matchNotEnded = true;
        try {
            String lineInput = scanner.readLine();
            while (matchNotEnded || !(lineInput.equals("quit") || lineInput.equals("q"))) {
                if (inMatch) {
                    os.println(lineInput);
                }
                else
                    System.out.println("Not in match");
                lineInput = scanner.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while communicating with server");
        }
    }

    /**
     * Inicia o menu do jogo após o login e permite ao jogador escolher o que quer fazer a seguir (e.g.: jogar, sair do programa, outras cenas que podemos inventar)
     *
     * @return Valor que identifica a opção do jogador (-1 -> ocorreu um erro ; 0 -> desistir; 1 -> jogar)
     */
    private int startMenu(){
        String cmd;

        try {

            do {
                System.out.println("O que queres fazer?");
                System.out.println("Jogar [p], sair [q]?:");

                cmd = scanner.readLine();

                if (cmd.equals("q")){
                    os.println("0");
                    return 0;
                }
                else if (cmd.equals("p")){
                    inMatch = false;
                    os.println("1");
                    return 1;
                }

            } while (!cmd.equals("p") && !cmd.equals("q")); // se o valor inserido não for válido, repete o processo

        } catch (IOException e) {
            e.printStackTrace();
        }

        os.println("-1"); //Avisa o servidor do erro.
        return -1; //Se chegar aqui é porque algo correu mal.
    }

    /**
     * Método que faz o processamento após o login.
     */
    private void init(){
        int cmd;
        ClientDaemon daemon;
        Thread t;

        do {
            cmd = startMenu();

            if (cmd == 1){
                System.out.println("À procura de jogo...");
                // Criar daemon thread que faz redireciona qualquer mensagem deste cliente para o seu input (listener)
                daemon = new ClientDaemon(socket, this);
                t = new Thread(daemon);
                t.start();

                findMatch();

                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (cmd != 0);
        disconnectUser();
    }

    /** Modifica o cliente para um estado de fora de matching.
     *
     * @throws Exception No caso de se tentar usar o método quando não se encontra em matching.
     */
    public void matchEnded() throws Exception{
        if (matchNotEnded) {
            matchNotEnded = false;
        }
        else {
            throw new Exception("Foi invocado o método gameEnded quando o cliente não estava em match");
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
            throw new Exception("Foi invocado o método foundMatch quando o cliente estava num match");
        }
    }

    public static void main(String args[]){

        Client client = new Client();

        client.connectUser();

        client.init();


        System.exit(0);

    }
}

