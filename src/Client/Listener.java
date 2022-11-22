package Client;

import application.controller.Controller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Platform;

public class Listener implements Runnable {
    final int portNumber = 8888;
    private BufferedReader in;
    private PrintWriter out;
    private Controller controller;
    private Socket socket;

    public Listener(Controller controller) throws IOException {
        //
        this.socket = new Socket("localhost", portNumber);
        this.controller = controller;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("Link"); // send Link message to server
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(){
        String inputLine;
        while (true)
        {
            try
            {
                String[] pos;
                if ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    if (inputLine.equals("Link")) {
                        System.out.println("Client and Server Link Success, please wait...");
                        out.println("Match");
                        out.flush();
                    }
                    if (inputLine.charAt(0) == 'M') {
                        System.out.println("Match Success");
                        pos = inputLine.split(" ");
                        if(pos[1].equals("0"))
                        {
                            System.out.println("Please move first.");
                            chessMove();
                        }
                        else {
                            System.out.println("Please wait for your opponent to move...");
                        }
                    }
                    if(inputLine.charAt(0) == 'C'){ // deal with the chess move information
                        System.out.println("Receive the chess move from the server.");
                        pos = inputLine.split(" ");
                        int x = Integer.parseInt(pos[1]);
                        int y = Integer.parseInt(pos[2]);
                        System.out.println(x);
                        System.out.println(y);
                        Platform.runLater(()-> {
                        controller.refreshBoard(x, y);
                        if (controller.judgeWin()) {
                            System.out.println("You lose this game.");
                            out.println("Quit");
                            out.flush();
                        }
                        else if(controller.judgeDraw()){
                            System.out.println("The game is draw.");
                            out.println("Quit");
                            out.flush();
                        }
                            else {
                                chessMove();
                            }
                        });
                    }
                    if(inputLine.equals("Disconnect")) {
                        System.out.println("Your opponent accidentally disconnected.");
                        System.out.println("So you win this game.");
                        System.out.println("Game quit.");
                        System.exit(0);
                    }
                    if(inputLine.equals("Quit")) {
                        System.out.println("The game is over, automatically exit in 5s...");
                        Thread.sleep(5000);
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                System.out.println("The server has been accidentally closed.");
                System.out.println("Quit the game.");
                System.exit(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void chessMove() {
        System.out.println("Please make your chess move.");
        Platform.runLater(()-> {
        controller.getGame_panel().setOnMouseClicked(mouseEvent ->
        {
            int x = (int) (mouseEvent.getX() / controller.getBound());
            int y = (int) (mouseEvent.getY() / controller.getBound());
            if (controller.refreshBoard(x, y)) {
                System.out.println("Send chess move");
                out.println("Chess " + x + " " + y);
                out.flush();
                if(controller.judgeWin()){
                    System.out.println("You win this game.");
                    out.println("Quit");
                    out.flush();
                }
                else if (controller.judgeDraw()) {
                    System.out.println("The game is draw.");
                    out.println("Quit");
                    out.flush();
                }
                controller.getGame_panel().setOnMouseClicked(null); // Cancel the listening.
            }
        });
        });
    }
}