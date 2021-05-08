package common;

import java.util.ArrayList;
import java.util.List;

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
