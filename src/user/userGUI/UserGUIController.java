package user.userGUI;

import common.*;
import commonGUI.CustomAuctionHouseTreeItem;
import commonGUI.CustomItemTreeItem;
import commonGUI.GuiStuff;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import user.Bid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static bank.BankListener.*;
import static common.MessageEnum.*;
import static user.userGUI.UserGUIApp.*;

public class UserGUIController {
    @FXML
    Pane pane;

    @FXML
    Label userIDAccountLabel;

    @FXML
    Label userAccountBalanceLabel;

    @FXML
    Label userBlockAmountLabel;

    @FXML
    Label currentAuctionHouseLabel;

    @FXML
    Label currentItemSelectedLabel;

    @FXML
    TextField userBidAmountTextField;

    @FXML
    Button bidButton;

    @FXML
    Label bidHistoryLabel;

    @FXML
    TextArea bidHistoryTextArea;

    @FXML
    Button userExitButton;

    // TODO: add refresh buttons

    private GuiStuff guiStuff;

    // FIXME: change from CustomAuctionHouseTreeItem
    private TreeItem<String> rootTreeItem;
    private List<CustomAuctionHouseTreeItem> houseTreeItemList = new ArrayList<>();

    private String bankHostName = "10.147.20.205";
    private int port = 3030;
    private int userID;
    private BankAccount userBankAccount;
    private List<Integer> houseIDsList = new ArrayList<>();
    private List<AuctionHouseUser> entireHousesList = new ArrayList<>();
    private List<Item> houseItemList = new ArrayList<>();

    private Socket bankSocket;
    private PrintWriter bankWriter;
    private BufferedReader bankReader;

    private Socket houseSocket;
    private PrintWriter houseWriter;
    private BufferedReader houseReader;

    private TreeView<String> houseItemTreeView;
    private CustomAuctionHouseTreeItem currentAuctionHouseTreeItem;
    private CustomAuctionHouseTreeItem currentItemTreeItem;
    private AuctionHouseUser currentAuctionHouseUser;
    private Item currentItemSelected;

    private List<FullMessage> fullMessagesActionList = new ArrayList<>();

    private class CustomTreeCell extends TreeCell<String> {
        // FIXME
        private ContextMenu connectContextMenu = new ContextMenu();

        public CustomTreeCell() {
            TreeItem<String> treeItem = getTreeItem();

            System.out.println();
            System.out.println("Entering constructor of Tree Cell");
            System.out.println("Tree item " + treeItem);

//            // maybe this will work...
//            if (treeItem instanceof CustomAuctionHouseTreeItem) {
//                System.out.println("Auction House: Tree Cell...");
//
//                MenuItem connectMenuItem = new MenuItem("Connect to this Auction " +
//                        "House");
//                connectContextMenu.getItems().add(connectMenuItem);
//
//                connectMenuItem.setOnAction(event -> {
//                    // send request to connect to the chosen auction house
//                    String[] houseTreeItemArgs = treeItem.getValue().split(" ");
//                    String houseID = houseTreeItemArgs[1];
//                    int parseHouseID = Integer.parseInt(houseID);
//
//                    for (CustomAuctionHouseTreeItem houseTreeItem : houseTreeItemList) {
//                        if (houseTreeItem.checkID(parseHouseID)) {
//                            currentAuctionHouseTreeItem = houseTreeItem;
//                            break;
//                        }
//                    }
//
//                    //TODO: update label, get items
//                    AuctionHouseUser auctionHouseUser =
//                            currentAuctionHouseTreeItem.getAuctionHouseUser();
//                    String houseHostName = auctionHouseUser.getHouseHostName();
//
//                    System.out.println();
//                    System.out.println("Initializing auction house connection" +
//                            " with: \n" + auctionHouseUser);
//
//                    initializeHouseConnection(houseHostName);
//
//                    System.out.println("Connection successful");
//
//                    System.out.println();
//                    System.out.println("Updating the corresponding item " +
//                            "list...");
//
//                    updateHouseItemList();
//
//                    System.out.println("Update successful");
//
//                    guiStuff.updateCurrentAuctionHouseLabel(
//                            auctionHouseUser);
//                });
//            } else if (treeItem instanceof CustomItemTreeItem) {
//                System.out.println("Item: Tree Cell...");
//
//                CustomItemTreeItem customItemTreeItem =
//                        (CustomItemTreeItem) treeItem;
//                Item item = customItemTreeItem.getItem();
//                this.setOnMousePressed(event -> itemMousePress(event,
//                        item));
//
//                guiStuff.updateCurrentItemSelectedLabel(item);
//            }
        }

