package Simulation;

import com.opencsv.CSVReader;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * Classe do programa que gera clientes que atuam aleatóriamente para fins de teste do servidor.
 */
public class ClientGenerator {
    /** Número de bots a clientes a serem gerados. */
    private Integer N;
    /** Variável para permitir fazer reads de input mais facilmente*/
    private BufferedReader scanner;
    /** Conjunto de threads dos clientes a ser corridos, mapeados por username*/
    private TreeMap<String, Thread> threads;
    /** Conjunto de clientes a ser corridos, mapeados por username*/
    private TreeMap <String, AutomatedClient> clients;
    /** Leitor de informações de usuário*/
    private CSVReader userReader;

    /**
     * Construtor da classe Simulation.ClientGenerator.
     */
    private ClientGenerator(){
        scanner = new BufferedReader(new InputStreamReader(System.in));
        N = initSetN();
        threads = new TreeMap<>();
        clients = new TreeMap<>();
        try {
            userReader = new CSVReader(new FileReader("randomUsers.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        runClients(loadPlayersInfo());
    }

    /**
     * Corre todos os clientes cuja informação de jogadores se encontra no map.
     *
     * @param players Map de informação de jogadores.
     */
    private void runClients(Map<String, String> players){
        players.forEach(this::initThread);
    }


    /**
     * Cria e corre uma thread de cliente.
     *
     * @param user Username do jogador.
     * @param pass Password do jogador.
     */
    private void initThread(String user, String pass){
        AutomatedClient c = new AutomatedClient(user, pass);
        Thread t = new Thread(c);
        threads.put(user, t);
        clients.put(user, c);
        t.start();
    }

    /**
     * Método utilizado para fazer parse da informação dos jogadores guardada num ficheiro csv.
     *
     * @return Inicialização da variável players.
     */
    private TreeMap<String, String> loadPlayersInfo() {
        String[] line;
        TreeMap<String, String> r = new TreeMap<>();

        try {

            for (int i = 0 ; i < N && (line = userReader.readNext()) != null ; i++){
                r.put(line[0], line[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return r;
    }

    /**
     * Método de inicialização da variável N, questionando ao utilizador qual o seu valor.
     *
     * @return N.
     */
    private Integer initSetN(){
        boolean repeat = false;
        Integer n = 0;

        do {
            try {
                System.out.println("Quantos bots quer gerar?");
                n = Integer.parseInt(scanner.readLine());
                repeat = false;

            } catch (NumberFormatException e1) {
                System.out.println("O input não é um integer válido!");
                repeat = true;

            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } while (repeat);
        return n;
    }

    private void removeClient () {
        Thread t = threads.pollFirstEntry().getValue();
        AutomatedClient c = clients.pollFirstEntry().getValue();
        N--;

        c.signalToLeave();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void menu () {
        String cmd;

        try {

            do {
                System.out.println("[+] para adicionar mais um cliente, [-] para desconectar e [q] para matar o gerador.");
                cmd = scanner.readLine();
                switch (cmd) {
                    case "+":
                        if (N < 1000) {

                        }
                        else {
                            System.out.println("Já todos os utilizadors foram utilizados.");
                        }
                        break;
                    case "-":
                        if (N > 0) {
                            removeClient();
                        }
                        else {
                            System.out.println("Já não há clientes a correr.");
                        }
                        break;
                }
            } while (!cmd.equals("q"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("A sair.");
    }

    /** Mata e espera que morram todos os clientes.
     *
     */
    private void killAllClients () {
        for (AutomatedClient c : clients.values()){
            c.signalToLeave();
        }

        for (Thread t : threads.values()) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main (String [] args){
        ClientGenerator cg = new ClientGenerator();

        cg.menu();

        cg.killAllClients();
    }
}
