package common;

import java.util.ArrayList;
import java.util.List;

/**
 * Class encapsulating any command (message received from the user end)
 *
 * Contains the original message as a string, the parsed out Message Enum
 * constant, and the parsed arguments given with the command as a list (helps
 * to break down the message into things where other parts of the code can use)
 */
public class FullMessage {
    private String messageString;
    private MessageEnum messageEnum;
    private List<String> messageArgs;

    public FullMessage(String messageString) {
        String[] separatedMessage = messageString.split(";");
        messageEnum = MessageEnum.parseCommand(separatedMessage[0]);
        messageArgs = MessageEnum.parseMessageArgs(messageString);
    }

    public String getMessageString() {
        return messageString;
    }

    public MessageEnum getMessageEnum() {
        return messageEnum;
    }

    public List<String> getMessageArgs() {
        return messageArgs;
    }

    @Override
    public String toString() {
        return messageEnum.name() + " " + messageArgs;
    }
}