        @Override
        public void updateItem(String itemString, boolean empty) {
            super.updateItem(itemString, empty) ;

            if (empty) {
                setText(null);
            } else {
                setText(itemString);

                TreeItem<String> treeItem = getTreeItem();

                System.out.println();
                System.out.println("Updating tree item...");
                System.out.println("Tree item " + treeItem);

                // maybe this will work...
                // FIXME: add if(connectContextMenu.getItems().isEmpty())
                if (treeItem instanceof CustomAuctionHouseTreeItem) {
                    if (connectContextMenu.getItems().isEmpty()) {
                        System.out.println("Auction House: Tree Cell...");

                        MenuItem connectMenuItem = new MenuItem("Connect to this Auction " +
                                "House");
                        connectContextMenu.getItems().add(connectMenuItem);

                        System.out.println();
                        System.out.println("Setting the context menu in the " +
                                "update");

                        setContextMenu(connectContextMenu);

                        connectMenuItem.setOnAction(event -> {
                            // send request to connect to the chosen auction house
//                        String[] houseTreeItemArgs = treeItem.getValue().split(" ");
//                        String houseID = houseTreeItemArgs[1];

                            // FIXME: changed how the houseID is gotten
                            CustomAuctionHouseTreeItem currentHouseTreeItem =
                                    (CustomAuctionHouseTreeItem) treeItem;
                            AuctionHouseUser auctionHouseUser =
                                    currentHouseTreeItem.getAuctionHouseUser();
                            int parseHouseID =
                                    auctionHouseUser.getHouseID();

                            for (CustomAuctionHouseTreeItem houseTreeItem : houseTreeItemList) {
                                if (houseTreeItem.checkID(parseHouseID)) {
                                    currentAuctionHouseTreeItem = houseTreeItem;
                                    currentAuctionHouseUser =
                                            currentAuctionHouseTreeItem.getAuctionHouseUser();
                                    break;
                                }
                            }

                            //TODO: update label, get items
//                        AuctionHouseUser auctionHouseUser =
//                                currentAuctionHouseTreeItem.getAuctionHouseUser();

                            String houseHostName = currentAuctionHouseUser.getHouseHostName();

                            System.out.println();
                            System.out.println("Initializing auction house connection" +
                                    " with: \n" + currentAuctionHouseUser);

                            initializeHouseConnection(houseHostName);

                            System.out.println("Connection successful");

                            System.out.println();
                            System.out.println("Updating the corresponding item " +
                                    "list...");

                            updateHouseItemList();

                            System.out.println("Update successful");

                            guiStuff.updateCurrentAuctionHouseLabel(
                                    currentAuctionHouseUser);
                        });
                    } else {
                        System.out.println();
                        System.out.println("Auction house connect context " +
                                "menu already initialized");
                    }
                } else if (treeItem instanceof CustomItemTreeItem) {
                    if (treeItem.isLeaf()
                            && getTreeItem().getParent() != null){
                        System.out.println("Item: Tree Cell...");

                        CustomItemTreeItem customItemTreeItem =
                                (CustomItemTreeItem) treeItem;

                        Item item = customItemTreeItem.getItem();

                        System.out.println("Current item selected: " + item);

                        this.setOnMousePressed(event -> itemMousePress(event,
                                item));

                        guiStuff.updateCurrentItemSelectedLabel(item);
                    } else {
                        System.out.println();
                        System.out.println("No update...");
                    }
                }

//                if (treeItem instanceof CustomAuctionHouseTreeItem) {
//                    System.out.println();
//                    System.out.println("Setting the context menu in the " +
//                            "update");
//
//                    setContextMenu(connectContextMenu);
//                }
            }
        }
    }

