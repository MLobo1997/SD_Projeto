import java.util.TreeSet;

public class Match extends Thread {
    TreeSet<ServerThread> players;

    Match (TreeSet<ServerThread> players) {
        this.players = (TreeSet<ServerThread>) players.clone();
    }

    public TreeSet<ServerThread> getPlayers() {
        return players;
    }

    public void run() {
        for (ServerThread st : players) {
            st.printToOutput("Welcome to the match!");
        }
    }

}
