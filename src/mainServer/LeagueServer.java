package mainServer;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import com.blogspot.debukkitsblog.net.Datapackage;
import com.blogspot.debukkitsblog.net.Executable;
import com.blogspot.debukkitsblog.net.Server;

@SuppressWarnings("BusyWait")
public class LeagueServer extends Server {

    int startgame = 1;

    int[][] positions = {{19, 0}, {0, 19}};

    public LeagueServer() {
        super(25598, true, true, false);
    }

    @Override
    public void preStart() {

        registerMethod("NEW_POSITION", new Executable() {
            @Override
            public void run(Datapackage pack, Socket socket) {
                System.out.println(pack);
                int absenderid = Integer.parseInt(String.valueOf(pack.get(1)));
                System.out.println(absenderid);
                positions[absenderid][0] = Integer.parseInt(String.valueOf(pack.get(2)));
                positions[absenderid][1] = Integer.parseInt(String.valueOf(pack.get(3)));
                sendReply(socket, "Hallo");
            }
        });

        registerMethod("AUTH", new Executable() {
            @Override
            public void run(Datapackage pack, Socket socket) {
                String authuser = (String) pack.get(1);
                System.out.println("Neuer Login: "+authuser + " ID: "+(getClientCount()-1));
                if(getClientCount() <= 2) {
                    sendReply(socket, "player"+(getClientCount()), getClientCount());
                }
                else{
                    sendReply(socket, "Server Full");
                }
                checkuser();
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

    public void checkuser() {
        if(getClientCount() == 2) {
            startgame = 2;
        } else {
            startgame = 1;
        }
    }

    public void startgame(LeagueServer server) throws InterruptedException {

        Thread checkuser = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    checkuser();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        checkuser.start();

        while (startgame != 0) {
            while (startgame == 1) {
                System.out.println("Nicht genug Spieler");
                server.broadcastMessage(new Datapackage("GAME_INFO", 0));
                Thread.sleep(1000);
            }
            server.broadcastMessage(new Datapackage("GAME_INFO", 5));
            Thread.sleep(1000);
            server.broadcastMessage(new Datapackage("GAME_INFO", 4));
            Thread.sleep(1000);
            server.broadcastMessage(new Datapackage("GAME_INFO", 3));
            Thread.sleep(1000);
            server.broadcastMessage(new Datapackage("GAME_INFO", 2));
            Thread.sleep(1000);
            server.broadcastMessage(new Datapackage("GAME_INFO", 1));
            Thread.sleep(1000);
            server.broadcastMessage(new Datapackage("GAME_INFO", 6));
            while(startgame == 2){
                System.out.println("Send Positions");
                server.broadcastMessage(new Datapackage("POSITIONS", positions[0][0], positions[0][1], positions[1][0], positions[1][1]));
                Thread.sleep(1000);
            }
        }
    }
}
