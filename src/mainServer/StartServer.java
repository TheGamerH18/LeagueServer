package mainServer;

public class StartServer {
    public static void main(String[] args) {

        LeagueServer server = new LeagueServer();
        try {
            server.startgame(server);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
