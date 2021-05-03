package commonGUI;

import javafx.scene.control.Label;

public class GuiStuff {
    private Label userIDAccountLabel;
    private Label userAccountBalanceLabel;

    private CustomLabel userIDAccountCustomLabel;
    private CustomLabel userAccountBalanceCustomLabel;

    public GuiStuff(Label userIDAccountLabel, Label userAccountBalanceLabel) {
        this.userIDAccountLabel = userIDAccountLabel;
        this.userAccountBalanceLabel = userAccountBalanceLabel;

        userIDAccountCustomLabel = new CustomLabel(userIDAccountLabel);
        userAccountBalanceCustomLabel =
                new CustomLabel(userAccountBalanceLabel);
    }

    public void updateUserIDAccountLabel() {

    }

    public void updateUserAccountBalanceLabel(double bidAmount) {

    }
}
