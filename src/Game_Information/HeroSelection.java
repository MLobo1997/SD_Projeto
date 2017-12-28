package Game_Information;

import java.util.HashMap;
import java.util.Map;

public class HeroSelection {
    /** Associar a cada indice um jogador, onde escreve um nr de 1 a 30 (representa os herois) */
    private Map<String,Integer> choices;

    /**
     * Construtor
     */
    public HeroSelection() {
        choices = new HashMap<>();
    }

    /**
     * Escolher um heroi se possível
     * @param playerName Nome do jogador que fará a escolha
     * @param indexHeroi heroi que é escolhido (0 .. 30)
     * @return true se escolha foi possível, false se heroi já escolhido por outra pessoa na equipa do jogador
     */
    synchronized public boolean chooseHero(String playerName, int indexHeroi) {

        for (Integer choice : choices.values()) {
            if (choice == indexHeroi) {
                return false;
            }
        }

        choices.put(playerName,indexHeroi);

        return true;
    }

    synchronized public int getHero(String playerName) {
        if (!choices.containsKey(playerName)) {
            return -1;
        }
        return choices.get(playerName);
    }
}
