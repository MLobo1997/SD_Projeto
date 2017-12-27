package Game_Information;

import Service_Threads.Match;
import Service_Threads.ServerThread;
import User_Executables.Server;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Barreira encarregue de fazer matchmaking (associar a thread a jogo com jogadores de rank semelhante e
 * fazer a thread esperar até haver condições para começar um jogo).
 */
public class lobbyBarrier {
    /** Contentor de todas as threads à espera de começar a jogar */
    private List<TreeSet<ServerThread>> playersWaiting;
    ///** Estrutura que permite saber quantos jogadores se encontram em cada entrada de playersWaiting.*/
    //private int[] playersEntering;
    ///** Época atual do jogo */ //TODO explicar melhor isto que o lobo não percebe
    //private int[] gameEpoch;
    /** Número de ranks */
    private static final int rankNum = 10;
    /** Número de jogadores por jogo.*/
    private static final int size = 3; // TODO: Valor temporário, mudar
    ///** Coleção de threads dedicadas a jogadores.*/
    //private HashSet<ServerThread> players;
    /** Lock associado à condição do lobby i */
    private ReentrantLock[] lockLobbies;
    /** Condition i -> lobby i está disponível para entrar? */
    private Condition[] conditionLobbiesAvailable;


    /**
     * Construtor
     */
    public lobbyBarrier() {
        //playersEntering = new int[rankNum];

        /* indice 0: Pessoas de rank 0
         * indice 1: Pessoas de rank 1
         * etc*/
        playersWaiting = new ArrayList<>();

        lockLobbies = new ReentrantLock[rankNum];
        conditionLobbiesAvailable = new Condition[rankNum];
        //gameEpoch = new int[rankNum];


        // inicializar todos os TreeSets da lista
        for (int i = 0; i < rankNum; i++) {
            playersWaiting.add(i,new TreeSet<>());
            lockLobbies[i] = new ReentrantLock();
            conditionLobbiesAvailable[i] = lockLobbies[i].newCondition();
            //gameEpoch[i] = 0;
            //playersEntering[i] = 0;
        }
        //players = new HashSet<>();
    }

    /**
     * Adicionar a todos os jogadores à espera de um certo jogo o objecto Match, para terem agora associado
     * um jogo concreto
     *
     * @param playerThreads Lobby de nrPlayers jogadores
     * @param match Objecto que será variável partilhada de todas as threads
     */
    private void informThreadsOfAddedMatch(TreeSet<ServerThread> playerThreads, Match match) {
        for (ServerThread st : playerThreads) {
            st.associateMatch(match);
        }
    }

    private void printPlayersInLobby(TreeSet<ServerThread> playerThreads) {
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
        TreeSet<ServerThread> matchThreads;

        int lobbyIndex = player.getRank();
        // Em que instância de um certo lobby vou estar?
        //int myEpoch = gameEpoch[lobbyIndex];

        try {
            // Locks acedidos quando são escritas variáveis partilhadas
            lockLobbies(lobbyIndex);

            //Mais um jogador
            playersWaiting.get(lobbyIndex).add(st);
            //playersEntering[lobbyIndex]++;

            System.out.println("Lobby de rank " + player.getRank() +  " já tem " + playersWaiting.get(lobbyIndex).size() + "/" + size + " jogadores.");
            // Já posso começar o jogo?

            matchThreads = findMatchPlayers(lobbyIndex); //tenta encontrar jogadores para um match
            if (matchThreads.size() == size) { //verifica se tem jogadores suficientes
                startMatch(matchThreads);
            }

            else{ //senão, fica em espera

                try {
                    //while (myEpoch == gameEpoch[lobbyIndex]) {
                    lobbiesAvailableAwait(lobbyIndex);
                    //}
                } catch (InterruptedException e) {
                    st.cleanup();
                }
            }
        }
        finally { //desbloqueia tudo
            unlockLobbies(lobbyIndex); //TODO remover
        }

    }

    /** Inicializa um lobby (match) com os 'size' jogadores.
     *
     * @param matchThreads Server threads dos jogador que se vão entrar no match.
     */
    private void startMatch(TreeSet<ServerThread> matchThreads){
        System.out.println("A iniciar um jogo.");
        Match match = new Match(matchThreads, size, new ReentrantLock());
        informThreadsOfAddedMatch(matchThreads, match);

        new Thread(match).start();

        endMatching(matchThreads);
        //gameEpoch[lobbyIndex]++;
        //conditionLobbiesAvailable[lobbyIndex].signalAll();
        //playersEntering[lobbyIndex] = 0;
        //playersWaiting.get(lobbyIndex).clear();
    }

