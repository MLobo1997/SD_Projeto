package Simulation;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    /** Conjunto de threads dos clientes a ser corridos, mapeadas pelos usernames dos respetivos clientes.*/
    private HashSet<Thread> clients;

    /**
     * Construtor da classe Simulation.ClientGenerator.
     */
    private ClientGenerator(){
        scanner = new BufferedReader(new InputStreamReader(System.in));
        N = initSetN();
        clients = new HashSet<>();
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
        clients.add(t);
        t.run();
    }

    /**
     * Método utilizado para fazer parse da informação dos jogadores guardada num ficheiro csv.
     *
     * @return Inicialização da variável players.
     */
    private TreeMap<String, String> loadPlayersInfo() {
        String[] line;
        TreeMap<String, String> r = new TreeMap<>();

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader("randomUsers.csv"));

            for (int i = 0 ; i < N && (line = reader.readNext()) != null ; i++){
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

    public static void main (String [] args){
        ClientGenerator cg = new ClientGenerator();

        for(Thread t : cg.clients){
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
