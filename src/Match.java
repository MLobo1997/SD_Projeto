import java.util.TreeSet;

public class Match extends Thread {
    TreeSet<ServerThread> players;

    Match (TreeSet<ServerThread> players) {
        this.players = players;
    }

    public void run() {
        // TODO: Implement chat
        for (ServerThread st : players) {
            st.printToOutput("Welcome to the match!");
        }
    }

}