    /** Tenta encontrar 'size' jogadores no lobbie pedido em conjunto com o do seu rank anterior ou seguinte (exclusivamente).
     *
     * @param lobbyIndex Identificador de lobby
     * @return Estrutura com os 'size' jogadores encontrados para um possível match.
     */
    private TreeSet<ServerThread> findMatchPlayers(int lobbyIndex){
        int nsize;
        TreeSet<ServerThread> tmp;
        TreeSet<ServerThread> r = new TreeSet<>(playersWaiting.get(lobbyIndex));

        nsize = size - r.size(); //Vê se o rank atual já tem jogadores suficientes
        if (nsize != 0){ //se não tiver, vê se com o anterior já tem
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
                /*
                else { //senão limpa o r para não retornar nenhum jogador de todo
                    r.clear();
                }
                */
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
        TreeSet<ServerThread> r = new TreeSet<>(), t;
        if (lobbyIndex >= 0 && lobbyIndex < rankNum && playersWaiting.get(lobbyIndex).size() == N) {
                r.addAll(playersWaiting.get(lobbyIndex));
        }
        return r;
    }

    /** Método que bloqueia um o lock de uma threads e o das suas vizinhas imediatas.
     *
     * @param lobbyIndex Índice do lobby a ser trancado.
     */
    private void lockLobbies(int lobbyIndex){
        if (lobbyIndex - 1 >= 0 ) { //é necessário bloquear para o caso de termos de ir buscar jogadores do rank imediatamente inferior
            lockLobbies[lobbyIndex - 1].lock();
        }

        if (lobbyIndex + 1 < rankNum) { //é necessário bloquear para o caso de termos de ir buscar jogadores do rank imediatamente superior
            lockLobbies[lobbyIndex + 1].lock();
        }

        lockLobbies[lobbyIndex].lock();
    }

    /** Método que desbloqueia um o lock de uma threads e o das suas vizinhas imediatas.
     *
     * @param lobbyIndex Índice do lobby a ser desbloqueado.
     */
    private void unlockLobbies(int lobbyIndex){
        if (lobbyIndex - 1 >= 0 && lockLobbies[lobbyIndex - 1].isLocked() && lockLobbies[lobbyIndex - 1].isHeldByCurrentThread()) {
            lockLobbies[lobbyIndex - 1].unlock();
        }

        if (lobbyIndex + 1 < rankNum && lockLobbies[lobbyIndex + 1].isLocked() && lockLobbies[lobbyIndex + 1].isHeldByCurrentThread()) {
            lockLobbies[lobbyIndex + 1].unlock();
        }

        lockLobbies[lobbyIndex].unlock();
    }

    /** Espera até haverem jogadores suficientes.
     *
     * @param lobbyIndex Índice do lobby.
     * @throws InterruptedException No caso do await lançar a exceção.
     */
    private void lobbiesAvailableAwait(int lobbyIndex) throws InterruptedException {
        if (lobbyIndex - 1 >= 0) {
            lockLobbies[lobbyIndex - 1].unlock();
        }

        if (lobbyIndex + 1 < rankNum) {
            lockLobbies[lobbyIndex + 1].unlock();
        }

        conditionLobbiesAvailable[lobbyIndex].await();
    }

    /** Finaliza e "limpa" o método waitGame
     *
     * @param matchThreads Threads dos jogadores que vão entrar no jogo.
     */
    private void endMatching (TreeSet<ServerThread> matchThreads){
        int lobbyIndex;
        boolean [] check = new boolean[rankNum];
        Arrays.fill(check, false);

        for (ServerThread st : matchThreads){
            lobbyIndex = st.getPlayer().getRank();
            //playersEntering[lobbyIndex]--; //retira um jogador à contagem daquele lobby
            if(!check[lobbyIndex]) {
                playersWaiting.get(lobbyIndex).clear(); //apaga o jogador do lobby
                conditionLobbiesAvailable[lobbyIndex].signalAll(); //notifica as threads do jogo para avançarem; QUANDO FOR PARA FAZER MATCHES DE QUALQUER RANK POR AQUI SIGNAL E O LOCK COM FAIR
                check[lobbyIndex] = true; //regista que não é necessário sinalizar mais a condição que acabou de ser sinalizada
            }
        }

    }

}
