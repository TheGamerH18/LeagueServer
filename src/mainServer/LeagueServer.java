package mainServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import champ.Ashe;
import champ.Champion;
import champ.Mundo;
import com.blogspot.debukkitsblog.net.Datapackage;
import com.blogspot.debukkitsblog.net.Executable;
import com.blogspot.debukkitsblog.net.Server;

@SuppressWarnings("BusyWait")
public class LeagueServer extends Server {

    int startgame = 1;

    int[][] positions = {{19, 0}, {0, 19}};

    // Player Health [0] = Current Health | [1] = Max Health
    int[][] playerhealth = new int[2][2];

    // Player Champs
    Champion[] playerchamps = new Champion[2];
    int champsselected = 0;

    // Winner var saving last winner
    int winner;

    public LeagueServer() {
        super(25598, true, true, false, false);
    }

    @Override
    public void preStart() {

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

        registerMethod("CHAMP_SELECT", new Executable(){
            @Override
            public void run(Datapackage pack, Socket socket) {
                // 1: Wer hat gewaehlt? | 2: Was wurde gewaehlt?
                int absenderplayerid = Integer.parseInt(String.valueOf(pack.get(1)));
                String champ = String.valueOf(pack.get(2));
                for (Champion playerchamp : playerchamps) {
                    if (playerchamp.Champname.equals(champ)) {
                        sendReply(socket, "Champ already selected");
                    }
                }
                try {
                    Class<?> clazz = Class.forName(champ);
                    playerchamps[absenderplayerid] = (Champion) clazz.newInstance();
                    sendReply(socket, "Champ accepted");
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    sendReply(socket, "Server error");
                }
            }
        });

        registerMethod("DAMAGE", new Executable() {
            @Override
            public void run(Datapackage pack, Socket socket) {
                int absenderid = Integer.parseInt(String.valueOf(pack.get(1)));
                int targetid = Integer.parseInt(String.valueOf(pack.get(2)));
                int amount = Integer.parseInt(String.valueOf(pack.get(3)));
                System.out.println(absenderid + " " + targetid + " Amount: " + amount);
                playerhealth[targetid][0] -= amount;
            }
        });

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



        registerMethod("PRINT", new Executable() {
            @Override
            public void run(Datapackage pack, Socket socket) {
                System.out.println("[SERVER] Nachricht von Client: "+pack);
                sendReply(socket, 123);
            }
        });
    }

    // Check if Users are logged in and if the Players are a life
    public void checkuser() {
        if(getClientCount() == 2) {
            startgame = 2;
            if(champsselected == 2) {
                if (playerhealth[0][0] <= 0 || playerhealth[1][0] <= 0) {
                    startgame = 0;
                    if (playerhealth[0][0] > 0) {
                        winner = 1;
                    } else {
                        winner = 2;
                    }
                }
            }
        } else {
            startgame = 1;
        }
    }

    public void gamereset() {
        positions = new int[][]{{19, 0}, {0, 19}};
        startgame = 1;
        playerhealth = new int[][]{{0, 0}, {0, 0}};
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

            while (champsselected != 2){
                for (Champion playerchamp : playerchamps) {
                    if (playerchamp.maxhealth == 0) {
                        champsselected++;
                    }
                }
                if(champsselected != 2){
                    champsselected = 0;
                }
            }

            for(int i = 0; i < playerchamps.length; i++){
                playerhealth[i][0] = playerchamps[i].maxhealth;
                playerhealth[i][1] = playerchamps[i].maxhealth;
            }

            if(startgame == 2) {
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
            }

            while(startgame == 2){
                System.out.println("Send Data");
                server.broadcastMessage(new Datapackage("P_HEALTH", playerhealth[0][0], playerhealth[0][1], playerhealth[1][0], playerhealth[1][1]));
                server.broadcastMessage(new Datapackage("POSITIONS", positions[0][0], positions[0][1], positions[1][0], positions[1][1]));
                Thread.sleep(1000);
            }

            server.broadcastMessage(new Datapackage("END", winner));

            gamereset();
        }
    }
}
