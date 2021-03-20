package mainServer;

import java.net.Socket;

import com.blogspot.debukkitsblog.net.Datapackage;
import com.blogspot.debukkitsblog.net.Executable;
import com.blogspot.debukkitsblog.net.Server;

public class LeagueServer extends Server {

    public LeagueServer() {
        super(25598, true, true, false);
    }

    @Override
    public void preStart() {
        registerMethod("NEW_POSITION", new Executable() {

            @Override
            public void run(Datapackage pack, Socket socket) {
                System.out.println(pack);
                String absender = pack.getSenderID();
                sendReply(socket, "Hallo");
            }
        });

        registerMethod("PRINT", new Executable() {
            @Override
            public void run(Datapackage pack, Socket socket) {
                System.out.println("[SERVER] Nachricht von Client: "+pack);
                sendReply(socket, 123);
            }
        });
    }

    public static void main(String[] args) {
        LeagueServer server = new LeagueServer();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("User Count " + server.getClientCount());

    }
}
