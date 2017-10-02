package com.company;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class Client {
    int PORT = 12345;
    int playerID;
    int teamPlayer;
    ArrayList<String> myCards;

    public static void main(String[] args) {
        new Client();
    }

    public Client() {
        try {

            Socket socket = new Socket("localhost", PORT);
            ObjectSender t1 = new ObjectSender(socket);
            t1.start();

            while (true) {
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                PacketFromServerToClient packet = (PacketFromServerToClient) input.readObject();
                //depending on type of msg give go to assigned function
                switch (packet.getTypeOfMsg()) {
                    case "getId":
                        getMyIdAndPrintMyName(packet);
                        break;
                    case "getNewCards":
                        getCardsFromServer(packet);
                        break;
                    case "AskForBid":
                        bidAsked(packet);
                        break;
                    case "giveCard":
                        giveCardToServer(packet);
                        break;
                    case "otherGivingCard":
                        tellPlayerToWait(packet);
                        break;
                    case "removeCardFromList":
                        removeCardFromMyHand(packet);
                        break;
                    case "print-it":
                        simplePrintMsg(packet);
                        break;
                    case "print-it-2":
                        simplePrintMsg2(packet);
                        break;
                    case "hand_winner":
                        handWinner(packet);
                        break;
                    case "RoundResults":
                        handWinner(packet);
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Server is not listening...");
            e.printStackTrace();
        }
    }

    private void handWinner(PacketFromServerToClient packet) {
        System.out.println(packet.getMessage());
    }

    private void simplePrintMsg(PacketFromServerToClient packet) {
        System.out.println(packet.getMessage());
    }

    private void simplePrintMsg2(PacketFromServerToClient packet) {
        System.out.println(packet.getMessage());
        System.out.println("Your cards:");
        System.out.println(sortList(myCards));
    }

    private void removeCardFromMyHand(PacketFromServerToClient packet) {
        myCards.remove(packet.getMessage());
    }

    private void tellPlayerToWait(PacketFromServerToClient packet) {
        System.out.println(packet.getMessage());
    }

    private void giveCardToServer(PacketFromServerToClient packet) {
        System.out.println(packet.getMessage());
    }

    private void bidAsked(PacketFromServerToClient packet) {
        System.out.println(packet.getMessage());
    }

    private void getCardsFromServer(PacketFromServerToClient packet) throws IOException, ClassNotFoundException {
        myCards = packet.getCards();
        System.out.println("***\nNew Cards:\n" + sortList(myCards));
    }

    private void getMyIdAndPrintMyName(PacketFromServerToClient packet) throws IOException, ClassNotFoundException {
        playerID = packet.getToPlayer();
        teamPlayer = (playerID + 1) % 4 + 1;
        System.out.println(packet.getMessage());
        System.out.println("Your partner is: Player " + teamPlayer);
    }

    /**
     * This function is used to compare string and sort them by grouping.
     * */
    private ArrayList<String> sortList(ArrayList<String> x) {
        Collections.sort(x, new Comparator<String>() {
            public int compare(String e1, String e2) {
                int c = new Character(e2.charAt(e2.length() - 1)).compareTo(e1.charAt(e1.length() - 1));
                if (c != 0)
                    return -c;


                int firstNum = 0;
                int secondNum = 0;
                try {
                    firstNum = Integer.parseInt(e1.substring(0, e1.length() - 1));

                } catch (Exception e) {
                    if (e1.substring(0, e1.length() - 1).equals("J"))
                        firstNum = 11;
                    if (e1.substring(0, e1.length() - 1).equals("Q"))
                        firstNum = 12;
                    if (e1.substring(0, e1.length() - 1).equals("K"))
                        firstNum = 13;
                    if (e1.substring(0, e1.length() - 1).equals("A"))
                        firstNum = 14;

                }

                try {
                    secondNum = Integer.parseInt(e2.substring(0, e2.length() - 1));
                } catch (Exception e) {
                    if (e2.substring(0, e2.length() - 1).equals("J"))
                        secondNum = 11;
                    if (e2.substring(0, e2.length() - 1).equals("Q"))
                        secondNum = 12;
                    if (e2.substring(0, e2.length() - 1).equals("K"))
                        secondNum = 13;
                    if (e2.substring(0, e2.length() - 1).equals("A"))
                        secondNum = 14;
                }
                return firstNum - secondNum;
            }
        });
        return x;
    }

    public class ObjectSender extends Thread {
        Socket socket;

        public ObjectSender(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            int i = 0;
            String input = "";

            while (true) {
                try {
                    Thread.sleep(100);
                    i = (i + 1) % 16;
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    PacketFromClientToServer packet = new PacketFromClientToServer();
                    Scanner src = new Scanner(System.in);
                    input = src.nextLine();
                    packet.setMessage(input);
                    output.writeObject(packet);
                    output.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(0);
                }


            }
        }
    }
}
