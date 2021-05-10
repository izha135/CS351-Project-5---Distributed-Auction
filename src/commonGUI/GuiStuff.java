package commonGUI;

import common.AuctionHouseUser;
import common.Item;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class GuiStuff {
    private Label usernameLabel;
    private Label userIDAccountLabel;
    private Label userAccountBalanceLabel;
    private Label userBlockAmountLabel;
    private Label currentAuctionHouseLabel;
    private Label currentItemSelectedLabel;
    private TextField userBidAmountTextField;
    private Label bidHistoryLabel;
    private TextArea bidHistoryTextArea;

    private CustomLabel usernameCustomLabel;
    private CustomLabel userIDAccountCustomLabel;
    private CustomLabel userAccountBalanceCustomLabel;
    private CustomLabel userBlockAmountCustomLabel;
    private CustomLabel currentAuctionHouseCustomLabel;
    private CustomLabel currentItemSelectedCustomLabel;
    private CustomLabel bidHistoryCustomLabel;

    public GuiStuff(Label usernameLabel, Label userIDAccountLabel,
                    Label userAccountBalanceLabel,
                    Label currentAuctionHouseLabel, Label currentItemSelectedLabel,
                    TextField userBidAmountTextField, Label bidHistoryLabel,
                    Label userBlockAmountLabel, TextArea bidHistoryTextArea) {
        this.usernameLabel = usernameLabel;
        this.userIDAccountLabel = userIDAccountLabel;
        this.userAccountBalanceLabel = userAccountBalanceLabel;
        this.userBlockAmountLabel = userBlockAmountLabel;
        this.currentAuctionHouseLabel = currentAuctionHouseLabel;
        this.currentItemSelectedLabel = currentItemSelectedLabel;
        this.userBidAmountTextField = userBidAmountTextField;
        this.bidHistoryLabel = bidHistoryLabel;

        usernameCustomLabel = new CustomLabel(usernameLabel);
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
    public void updateUsernameLabel(String username) {
        usernameCustomLabel.updateLabel(username);
        usernameLabel.setText(usernameCustomLabel.getText());
    }

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

    /**
     * Should only be used for currentSelectedItemLabel and
     * currentAuctionHouseLabel
     *
     * The CustomLabel object corresponding to the Label given has to be
     * gotten to change the outputMessage member variable and update the
     * label correctly
     * @param label
     */
    public void resetLabel(Label label) {
//        Platform.runLater(() -> {
//            CustomLabel customLabel = new CustomLabel(label);
//            customLabel.resetLabel();
//            label.setText(customLabel.getText());
//        });

        CustomLabel currentCustomLabel;
        if (label == currentItemSelectedLabel) {
            currentCustomLabel = currentItemSelectedCustomLabel;
        } else if (label == currentAuctionHouseLabel) {
            currentCustomLabel = currentAuctionHouseCustomLabel;
        } else { // shouldn't happen...
            System.out.println();
            System.out.println("Error resetting the label...");

            showAlert(Alert.AlertType.ERROR,
                    "Error Resetting the Label",
                    "Wrong label being updated...");
            return;
        }

        currentCustomLabel.resetLabel();
        label.setText(currentCustomLabel.getText());
    }

    private void showAlert(Alert.AlertType alertType, String titleAlert,
                           String contextStringAlert) {
        CustomAlert bankExitAlert = new CustomAlert(
                alertType,
                titleAlert,
                contextStringAlert);
        bankExitAlert.show();
    }
}
