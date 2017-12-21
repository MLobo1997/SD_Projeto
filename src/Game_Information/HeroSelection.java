package Game_Information;

public class HeroSelection {
    /** Associar a cada indice um jogador, onde escreve um nr de 1 a 30 (representa os herois) */
    private int[] escolhas;
    private int playerNum;

    /**
     * Construtor
     */
    public HeroSelection() {
        escolhas = new int[playerNum]; // TODO: MUDAR ESTA VARIAVEL TEMPORARIA
        playerNum = 2;
    }

    /**
     * Escolher um heroi se possível
     * @param indexJogador índice do jogador que fará a escolha (0 .. playerNum)
     * @param indexHeroi heroi que é escolhido (0 .. 30)
     * @return true se escolha foi possível, false se heroi já escolhido por outra pessoa na equipa do jogador
     */
    synchronized public boolean chooseHero(int indexJogador, int indexHeroi) {
        for (int i = 0; i < playerNum; i++) {
            if (escolhas[i] == indexHeroi) {
                return false;
            }
        }

        escolhas[indexJogador] = indexHeroi;

        return true;
    }
}