    private class UserGUIActionListener extends Thread {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private boolean run;

        public UserGUIActionListener(Socket socket,
                                     BufferedReader reader,
                                     PrintWriter writer) {
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
            this.run = true;
        }

        @Override
        public void run() {
            while (run) {
                while (!fullMessagesActionList.isEmpty()) {
                    FullMessage currentFullMessage =
                            fullMessagesActionList.remove(0);
                    MessageEnum messageEnum =
                            currentFullMessage.getMessageEnum();
                    List<String> messageArgs =
                            currentFullMessage.getMessageArgs();

                    // only two action listeners: bank or current house
                    // connected
                    if (socket == bankSocket) {
                        switch (messageEnum) {

                        }
                    } else {
                        switch (messageEnum) {

                        }
                    }
                }
            }
        }

        public void stopRunning() {
            run = false;
        }
    }

    private void initializeLabels() {
        guiStuff.updateUserIDAccountLabel(userID);
        guiStuff.updateUserAccountBalanceLabel(
                userBankAccount.getBalance());
    }

    @FXML
    public void initialize() throws InterruptedException {
        //String hostName = "localhost";
        //int port = 3030;

        //try (Socket socket = new Socket(hostName, port);
        //     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        //     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));){

        // FIXME: now with two separate IO, maybe use a file or something
        //  (for the house) for testing...
        System.out.println();
        System.out.println("Initializing the bank connection...");

        initializeBankConnection();

        System.out.println("Connection successful");

        System.out.println();
        System.out.println("Printing user...");
        bankWriter.println("user;" + userID);

        System.out.println();
        System.out.println("Setting up user ID...");

        setUserID();

        System.out.println("Finished...user ID: " + userID);

        // default id of -1 at the root
        // FIXME: change to just NORMAL Tree Item
//        rootTreeItem = new CustomAuctionHouseTreeItem(
//                "List of available Auction Houses",
//                null);
        rootTreeItem = new TreeItem<>(
                "List of available " +
                "Auction Houses");
        rootTreeItem.setExpanded(true);

        houseItemTreeView = new TreeView<>();
        houseItemTreeView.setRoot(rootTreeItem);
        // FIXME
        //houseItemTreeView.setCellFactory(param -> new CustomTreeCell());
        houseItemTreeView.setCellFactory(param -> new CustomTreeCell());

        //pane.getChildren().clear();
        pane.getChildren().add(houseItemTreeView);

        System.out.println();
        System.out.println("Updating the auction house list...");

        updateAuctionHouseList();

        System.out.println("Finished...");
        System.out.println("Auction house list:");
        for (AuctionHouseUser auctionHouseUser : entireHousesList) {
            System.out.println(auctionHouseUser);
        }

        guiStuff = new GuiStuff(userIDAccountLabel,
                userAccountBalanceLabel,
                currentAuctionHouseLabel,
                currentItemSelectedLabel,
                userBidAmountTextField,
                bidHistoryLabel,
                userBlockAmountLabel,
                bidHistoryTextArea);

        initializeLabels();

        bidButton.setOnAction(event -> bidButtonOnAction());

        // don't know what the difference is with lambda and method reference...
        userExitButton.setOnAction(this::exitRequestCheck);

        //}
        //catch (Exception e) {
        //    e.printStackTrace();
        //}
    }

