package commonGUI;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class GuiStuff {
    private Label userIDAccountLabel;
    private Label userAccountBalanceLabel;
    private Label currentItemSelectedLabel;
    private TextField userBidAmountTextField;
    private Label bidHistoryLabel;

    private CustomLabel userIDAccountCustomLabel;
    private CustomLabel userAccountBalanceCustomLabel;
    private CustomLabel currentItemSelectedCustomLabel;
    private CustomLabel bidHistoryCustomLabel;

    public GuiStuff(Label userIDAccountLabel, Label userAccountBalanceLabel,
                    Label currentItemSelectedLabel,
                    TextField userBidAmountTextField, Label bidHistoryLabel) {
        this.userIDAccountLabel = userIDAccountLabel;
        this.userAccountBalanceLabel = userAccountBalanceLabel;
        this.currentItemSelectedLabel = currentItemSelectedLabel;
        this.userBidAmountTextField = userBidAmountTextField;
        this.bidHistoryLabel = bidHistoryLabel;

        userIDAccountCustomLabel = new CustomLabel(userIDAccountLabel);
        userAccountBalanceCustomLabel =
                new CustomLabel(userAccountBalanceLabel);
        currentItemSelectedCustomLabel =
                new CustomLabel(currentItemSelectedLabel);
        bidHistoryCustomLabel = new CustomLabel(bidHistoryLabel);
    }

    public void updateUserIDAccountLabel(int userID) {
        userIDAccountCustomLabel.updateLabel(
                Integer.toString(userID));
        userIDAccountLabel.setText(userIDAccountCustomLabel.getText());
    }

    public void updateUserAccountBalanceLabel(double bidAmount) {

    }

    public void updateCurrentItemSelectedLabel() {

    }

    public double parseUserBidAmountTextField() {
        return 0.0;
    }

    public void updateBidHistoryLabel() {

    }
}
