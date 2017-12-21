package Exceptions;

/** Exceção de quando um utilizador sobe de nível, criada com a intenção de implementar uma notificação para o jogador de que isto aconteceu.
 *
 */
public class LevelUpException extends Exception{
    /** Novo rank para o qual subiu de nível*/
    private Integer newRank;

    public LevelUpException(Integer newRank) {
        super();
        this.newRank = newRank;
    }
    public Integer getNewRank() {
        return newRank;
    }
}