    private void updateHouseListTreeView() {
        houseTreeItemList.clear();

        System.out.println();
        System.out.println("House tree items:");

        rootTreeItem.getChildren().clear();
        for (AuctionHouseUser auctionHouseUser : entireHousesList) {
            //TreeItem<String> houseTreeView = new TreeItem<>(houseID);
            int houseID = auctionHouseUser.getHouseID();
            CustomAuctionHouseTreeItem houseTreeView =
                    new CustomAuctionHouseTreeItem("House ID: " + houseID,
                            auctionHouseUser);
            rootTreeItem.getChildren().add(houseTreeView);
            houseTreeItemList.add(houseTreeView);

            // FIXME
            System.out.println(houseTreeView);
        }
    }

    // TODO: implement
    private void bidButtonOnAction() {
//        String bidReturnMessage;
//        while (true) {
//            try {
//                if (houseReader.ready()) {
//                    bidReturnMessage = houseReader.readLine();
//                    break;
//                }
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }
//        }

        if (currentItemSelected == null) {
            Alert invalidItemSelectedAlert = new Alert(Alert.AlertType.ERROR);
            invalidItemSelectedAlert.setTitle("Invalid Item selected");
            invalidItemSelectedAlert.setContentText("Please select an item before making a bid");

            invalidItemSelectedAlert.show();
            return;
        }

        System.out.println();
        System.out.println("Current item selected: " + currentItemSelected);
        System.out.println("Current auction house: " + currentAuctionHouseUser);

        double bidAmount = Double.parseDouble(
                userBidAmountTextField.getText());
        int itemID = currentItemSelected.getItemId();
        int houseID = currentAuctionHouseUser.getHouseID();

        // FIXME: bidWrite
        bidButtonWrite(bidAmount, itemID, houseID);

        FullMessage bidFullMessage =
                getFullMessageFromReader(houseReader);
        MessageEnum bidMessageEnum =
                bidFullMessage.getMessageEnum();

        Alert bidStatusAlert;
        Bid bid;
        switch (bidMessageEnum) {
            case ACCEPT:
                bidStatusAlert =
                        new Alert(
                                Alert.AlertType.CONFIRMATION);
                bidStatusAlert.setTitle(bidMessageEnum.name());
                bidStatusAlert.setContentText("Your bid of $"
                        + bidAmount + " on item "
                        + currentItemSelected.getTreeItemTitle()
                        + " was accepted!");

                userBankAccount.removeFunds(bidAmount);

                guiStuff.updateUserAccountBalanceLabel(bidAmount);
                guiStuff.updateUserBlockAmountLabel(bidAmount,
                        true);

                bid = new Bid(bidAmount, currentItemSelected,
                        bidMessageEnum);
                guiStuff.updateBidHistoryTextArea(bid.toString());
                break;
            case REJECT:
                bidStatusAlert =
                        new Alert(Alert.AlertType.WARNING);
                bidStatusAlert.setTitle(bidMessageEnum.name());
                bidStatusAlert.setContentText("Sorry, your bid of $"
                        + bidAmount + " on item "
                        + currentItemSelected.getTreeItemTitle()
                        + " was rejected...Better luck next time!");

                bid = new Bid(bidAmount, currentItemSelected,
                        bidMessageEnum);
                guiStuff.updateBidHistoryTextArea(bid.toString());
                break;
            default:
                bidStatusAlert =
                        new Alert(Alert.AlertType.ERROR);
                bidStatusAlert.setTitle(bidMessageEnum.name());
                bidStatusAlert.setContentText("System error..." +
                        "Wrong message given by network...");

                bid = new Bid(bidAmount, currentItemSelected,
                        ERROR);
                guiStuff.updateBidHistoryTextArea(bid.toString());
                break;
        }

