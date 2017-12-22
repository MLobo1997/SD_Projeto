package Game_Information;

import Service_Threads.Match;
import Service_Threads.ServerThread;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Barreira encarregue de fazer matchmaking (associar a thread a jogo com jogadores de rank semelhante e
 * fazer a thread esperar até haver condições para começar um jogo).
 */
public class lobbyBarrier {
    /** Contentor de todas as threads à espera de começar a jogar */
    private List<TreeSet<ServerThread>> playersWaiting;
    /** Estrutura que permite saber quantos jogadores se encontram em cada entrada de playersWaiting.*/
    private int[] playersEntering;
    /** Época atual do jogo */ //TODO explicar melhor isto que o lobo não percebe
    private int[] gameEpoch;
    /** Número de ranks */
    private static final int rankNum = 10;
    /** Número de jogadores por jogo.*/
    public static final int size = 3; // TODO: Valor temporário, mudar
    /** Coleção de threads dedicadas a jogadores.*/
    private HashSet<ServerThread> players;
    /** Lock associado à condição do lobby i */
    private ReentrantLock[] lockLobbies;
    /** Condition i -> lobby i está disponível para entrar? */
    private Condition[] conditionLobbiesAvailable;


    /**
     * Construtor
     */
    public lobbyBarrier() {
        playersEntering = new int[rankNum];

        /* indice 0: Pessoas de rank 0
         * indice 1: Pessoas de rank 1
         * etc*/
        playersWaiting = new ArrayList<>();

        lockLobbies = new ReentrantLock[rankNum];
        conditionLobbiesAvailable = new Condition[rankNum];
        gameEpoch = new int[rankNum];


        // inicializar todos os TreeSets da lista
        for (int i = 0; i < rankNum; i++) {
            playersWaiting.add(i,new TreeSet<>());
            lockLobbies[i] = new ReentrantLock();
            conditionLobbiesAvailable[i] = lockLobbies[i].newCondition();
            gameEpoch[i] = 0;
            playersEntering[i] = 0;
        }
        players = new HashSet<>();
    }

    /**
     * Adicionar a todos os jogadores à espera de um certo jogo o objecto Match, para terem agora associado
     * um jogo concreto
     *
     * @param playerThreads Lobby de nrPlayers jogadores
     * @param match Objecto que será variável partilhada de todas as threads
     */
    public void informThreadsOfAddedMatch(TreeSet<ServerThread> playerThreads, Match match) {
        for (ServerThread st : playerThreads) {
            st.associateMatch(match);
        }
    }

    public void printPlayersInLobby(TreeSet<ServerThread> playerThreads) {
        for (ServerThread st : playerThreads) {
            System.out.println(st.getPlayer());
        }
    }

    /**
     * Manter thread de jogador a dormir até as condições necessárias ocorrerem para esta poder começar a jogar //TODO este método atualmente faz mt mais que isto
     * @param st thread que presta serviços ao cliente
     */
    public void waitGame(ServerThread st) {
        System.out.println("Entrou para a lista de espera o jogador " + st.getPlayer().getUsername());
        // TODO: Implementar distribuição normal pelas salas disponíveis talvez para reduzir espera?
        Player player = st.getPlayer();

        int lobbyIndex = player.getRank();
        // Em que instância de um certo lobby vou estar?
        int myEpoch = gameEpoch[lobbyIndex];

        try {
            // Locks acedidos quando são escritas variáveis partilhadas
            lockLobbies(lobbyIndex);

            //Mais um jogador
            playersWaiting.get(lobbyIndex).add(st);
            playersEntering[lobbyIndex]++; //TODO: Como se vai mudar a implementação para ir buscar também aos vizinhos

            System.out.println("Lobby de rank " + player.getRank() +  " já tem " + playersEntering[lobbyIndex] + " jogadores.");
            // Já posso começar o jogo?
            if (playersEntering[lobbyIndex] == size) { // TODO: Colocar aqui o cenas dos ranks
                startMatch(st);
            }

            printPlayersInLobby(playersWaiting.get(lobbyIndex));

            try {

                while (myEpoch == gameEpoch[lobbyIndex]) { //TODO : Que é que isto faz exatamente (comentem que eu não tenho a certeza pls)
                    lobbiesAvailableAwait(lobbyIndex, player);
                }
            } catch (InterruptedException e) {
                st.cleanup();
            }
        }
        finally { //desbloqueia tudo
            unlockLobbies(lobbyIndex);
        }

    }

