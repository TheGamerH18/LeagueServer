package mainServer;

public class StartServer {
    public static void main(String[] args) {

        String version = "V.0.4.0";

        System.out.println("Starte Ver. "+ version);

        LeagueServer server = new LeagueServer();
        try {
            server.startgame(server);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