        bidStatusAlert.show();
    }

    private void bidButtonWrite(double bidAmount, int itemID, int houseID) {
        List<String> bidArgs = Arrays.asList(Integer.toString(userID),
                Double.toString(bidAmount),
                Integer.toString(itemID),
                Integer.toString(houseID));

        String bidMessage = MessageEnum.createMessageString(BID,
                bidArgs);

        houseWriter.println(bidMessage);
    }

    private void bidButtonRead() {

    }

    private void initializeBankConnection() {
        try {
            bankSocket = new Socket(bankHostName, bankPort);
            bankWriter = new PrintWriter(bankSocket.getOutputStream(),
                    true);
            bankReader = new BufferedReader(
                    new InputStreamReader(bankSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void initializeHouseConnection(String houseHostName) {
        try {
            houseSocket = new Socket(houseHostName, housePort);
            houseWriter = new PrintWriter(houseSocket.getOutputStream(),
                    true);
            houseReader = new BufferedReader(
                    new InputStreamReader(houseSocket.getInputStream()));

            Thread.sleep(1000);

            System.out.println();
            System.out.println("Checking the house socket " + houseSocket);
            System.out.println("Checking the house reader " + houseReader);
            System.out.println("Checking the house writer " + houseWriter);
            System.out.println("Sending the user ID to the house...");
            houseWriter.println(userID);
            System.out.println("Completed");

//            while (true) {
//                if (houseReader.ready()) {
//                    String houseMessage = houseReader.readLine();
//                    System.out.println();
//                    System.out.println("Message from house: " + houseMessage);
//                    break;
//                }
//            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateHouseItemList() {
        houseItemList.clear();

        houseWriter.println(MessageEnum.createMessageString(GET_ITEMS,
                new ArrayList<>()));

//        String itemsMessage;
//        while (true) {
//            try {
//                if (houseReader.ready()) {
//                    itemsMessage = houseReader.readLine();
//                    break;
//                }
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }
//        }

        FullMessage houseItemListFullMessage =
                getFullMessageFromReader(houseReader);
        List<String> houseItemListArgsList =
                houseItemListFullMessage.getMessageArgs();

        System.out.println();
        System.out.println("House Item List args: " + houseItemListArgsList);

        int houseID = Integer.parseInt(houseItemListArgsList.get(0));
        int itemCount = Integer.parseInt(houseItemListArgsList.get(1));

        houseItemListArgsList.remove(0);
        houseItemListArgsList.remove(0);

        int itemArgIndex = 0;
        for (int i = 0; i < itemCount; i++) {
            itemArgIndex = i * 4;
            houseItemList.add(new Item(houseItemListArgsList.get(itemArgIndex),
                    Integer.parseInt(houseItemListArgsList.get(itemArgIndex + 1)),
                    Double.parseDouble(houseItemListArgsList.get(itemArgIndex + 2)),
                    houseItemListArgsList.get(itemArgIndex + 3)));
        }

        // add items to the current house tree item
        for (Item item : houseItemList) {
            CustomItemTreeItem itemRootTreeItem =
                    new CustomItemTreeItem(
                            item.getTreeItemTitle(), item);

            // add event handler (click) for each item
            // MOVED TO CustomTreeCell ABOVE
            // FIXME: remove label
//                        Label itemBodyLabel = new Label(item.toString());
//                        itemBodyLabel.setOnMousePressed(event -> itemMousePress(
//                                event, item));

            //customItemTreeItem.getChildren().add(new TreeItem<>
            // (item.toString()));
            itemRootTreeItem.getChildren().add(
                    new CustomItemTreeItem(
                            item.toString(), item));
            currentAuctionHouseTreeItem.getChildren().add(
                    itemRootTreeItem);
        }

        // alert for items
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Items Notification");
        alert.setContentText("Successfully initialized " + itemCount +
                " items from house id: " + houseID);

        alert.show();
    }

    private void itemMousePress(MouseEvent mouseEvent, Item item) {
        currentItemSelected = item;
    }

    // get the list of auction houses
    private void updateAuctionHouseList() {
        List<String> getHousesArgs = new ArrayList<>();
        getHousesArgs.add(Integer.toString(userID));
        String getHousesMessage =
                MessageEnum.createMessageString(GET_HOUSES,
                        getHousesArgs);
        bankWriter.println(getHousesMessage);

        FullMessage houseListMessage = getFullMessageFromReader(
                bankReader);
        List<String> housesArgs = houseListMessage.getMessageArgs();

//        while (true) {
//            try {
//                if (bankReader.ready()) {
//                    String housesMessage = bankReader.readLine();
//                    housesArgs =
//                            MessageEnum.parseMessageArgs(housesMessage);
//                    break;
//                }
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }
//        }

        System.out.println();
        System.out.println("House args:");
        System.out.println(housesArgs);

        entireHousesList.clear();
        for (int i = 0; i < housesArgs.size(); i += 2) {
            entireHousesList.add(new AuctionHouseUser(Integer.parseInt(
                    housesArgs.get(i)), housesArgs.get(i + 1)));
        }

        updateHouseIDsList();
        updateHouseListTreeView();
    }

    private void updateHouseIDsList() {
        houseIDsList.clear();

        for (AuctionHouseUser auctionHouseUser : entireHousesList) {
            int houseID = auctionHouseUser.getHouseID();
            houseIDsList.add(houseID);
        }
    }

    // get the user id
    private void setUserID() {
//        while (true) {
//            try {
//                if (bankReader.ready()) {
//                    String userIDMessage = bankReader.readLine();
//                    List<String> userIDArgs =
//                            MessageEnum.parseMessageArgs(userIDMessage);
//                    userID = Integer.parseInt(userIDArgs.get(0));
//
//                    // account ID is the same as the user ID
//                    userBankAccount = new BankAccount(INITIAL_BALANCE,
//                            userID);
//                    break;
//                }
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }
//        }

        FullMessage userIDMessage = getFullMessageFromReader(
                bankReader);
        List<String> userIDArgs =
                userIDMessage.getMessageArgs();

        userID = Integer.parseInt(userIDArgs.get(0));

        // account ID is the same as the user ID
        userBankAccount = new BankAccount(INITIAL_BALANCE,
                userID);
    }

    private FullMessage getFullMessageFromReader(BufferedReader currentReader) {
        String returnMessage;
        while (true) {
            try {
                if (currentReader.ready()) {
                    returnMessage = currentReader.readLine();
                    break;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println();
        System.out.println("Message received: " + returnMessage);

        return new FullMessage(returnMessage);
    }

    private void exitRequestCheck(Event event) {
        List<String> exitBankArgs = new ArrayList<>();
        exitBankArgs.add(Integer.toString(userID));

        List<String> exitHouseArgs = new ArrayList<>();
        exitHouseArgs.add(
                Integer.toString(userID));

        bankWriter.println(
                MessageEnum.createMessageString(EXIT,
                        exitBankArgs));
        houseWriter.println(MessageEnum.createMessageString(EXIT,
                exitHouseArgs));

        FullMessage exitBankMessage = getFullMessageFromReader(
                bankReader);
        FullMessage exitHouseMessage = getFullMessageFromReader(
                houseReader);

        MessageEnum exitBankMessageEnum = exitBankMessage.getMessageEnum();
        MessageEnum exitHouseMessageEnum = exitHouseMessage.getMessageEnum();

        if (exitBankMessageEnum == CAN_EXIT
                && exitHouseMessageEnum == CAN_EXIT) {
            // get a handle to the stage
            Stage stage = (Stage) userExitButton.getScene().getWindow();
            stage.close();
        } else {
            Alert exitAlert = new Alert(Alert.AlertType.ERROR);
            exitAlert.setTitle("Exit Auction House Error");
            exitAlert.setContentText("Sorry, you are unable to exit due to " +
                    "various reasons (bids still in progress)...");

            exitAlert.show();

            event.consume();
        }
    }
}