    /** Inicializa um lobby (match) com os 'size' jogadores.
     *
     * @param st Server thread do jogador que se está a juntar
     */
    private void startMatch(ServerThread st){
        System.out.println("A iniciar um jogo.");

        Player player = st.getPlayer();

        int lobbyIndex = player.getRank();


        Match match = new Match((TreeSet<ServerThread>) playersWaiting.get(lobbyIndex).clone(), size, new ReentrantLock());
        informThreadsOfAddedMatch(playersWaiting.get(lobbyIndex), match);

        new Thread(match).start();

        gameEpoch[lobbyIndex]++;
        conditionLobbiesAvailable[lobbyIndex].signalAll(); //TODO será necessário notificar os dos outros ranks.
        playersEntering[lobbyIndex] = 0;
        playersWaiting.get(lobbyIndex).clear();
    }

    /** Tenta encontrar 'size' jogadores no lobbie pedido em conjunto com o do seu rank anterior ou seguinte (exclusivamente).
     *
     * @param lobbyIndex Identificador de lobby
     * @return Estrutura com os 'size' jogadores encontrados ou então nenhum jogador, no caso de não conseguido arranjar 'size'.
     */
    private TreeSet<ServerThread> findMatchPlayers(int lobbyIndex){
        int nsize;
        TreeSet<ServerThread> tmp;
        TreeSet<ServerThread> r = new TreeSet<>(playersWaiting.get(lobbyIndex));

        nsize = size - r.size(); //Vê se o rank atual já tem jogadores suficientes
        if (nsize == 0){ //senão tiver, vê se com o anterior já tem
            tmp = getNPlayers(lobbyIndex - 1, nsize);

            nsize = size - r.size() - tmp.size();

            if (nsize == 0) { //Se tiver, adiciona
                r.addAll(tmp);
            }
            else { //senão, tenta ver se o rank seguinte tem
                tmp = getNPlayers(lobbyIndex + 1, nsize);

                nsize = size - r.size() - tmp.size();

                if (nsize == 0) { //se tiver, adiciona
                    r.addAll(tmp);
                }
                else { //senão limpa o r para não retornar nenhum jogador de todo
                    r.clear();
                }
            }
        }

        return r;
    }

    /** Adiciona um número específico de jogadores de um determinado rank de espera a um TreeSet
     *
     * @param lobbyIndex Rank de espera.
     * @param N Número de jogadores a serem adicionados.
     */
    private TreeSet<ServerThread> getNPlayers(int lobbyIndex, int N){
        int i = 0;
        TreeSet<ServerThread> t = new TreeSet<>();

        if (lobbyIndex >= 0 && lobbyIndex < rankNum) {
            for (ServerThread st : playersWaiting.get(lobbyIndex)) { //para poder extrair apenas n

                if (i < N) {
                    break;   //acaba o ciclo quando já encontramos o número de jogadores necessário
                }

                t.add(st);
                i++;
            }
        }
        return t;
    }

    /** Método que bloqueia um o lock de uma threads e o das suas vizinhas imediatas.
     *
     * @param lobbyIndex Índice do lobby a ser trancado.
     */
    private void lockLobbies(int lobbyIndex){
        if (lobbyIndex - 1 >= 0 ) { //é necessário bloquear para o caso de termos de ir buscar jogadores do rank imediatamente inferior
            lockLobbies[lobbyIndex - 1].lock();
        }

        lockLobbies[lobbyIndex].lock();

        if (lobbyIndex + 1 < rankNum) { //é necessário bloquear para o caso de termos de ir buscar jogadores do rank imediatamente superior
            lockLobbies[lobbyIndex + 1].lock();
        }
    }

    /** Método que desbloqueia um o lock de uma threads e o das suas vizinhas imediatas.
     *
     * @param lobbyIndex Índice do lobby a ser desbloqueado.
     */
    private void unlockLobbies(int lobbyIndex){
        if (lobbyIndex - 1 >= 0  && lockLobbies[lobbyIndex - 1].isLocked()) {
            lockLobbies[lobbyIndex - 1].unlock();
        }

        lockLobbies[lobbyIndex].unlock();

        if (lobbyIndex + 1 < rankNum && lockLobbies[lobbyIndex + 1].isLocked()) {
            lockLobbies[lobbyIndex + 1].unlock();
        }
    }

    /** Faz o wait da condição do lobby estar livre.
     *
     * @param lobbyIndex Índice do lobby.
     * @throws InterruptedException No caso do await lançar a exceção.
     */
    private void lobbiesAvailableAwait(int lobbyIndex, Player p) throws InterruptedException{
        if (lobbyIndex - 1 >= 0  && lockLobbies[lobbyIndex - 1].isLocked()) {
            lockLobbies[lobbyIndex - 1].unlock();
        }

        if (lobbyIndex + 1 < rankNum && lockLobbies[lobbyIndex + 1].isLocked()) {
            lockLobbies[lobbyIndex + 1].unlock();
        }

        conditionLobbiesAvailable[lobbyIndex].await();

    }
}
