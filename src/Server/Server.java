package Server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private ServerSocket server;
    private int port;
    private Socket socket = null;
    private List<Player> players;

    private List<Game> games;

    private Link link;
    private Match match;
    class Link implements Runnable{

        @Override
        public void run()
        {
            while(true)
            {
                if(players.size() < 10) {
                    try {
                        socket = server.accept();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Player player = new Player(socket);
                    synchronized (new Link()){
                        players.add(player);
                    }
                    System.out.println(player.name);
                    Thread t = new Thread(player);
                    t.start();
                    player.send("Link");// Link means that the player(listener) has been connected to server.
                }
                else{
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class Match implements Runnable{

        @Override
        public void run() {
            while(true)
            {
                Player circle = null;
                Player cross = null;
                synchronized (new Match()){
                    for (Player player:players){
                        if (player.getStatus() == 1){
                            circle = player;
                            break;
                        }
                    }
                    for (Player player:players){
                        if (player.getStatus() == 1 && player != circle){
                            cross = player;
                            break;
                        }
                    }
                }
                if(circle != null && cross != null)
                {
                    games.add(new Game(circle, cross));
                    circle.setStatus(2);
                    cross.setStatus(2);
                    System.out.println("A new game between " + circle.name + " and " + cross.name + " begins.");
                    circle.rival = cross.name;
                    cross.rival = circle.name;
                    circle.send("Match 0");
                    cross.send("Match 1");
                }
            }
        }
    }

    class Player implements Runnable{

        Socket socket;
        String name;
        String rival;
        InetAddress ip;
        BufferedReader in;
        PrintWriter out;
        int status = 0;
        public Player(Socket socket) {
            this.socket = socket;
            try{
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ip = socket.getLocalAddress();
                int cur = players.size() + 1;
                name = "Player " + cur;
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String inputLine = null;
            while(true){
                try
                {
                    inputLine = in.readLine();
                    System.out.println("Receive the message from client:" + name + " " + inputLine);
                }
                catch (IOException e)
                {
//                    throw new RuntimeException(e);
                    System.out.println(this.name + " has accidentally disconnected.");
                    for(Player p : players){
                        if(Objects.equals(p.name, this.rival))
                        {
                            p.send("Disconnect");
                        }
                    }
                    disconnect(this);
                    return;
                }
                if(inputLine == null) continue;

                if(inputLine.equals("Match")) status = 1;
                if(inputLine.charAt(0) == 'C')
                {
                    for(Player p : players)
                    {
                        if(Objects.equals(p.name, this.rival))
                        {
                            p.send(inputLine);
                            System.out.println("The chess move has been sent to " + this.rival);
                        }
                    }
                }
                if(inputLine.equals("Quit"))
                {
                    System.out.println("Disconnect:" + this.name);
                    disconnect(this);
                    return;
                }
            }
        }

        public void send(String msg){
            out.println(msg);
            out.flush();
        }

        public void setStatus(int status){
            this.status = status;
        }

        public int getStatus(){
            return status;
        }
    }

    public synchronized void disconnect(Player player){
        player.send("Quit");
        players.remove(player);
    }

    public void init(){
        System.out.println("Server ready.");
        port = 8888;
        players = new CopyOnWriteArrayList<>();
        games = new CopyOnWriteArrayList<>();
        try{
            server = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        link = new Link();
        Thread threadLink = new Thread(link);
        match = new Match();
        Thread threadMatch = new Thread(match);
        threadLink.start();
        threadMatch.start();
    }

    class Game{
        private Player circle;
        private Player cross;

        public Game(Player circle, Player cross) {
            this.circle = circle;
            this.cross = cross;
        }
    }

    public static void main(String[] args){
        Server serverGame = new Server();
        serverGame.init();
    }
}
