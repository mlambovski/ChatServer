/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.Serializable;

/**
 *
 * @author Myro
 */
public class ChatMessage implements Serializable {

    static final int USER_LIST = 0, MESSAGE = 1, LOGOUT = 2, FILE = 3;
    private int type;
    private String message;
    private String recipient;
    private String fileName;

    ChatMessage(int type, String message, String recipient, String fileName) {
        this.type = type;
        this.message = message;
        this.recipient = recipient;
        this.fileName = fileName;
    }

    int getType() {
        return type;
    }

    String getMessage() {
        return message;
    }

    String getRecipient() {
        return recipient;
    }

    String getFileName() {
        return fileName;
    }
}
