/** A classe principal do servidor.
 *
 */
public class MatchMaker {
    /** Registo de todas as contas. */
    private final PlayersRegister allPlayers = new PlayersRegister();
    /** Estrutura com todos os jogadores que estão ligados ao servidor no momento. */
    private /*final*/ OnlinePlayers onlinePlayers/* = new OnlinePlayers();*/;
    /** Jogadores que estão atualmente à procura de um jogo*/
    private /*final*/ MatchingPlayers matchingPlayers;
    /** Equipas já formadas que estão em processo de início de jogo*/
    private /*final*/ Lobbies currentLobbies;
}
