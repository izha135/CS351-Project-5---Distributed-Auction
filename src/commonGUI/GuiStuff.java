package commonGUI;

import common.AuctionHouseUser;
import common.Item;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class GuiStuff {
    private Label userIDAccountLabel;
    private Label userAccountBalanceLabel;
    private Label userBlockAmountLabel;
    private Label currentAuctionHouseLabel;
    private Label currentItemSelectedLabel;
    private TextField userBidAmountTextField;
    private Label bidHistoryLabel;
    private TextArea bidHistoryTextArea;

    private CustomLabel userIDAccountCustomLabel;
    private CustomLabel userAccountBalanceCustomLabel;
    private CustomLabel userBlockAmountCustomLabel;
    private CustomLabel currentAuctionHouseCustomLabel;
    private CustomLabel currentItemSelectedCustomLabel;
    private CustomLabel bidHistoryCustomLabel;

    public GuiStuff(Label userIDAccountLabel, Label userAccountBalanceLabel,
                    Label currentAuctionHouseLabel, Label currentItemSelectedLabel,
                    TextField userBidAmountTextField, Label bidHistoryLabel,
                    Label userBlockAmountLabel, TextArea bidHistoryTextArea) {
        this.userIDAccountLabel = userIDAccountLabel;
        this.userAccountBalanceLabel = userAccountBalanceLabel;
        this.userBlockAmountLabel = userBlockAmountLabel;
        this.currentAuctionHouseLabel = currentAuctionHouseLabel;
        this.currentItemSelectedLabel = currentItemSelectedLabel;
        this.userBidAmountTextField = userBidAmountTextField;
        this.bidHistoryLabel = bidHistoryLabel;

        userIDAccountCustomLabel = new CustomLabel(userIDAccountLabel);
        userAccountBalanceCustomLabel =
                new CustomLabel(userAccountBalanceLabel);
        userBlockAmountCustomLabel = new CustomLabel(userBlockAmountLabel);
        currentAuctionHouseCustomLabel =
                new CustomLabel(currentAuctionHouseLabel);
        currentItemSelectedCustomLabel =
                new CustomLabel(currentItemSelectedLabel);
        bidHistoryCustomLabel = new CustomLabel(bidHistoryLabel);
        this.bidHistoryTextArea = bidHistoryTextArea;
    }

    // FIXME: update on startup...
    // shouldn't be needed except for the initial startup...
    public void updateUserIDAccountLabel(int userID) {
//        Platform.runLater(() -> {
//            userIDAccountCustomLabel.updateLabel(
//                    Integer.toString(userID));
//            userIDAccountLabel.setText(userIDAccountCustomLabel.getText());
//        });

        userIDAccountCustomLabel.updateLabel(
                Integer.toString(userID));
        userIDAccountLabel.setText(userIDAccountCustomLabel.getText());
    }

    public void updateUserAccountBalanceLabel(double accountBalance) {
//        Platform.runLater(() -> {
//            userAccountBalanceCustomLabel.updateLabel(
//                    Double.toString(accountBalance));
//            userAccountBalanceLabel.setText(
//                    userAccountBalanceCustomLabel.getText());
//        });

        userAccountBalanceCustomLabel.updateLabel(
                Double.toString(accountBalance));
        userAccountBalanceLabel.setText(
                userAccountBalanceCustomLabel.getText());
    }

    public void updateUserBlockAmountLabel(double bidAmount,
                                           boolean initialBid) {
//        Platform.runLater(() -> {
//            double blockAmount;
//            if (userBlockAmountCustomLabel.getOutputMessage().isEmpty()) {
//                blockAmount = 0.00;
//            } else {
//                blockAmount =
//                        Double.parseDouble(
//                                userBlockAmountCustomLabel.getOutputMessage());
//            }
//
//            if (initialBid) {
//                blockAmount += bidAmount;
//            } else {
//                blockAmount -= bidAmount;
//            }
//
//            userBlockAmountCustomLabel.updateLabel(
//                    Double.toString(blockAmount));
//            userBlockAmountLabel.setText(userBlockAmountCustomLabel.getText());
//        });

        double blockAmount;
        if (userBlockAmountCustomLabel.getOutputMessage().isEmpty()) {
            blockAmount = 0.00;
        } else {
            blockAmount =
                    Double.parseDouble(
                            userBlockAmountCustomLabel.getOutputMessage());
        }

        if (initialBid) {
            blockAmount += bidAmount;
        } else {
            blockAmount -= bidAmount;
        }

        userBlockAmountCustomLabel.updateLabel(
                Double.toString(blockAmount));
        userBlockAmountLabel.setText(userBlockAmountCustomLabel.getText());
    }

    public void addFundsToBalanceLabel(double bidAmount) {
//        Platform.runLater(() -> {
//            double accountBalance =
//                    userAccountBalanceCustomLabel.getDoubleOutputMessage();
//            accountBalance += bidAmount;
//
//            updateUserAccountBalanceLabel(accountBalance);
//        });

        double accountBalance =
                userAccountBalanceCustomLabel.getDoubleOutputMessage();
        accountBalance += bidAmount;

        updateUserAccountBalanceLabel(accountBalance);
    }

    public void removeFundsFromBalanceLabel(double bidAmount) {
//        Platform.runLater(() -> {
//            double accountBalance =
//                    userAccountBalanceCustomLabel.getDoubleOutputMessage();
//            accountBalance -= bidAmount;
//
//            updateUserAccountBalanceLabel(accountBalance);
//        });

        double accountBalance =
                userAccountBalanceCustomLabel.getDoubleOutputMessage();
        accountBalance -= bidAmount;

        updateUserAccountBalanceLabel(accountBalance);
    }

    public void updateCurrentAuctionHouseLabel(
            AuctionHouseUser auctionHouseUser) {
//        Platform.runLater(() -> {
//            currentAuctionHouseCustomLabel.updateLabel(
//                    auctionHouseUser.toString());
//            currentAuctionHouseLabel.setText(
//                    currentAuctionHouseCustomLabel.getText());
//        });

        currentAuctionHouseCustomLabel.updateLabel(
                auctionHouseUser.toString());
        currentAuctionHouseLabel.setText(
                currentAuctionHouseCustomLabel.getText());
    }

    public void updateCurrentItemSelectedLabel(Item item) {
//        Platform.runLater(() -> {
//            currentItemSelectedCustomLabel.updateLabel(
//                    item.getTreeItemTitle());
//            currentItemSelectedLabel.setText(
//                    currentItemSelectedCustomLabel.getText());
//        });

        currentItemSelectedCustomLabel.updateLabel(
                item.getTreeItemTitle());
        currentItemSelectedLabel.setText(
                currentItemSelectedCustomLabel.getText());
    }

    public double parseUserBidAmountTextField() {
        return Double.parseDouble(
                userBidAmountTextField.getText());
    }

    // FIXME: remove and replace with the TextArea...
    public void updateBidHistoryLabel() {

    }

    public void updateBidHistoryTextArea(String bidEntry) {
//        Platform.runLater(() -> {
//            String currentBidHistory = bidHistoryTextArea.getText();
//            currentBidHistory += bidEntry + "\n";
//            // FIXME: don't know how new line will affect the text area...
//
//            bidHistoryTextArea.setText(currentBidHistory);
//        });

        String currentBidHistory = bidHistoryTextArea.getText();
        currentBidHistory += bidEntry + "\n";
        // FIXME: don't know how new line will affect the text area...

        bidHistoryTextArea.setText(currentBidHistory);
    }

    public void resetLabel(Label label) {
//        Platform.runLater(() -> {
//            CustomLabel customLabel = new CustomLabel(label);
//            customLabel.resetLabel();
//            label.setText(customLabel.getText());
//        });

        CustomLabel customLabel = new CustomLabel(label);
        customLabel.resetLabel();
        label.setText(customLabel.getText());
    }
}
