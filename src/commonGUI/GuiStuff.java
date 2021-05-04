package commonGUI;

import common.AuctionHouseUser;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class GuiStuff {
    private Label userIDAccountLabel;
    private Label userAccountBalanceLabel;
    private Label currentAuctionHouseLabel;
    private Label currentItemSelectedLabel;
    private TextField userBidAmountTextField;
    private Label bidHistoryLabel;

    private CustomLabel userIDAccountCustomLabel;
    private CustomLabel userAccountBalanceCustomLabel;
    private CustomLabel currentAuctionHouseCustomLabel;
    private CustomLabel currentItemSelectedCustomLabel;
    private CustomLabel bidHistoryCustomLabel;

    public GuiStuff(Label userIDAccountLabel, Label userAccountBalanceLabel,
                    Label currentAuctionHouseLabel, Label currentItemSelectedLabel,
                    TextField userBidAmountTextField, Label bidHistoryLabel) {
        this.userIDAccountLabel = userIDAccountLabel;
        this.userAccountBalanceLabel = userAccountBalanceLabel;
        this.currentAuctionHouseLabel = currentAuctionHouseLabel;
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

    public void updateCurrentAuctionHouseLabel(
            AuctionHouseUser auctionHouseUser) {
        currentAuctionHouseCustomLabel.updateLabel(
                auctionHouseUser.toString());
        currentAuctionHouseLabel.setText(
                currentAuctionHouseCustomLabel.getText());
    }

    public void updateCurrentItemSelectedLabel() {

    }

    public double parseUserBidAmountTextField() {
        return 0.0;
    }

    public void updateBidHistoryLabel() {

    }
}
