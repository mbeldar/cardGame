

import java.io.Serializable;
import java.util.ArrayList;

public class PacketFromServerToClient implements Serializable{
    String message;
    String TypeOfMsg;
    ArrayList<String>cards;
    int toPlayer;

    public String getTypeOfMsg() {
        return TypeOfMsg;
    }

    public void setTypeOfMsg(String typeOfMsg) {
        TypeOfMsg = typeOfMsg;
    }

    public ArrayList<String> getCards() {
        return cards;
    }

    public void setCards(ArrayList<String> cards) {
        this.cards = cards;
    }

    public int getToPlayer() {
        return toPlayer;
    }

    public void setToPlayer(int toPlayer) {
        this.toPlayer = toPlayer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
