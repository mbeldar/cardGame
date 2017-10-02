

import java.io.Serializable;

public class PacketFromClientToServer implements Serializable{
    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
