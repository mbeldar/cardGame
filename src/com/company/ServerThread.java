
package com.company;

/**
 * Mayur
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * SERVER-SIDE                                                                           *
 * Note: We have used Sleep method for proper synchronisation of the clients with server.*
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unchecked")
public class ServerThread {
    int PORT = 12345;
    boolean isNewHand = true;
    int[] bid = new int[4];
    int turn = 1;
    int counterForOneRound = 1;
    String bigCard = "na";
    int bigCardPlayerId = 0;
    ArrayList<String> cardsOntable = new ArrayList<String>();
    int[] bidScore = new int[4];
    HashMap<String, Socket> hashMapPlayerNameSocket = new HashMap<String, Socket>();
    static boolean waitForOthers[] = new boolean[4];

    public static void main(String[] args) {
        new ServerThread();
    }


    public ServerThread() {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + PORT);
            e.printStackTrace();
        }

        /**
         * wait for connections from 4 players
         * Then start the connection
         * Open new thread for every client to handle their requests and give back response.
         */
        for (int i = 0; i < 4; i++) {
            try {
                System.out.println("Waiting...\nfor Player " + (i + 1));
                Socket individualSocket = serverSocket.accept();
                System.out.println("Player " + (i + 1) + " Connected");
                System.out.println("Started new connected from: " + individualSocket.getPort());
                hashMapPlayerNameSocket.put((i + 1) + "", individualSocket); //Add player ID and it's socket to hashmap.
                ConnectionThread t1 = new ConnectionThread(individualSocket, i + 1);
                t1.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //print results of hashmap for our reference.
        for (Map.Entry entry : hashMapPlayerNameSocket.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }

        System.out.println("All players are connected. Not listening anymore!!!");
    }

    static ArrayList<String> cards = new ArrayList<String>(Cards.getShuffledCards());
    static ArrayList<String> cards1;
    static ArrayList<String> cards2;
    static ArrayList<String> cards3;
    static ArrayList<String> cards4;


    class ConnectionThread extends Thread {

        private Socket socket = null;
        private int playerID;
        int basicTurn = 0;
        private boolean haveToBid = true;
        int Team1 = 0;
        int Team2 = 0;

        //Constructor to initialize variables
        public ConnectionThread(Socket socket, int playerID) {
            super("ConnectionThread");
            this.socket = socket;
            this.playerID = playerID;
        }

        public void run() {
            try {
                sendIDsToClients();
                while (true) {
                    if (isNewHand) {

                        Thread.sleep(getRandomSleep(0, 100));
                        //Next turn 1->2->3->4->1.
                        nextBasicTurn();

                        if (cards.size() == 0)
                            cards = Cards.getShuffledCards();

                        Thread.sleep(getRandomSleep());
                        //Send shuffled cards to all users
                        sendCardsTo(playerID);
                    }
                    Thread.sleep(100);
                    //Set all bid to zero
                    initializeBids();
                    if (haveToBid)
                        askForBid(playerID, ""); //Ask for bid
                    int i = 0;
                    reSetBidScore(); //Set score to 0
                    //round till 13 cards
                    turn = basicTurn;
                    while (true) {
                        waitForOthers[playerID - 1] = true;
                        if (bid[playerID - 1] >= 0)
                            askForCard(playerID, "");

                        //Wait for everyone to play, then calculate results.
                        while (waitForOthers[playerID - 1]) {
                            Thread.sleep(100);
                            //  System.out.println(playerID + " waiting...");
                        }
                        Thread.sleep(100);
                        calculateSingleBasicRoundResults();
                        Thread.sleep(100);

                        //The one who wins plays next round first
                        assignTurnToWinner();
                        Thread.sleep(100);

                        System.out.println("Player" + playerID + " has bid = " + bidScore[playerID - 1]);
                        System.out.println(++i + " basic round done!");

                        Thread.sleep(100);
                        //Reset cards on table to start new hand
                        tellWhoIsWinnerAndResetCardsOnTable();
                        System.out.println("card1.size(): " + cards1.size());
                        Thread.sleep(100);
                        //if there are zero cards with user calculate round result
                        if (cards1.size() == 0) {
                            Thread.sleep(getRandomSleep(100, 300));
                            System.out.println("***card1 size 12 in " + playerID);
                            calculateRoundResult();
                            Thread.sleep(getRandomSleep(100, 300));
                            sendRoundResults();
                            if (Team1 >= 250 || Team2 >= 250) {
                                sendWinners();
                                this.socket.close();
                                // Thread.sleep(1000);
                                // System.exit(0);
                            }
                            isNewHand = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * get random value between 1 to 1000
         * */
        private long getRandomSleep() {
            return ThreadLocalRandom.current().nextInt(1, 1000 + 1);
        }

        /**
         * get random value between min and max
         * */
        private long getRandomSleep(int min, int max) {
            return ThreadLocalRandom.current().nextInt(min, max + 1);
        }

        /**
         * initialize bid to -1
         * */
        private void initializeBids() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bid[playerID - 1] = -1;
        }


        /**
         * Send who won the game
         * */
        private void sendWinners() {
            try {

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                PacketFromServerToClient packet = new PacketFromServerToClient();
                packet.setTypeOfMsg("RoundResults");
                if (Team1 > Team2)
                    packet.setMessage("Team 1 wins!!! Congrats Player 1 and 3");
                else
                    packet.setMessage("Team 2 wins!!! Congrats Player 2 and 4");


                output.writeObject(packet);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        /**
         * Send each round results
         * */
        private void sendRoundResults() {
            try {

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                PacketFromServerToClient packet = new PacketFromServerToClient();
                packet.setTypeOfMsg("RoundResults");
                packet.setMessage("***Round is done team scores are:\nSCORES\nTeam1: " + Team1 + "\nTeam2: " + Team2);
                output.writeObject(packet);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Calculate round results
         * */
        private void calculateRoundResult() {

            int _Team1 = bidScore[0] + bidScore[2];
            int _Team2 = bidScore[1] + bidScore[3];

            if (bid[0] + bid[2] <= _Team1) {
                Team1 += (bid[0] + bid[2]) * 10 + _Team1 - (bid[0] + bid[2]);
            } else {
                Team1 += -(bid[0] + bid[2]) * 10;
            }

            if (bid[1] + bid[3] <= _Team2) {
                Team2 += (bid[1] + bid[3]) * 10 + _Team2 - (bid[1] + bid[3]);
            } else {
                Team2 += -(bid[1] + bid[3]) * 10;
            }
            System.out.println("T1: " + Team1);
            System.out.println("T2: " + Team2);

        }


        /**
         * Self explanatory
         * */
        private void tellWhoIsWinnerAndResetCardsOnTable() {
            try {
                Thread.sleep(10);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                PacketFromServerToClient packet = new PacketFromServerToClient();
                if (playerID != bigCardPlayerId)
                    packet.setMessage("Player " + bigCardPlayerId + " wins this hand!");
                else
                    packet.setMessage("You win this hand!");

                packet.setTypeOfMsg("hand_winner");
                output.writeObject(packet);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            cardsOntable.clear();
            bigCard = "na";

        }

        /**
         * Self explanatory
         * */
        private void assignTurnToWinner() {
            turn = bigCardPlayerId;
            System.out.println("Turn of " + turn);
        }

        private void reSetBidScore() {
            for (int i = 0; i < 4; i++) {
                bidScore[i] = 0;
            }
        }

        /**
         * Player who played bigger card gets +1 score.
         * */
        private void calculateSingleBasicRoundResults() {
            if (playerID == 1)
                bidScore[bigCardPlayerId - 1]++;
            System.out.println(bigCardPlayerId + " win this round!");
        }

        /**
         * Ask for  cards from user if the input is invalid then we do a recursive call to this function.
         * @param warning : it is a message to be send to player.
         * */
        private void askForCard(int playerID, String warning) {
            System.out.println("Asking for card from " + turn);
            String msg = "";
            String typeMsg = "";
            if (playerID == turn) {
                msg = "It's your turn";
                typeMsg = "giveCard";
            } else {
                //  msg = "It's Player " + turn + "s turn";
                typeMsg = "otherGivingCard";
                msg = "Wait for your turn";
            }
            try {
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                PacketFromServerToClient packet = new PacketFromServerToClient();
                packet.setTypeOfMsg(typeMsg);
                packet.setMessage(msg + "\n" + warning);
                output.writeObject(packet);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                PacketFromClientToServer packet1 = (PacketFromClientToServer) input.readObject();
                String cardPlayedByUser = packet1.getMessage().toUpperCase();

                //if it is your turn
                if (turn == playerID) {
                    counterForOneRound++;
                    if (playerHasThisCard(cardPlayedByUser, playerID) && canPlayerPlayThisCard(cardPlayedByUser, playerID)) {


                        System.out.println("Valid Card played by player " + playerID);

                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        PacketFromServerToClient packet3 = new PacketFromServerToClient();
                        packet3.setTypeOfMsg("removeCardFromList");
                        packet3.setMessage(cardPlayedByUser);
                        out.writeObject(packet3);
                        out.flush();
                        cards.remove(cardPlayedByUser);
                        removeCardFromUser(playerID, cardPlayedByUser);
                        cardsOntable.add(cardPlayedByUser);
                        tellEveryoneWhatIPlayed(cardPlayedByUser);
                        sendCardsOnTable();

                        //give next turn
                        nextTurn();
                        if ("na".equals(bigCard)) {
                            bigCard = cardPlayedByUser;
                            bigCardPlayerId = playerID;
                        } else {
                            //if player has played different card
                            if (typeOfCard(bigCard).equals(typeOfCard(cardPlayedByUser)))
                                if (isCurrentCardBiggerThanOldCard(bigCard, cardPlayedByUser)) {
                                    bigCard = cardPlayedByUser;
                                    bigCardPlayerId = playerID;
                                }
                            System.out.println("Bigger card is: " + bigCard);
                        }
                        if (cardsOntable.size() == 4)
                            DontWaitAnymore();


                    } else {
                        askForCard(playerID, "You cannot play this card at the moment");
                    }
                } else {
                    askForCard(playerID, "It is not your turn. Please wait.");
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * Self explanatory
         * */
        private void DontWaitAnymore() {
            waitForOthers[0] = false;
            waitForOthers[1] = false;
            waitForOthers[2] = false;
            waitForOthers[3] = false;

        }


        /**
         * Self explanatory
         * */
        private void sendCardsOnTable() {
            String ms = "Cards on table are:\n" + cardsOntable.toString();
            for (int i = 0; i < hashMapPlayerNameSocket.size(); i++) {
                try {
                    ObjectOutputStream op = new ObjectOutputStream(hashMapPlayerNameSocket.get((i + 1) + "").getOutputStream());
                    PacketFromServerToClient packet = new PacketFromServerToClient();
                    packet.setMessage(ms);
                    packet.setTypeOfMsg("print-it");
                    op.writeObject(packet);
                    op.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Self explanatory
         * */
        private void tellEveryoneWhatIPlayed(String cardPlayedByUser) {
            int nextTurn = 0;
            if ((turn + 1) == 5)
                nextTurn = 1;
            else
                nextTurn = turn + 1;

            String mg = "Player " + turn + " played " + cardPlayedByUser +
                    "\nPlayer " + (nextTurn) + " is playing now!";


            if (nextTurn == this.playerID)
                mg = "Player " + turn + " played " + cardPlayedByUser +
                        "\nYour turn now!\n";


            for (int i = 0; i < hashMapPlayerNameSocket.size(); i++) {
                try {
                    ObjectOutputStream op = new ObjectOutputStream(hashMapPlayerNameSocket.get((i + 1) + "").getOutputStream());
                    PacketFromServerToClient packet = new PacketFromServerToClient();
                    packet.setMessage(mg);
                    packet.setTypeOfMsg("print-it-2");

                    op.writeObject(packet);
                    op.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /**
         * Self explanatory
         * @bigcard is the card which was played first or which is highest hand
         * */
        private boolean canPlayerPlayThisCard(String cardPlayedByUser, int playerID) {
            if ("na".equals(bigCard))
                return true;
            if (typeOfCard(bigCard).equals(typeOfCard(cardPlayedByUser)))
                return true;
            else {
                if (DoesNotHaveThisType(bigCard, playerID))
                    return true;
            }
            return false;
        }
        /**
         * Self explanatory
         * */
        private boolean DoesNotHaveThisType(String bigCard, int playerID) {
            String cardType = bigCard.substring(bigCard.length() - 1);
            ArrayList<String> checkList = null;
            switch (playerID) {
                case 1:
                    checkList = new ArrayList<String>(cards1);
                    break;
                case 2:
                    checkList = new ArrayList<String>(cards2);
                    break;
                case 3:
                    checkList = new ArrayList<String>(cards3);
                    break;
                case 4:
                    checkList = new ArrayList<String>(cards4);
                    break;
            }
            for (int i = 0; i < checkList.size(); i++) {
                String ele = checkList.get(i).substring(checkList.get(i).length() - 1);
                if (ele.equals(cardType)) {
                    return false;
                }
            }

            return true;
        }

        /**
         * Self explanatory
         * */
        private String typeOfCard(String _card) {
            return _card.substring(_card.length() - 1).toUpperCase();
        }

        private void updateCurrentHand() {
        }

        private boolean isCurrentCardBiggerThanOldCard(String e1, String e2) {

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
            if (firstNum - secondNum < 0) {
                return true;
            }

            return false;
        }
        /**
         * this is hand turn
         * */
        private void nextTurn() {
            turn++;
            if (turn == 5)
                turn = 1;
        }
        /**
         * This is round turn
         * */
        private void nextBasicTurn() {

            basicTurn++;
            if (basicTurn == 5)
                basicTurn = 1;

        }

        private void removeCardFromUser(int playerID, String cardPlayedByUser) {
            switch (playerID) {
                case 1:
                    cards1.remove(cardPlayedByUser);
                    break;
                case 2:
                    cards2.remove(cardPlayedByUser);
                    break;
                case 3:
                    cards3.remove(cardPlayedByUser);
                    break;
                case 4:
                    cards4.remove(cardPlayedByUser);
                    break;
            }
        }

        /**
         * Self explanatory
         * */
        private boolean playerHasThisCard(String x, int playerID) {
            switch (playerID) {
                case 1:
                    if (cards1.contains(x.toUpperCase())) return true;
                    break;
                case 2:
                    if (cards2.contains(x.toUpperCase())) return true;
                    break;
                case 3:
                    if (cards3.contains(x.toUpperCase())) return true;
                    break;
                case 4:
                    if (cards4.contains(x.toUpperCase())) return true;
                    break;
            }
            return false;
        }

        /**
         * Self explanatory
         * */
        private void askForBid(int playerID, String warning) {
            System.out.println("Asking for bid from Player " + playerID);
            ObjectOutputStream output = null;
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                PacketFromServerToClient packet = new PacketFromServerToClient();
                packet.setTypeOfMsg("AskForBid");
                packet.setMessage(warning + "Enter your Bid:");
                output.writeObject(packet);
                output.flush();

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                PacketFromClientToServer packet1 = (PacketFromClientToServer) input.readObject();
                if (hashMapPlayerNameSocket.size() < 4)
                    askForBid(playerID, "Wait for others to join then ");
                try {
                    int x = Integer.parseInt(packet1.getMessage());
                    if (x < 1 || x > 13) {
                        askForBid(playerID, "Invalid Bid!\n");
                    } else {
                        bid[playerID - 1] = x;
                        System.out.println("Player " + playerID + " bid: " + x);
                        if (bid[0] != -1 && bid[2] != -1 && bid[3] != -1 && bid[1] != -1)
                            sendBidToEveryOne();
                    }
                } catch (Exception e) {
                    askForBid(playerID, "Bid must be a number between 1 and 13\n");
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        public void sendBidToEveryOne() {
            String ms = "All Bids are: " + bid[0] + ", " + bid[1] + ", " + bid[2] + ", " + bid[3] + " respectively";
            for (int i = 0; i < hashMapPlayerNameSocket.size(); i++) {
                try {
                    ObjectOutputStream op = new ObjectOutputStream(hashMapPlayerNameSocket.get((i + 1) + "").getOutputStream());
                    PacketFromServerToClient packet = new PacketFromServerToClient();
                    packet.setMessage(ms);
                    packet.setTypeOfMsg("print-it");
                    op.writeObject(packet);
                    op.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendCardsTo(int playerID) {
            System.out.println("Sending cards to... Player " + playerID);
            switch (playerID) {
                case 1:
                    cards1 = new ArrayList<String>(cards.subList(0, 13));
                    send13Cards(cards1);
                    break;
                case 2:
                    cards2 = new ArrayList<String>(cards.subList(0, 13));
                    send13Cards(cards2);
                    break;
                case 3:
                    cards3 = new ArrayList<String>(cards.subList(0, 13));
                    send13Cards(cards3);
                    break;
                case 4:
                    cards4 = new ArrayList<String>(cards.subList(0, 13));
                    send13Cards(cards4);
                    break;
            }
            cards = new ArrayList<String>(cards.subList(13, cards.size()));
            System.out.println("***\nNew set of cards are " + cards.size());
        }

        private void send13Cards(ArrayList<String> cards_) {
            try {
                Thread.sleep(10);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                PacketFromServerToClient packet = new PacketFromServerToClient();
                packet.setCards(cards_);
                packet.setTypeOfMsg("getNewCards");
                output.writeObject(packet);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * At the start of game send everyone their IDs.
         * */
        private void sendIDsToClients() throws IOException, InterruptedException {
            Thread.sleep(10);
            System.out.println("Sending ID to... " + playerID);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            PacketFromServerToClient packet = new PacketFromServerToClient();
            packet.setMessage("You are player " + playerID);
            packet.setToPlayer(playerID);
            packet.setTypeOfMsg("getId");
            output.writeObject(packet);
            output.flush();
        }
    }
}
