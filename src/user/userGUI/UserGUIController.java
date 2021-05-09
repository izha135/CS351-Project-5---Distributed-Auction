package user.userGUI;

import common.*;
import commonGUI.*;
import javafx.application.Platform;
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
import java.util.*;

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

    @FXML
    Button exitHouseButton;

    @FXML
    Button exitBankButton;

    // TODO: add refresh buttons
    @FXML
    Button refreshHouseListButton;

    @FXML
    Button refreshItemListButton;

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

    private final List<Bid> currentBidList = new ArrayList<>();

    private final List<FullMessage> bankMessagesActionList = new ArrayList<>();
    private final List<FullMessage> houseMessagesActionList = new ArrayList<>();

    private boolean checkBankExit = false;
    private boolean checkHouseExit = false;

    private UserGUIReaderListener bankReaderListener;
    private UserGUIActionListener bankActionListener;

    private UserGUIReaderTimer bankReaderTimer;
    private UserGUIActionTimer bankActionTimer;

    private UserGUIReaderListener houseReaderListener;
    private UserGUIActionListener houseActionListener;

    private UserGUIReaderTimer houseReaderTimer;
    private UserGUIActionTimer houseActionTimer;

    @FXML
    public void initialize() throws InterruptedException {
        //String hostName = "localhost";
        //int port = 3030;

        //try (Socket socket = new Socket(hostName, port);
        //     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        //     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));){

        System.out.println("Starting up the user program...");
        System.out.println("Initializing...");

        // FIXME: moved all calls of Platform.runLater to here...
        // FIXME: might need to add it to the listeners as well...

        // FIXME: now with two separate IO, maybe use a file or something
        //  (for the house) for testing...

        initializeBankConnection();

        //getUserID();

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

        //updateAuctionHouseList();

        guiStuff = new GuiStuff(userIDAccountLabel,
                userAccountBalanceLabel,
                currentAuctionHouseLabel,
                currentItemSelectedLabel,
                userBidAmountTextField,
                bidHistoryLabel,
                userBlockAmountLabel,
                bidHistoryTextArea);

        bidButton.setOnAction(event -> bidButtonOnAction());

        // TODO: implement EXIT buttons
        // don't know what the difference is with lambda and method reference...
        userExitButton.setOnAction(this::exitRequestCheck);

        exitHouseButton.setOnAction(this::exitHouseButtonOnAction);

        // same as general exit button, but button more explicit to disconnect
        // with the bank specifically
        exitBankButton.setOnAction(this::exitRequestCheck);

        // TODO: implement REFRESH buttons
        refreshHouseListButton.setOnAction(event -> askAuctionHouseList());

        refreshItemListButton.setOnAction(event -> askHouseItemList());

        //}
        //catch (Exception e) {
        //    e.printStackTrace();
        //}
    }

    private void initializeLabels() {
        guiStuff.updateUserIDAccountLabel(userID);
        guiStuff.updateUserAccountBalanceLabel(
                userBankAccount.getBalance());

        guiStuff.resetLabel(currentAuctionHouseLabel);
        guiStuff.resetLabel(currentItemSelectedLabel);
        guiStuff.updateUserBlockAmountLabel(0.00, true);
    }

    private synchronized void updateHouseListTreeView() {
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

    private void exitHouseButtonOnAction(Event event) {
        System.out.println();
        System.out.println("Breaking connection with House ID: "
                + currentAuctionHouseUser.getHouseID() + "...");

        List<String> houseExitArgs = Collections.singletonList(
                Integer.toString(userID));
        houseWriter.println(MessageEnum.createMessageString(EXIT,
                houseExitArgs));
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
//            Alert invalidItemSelectedAlert = new Alert(Alert.AlertType.ERROR);
//            invalidItemSelectedAlert.setTitle("Invalid Item selected");
//            invalidItemSelectedAlert.setContentText("Please select an item before making a bid");
//
//            invalidItemSelectedAlert.show();

            showAlert(Alert.AlertType.ERROR,
                    "Invalid Item selected",
                    "Please select an item before making a bid");
            return;
        }

        System.out.println();
        System.out.println("Current auction house: " + currentAuctionHouseUser);
        System.out.println("Current item selected: " + currentItemSelected);

        double bidAmount = Double.parseDouble(
                userBidAmountTextField.getText());
        int itemID = currentItemSelected.getItemId();
        int houseID = currentAuctionHouseUser.getHouseID();

        if (Double.compare(userBankAccount.getBalance(), bidAmount) < 0) {
//            CustomAlert notEnoughFundsAlert =
//                    new CustomAlert(Alert.AlertType.ERROR,
//                            "Not Enough Funds",
//                            "Sorry, you do not have enough funds " +
//                                    "to place that bid. Please try again...");
//            notEnoughFundsAlert.show();

            showAlert(Alert.AlertType.ERROR,
                    "Not Enough Funds",
                    "Sorry, you do not have enough funds " +
                            "to place that bid. Please try again...");

            return;
        }

        // FIXME: changed implementation of Bid object
        Bid currentBid = new Bid(bidAmount, houseID,
                currentItemSelected);

        System.out.println();
        System.out.println("Current bid: " + currentBid);

        System.out.println();
        System.out.println("List of bids so far:");
        for (Bid bid : currentBidList) {
            System.out.println(bid);
        }

        System.out.println("Check: !currentBidList.contains(currentBid) " +
                !currentBidList.contains(currentBid));

        if (!currentBidList.contains(currentBid)) {
            currentBidList.add(currentBid);

            userBankAccount.removeFunds(bidAmount);

            // TODO: check if the same item was already bid on...
            guiStuff.removeFundsFromBalanceLabel(bidAmount);
            guiStuff.updateUserBlockAmountLabel(bidAmount,
                    true);
        } else {
//            CustomAlert bidInProgressAlert =
//                    new CustomAlert(Alert.AlertType.ERROR,
//                            "Bid in Progress",
//                            "Sorry, you cannot place multiple " +
//                                    "bids on the same item until your bid " +
//                                    "was processed...");
//            bidInProgressAlert.show();

            showAlert(Alert.AlertType.ERROR,
                    "Bid in Progress",
                    "Sorry, you cannot place multiple " +
                            "bids on the same item until your bid " +
                            "was processed...");

            return;
        }

        // FIXME: broken up the bid for the listeners...
        askBid(currentBid);

//        FullMessage bidFullMessage =
//                getFullMessageFromReader(houseReader);
//        MessageEnum bidMessageEnum =
//                bidFullMessage.getMessageEnum();
//
//        Alert bidStatusAlert;
//        Bid bid;
//        switch (bidMessageEnum) {
//            case ACCEPT:
//                bidStatusAlert =
//                        new Alert(
//                                Alert.AlertType.CONFIRMATION);
//                bidStatusAlert.setTitle(bidMessageEnum.name());
//                bidStatusAlert.setContentText("Your bid of $"
//                        + bidAmount + " on item "
//                        + currentItemSelected.getTreeItemTitle()
//                        + " was accepted!");
//
//                userBankAccount.removeFunds(bidAmount);
//
//                guiStuff.updateUserAccountBalanceLabel(bidAmount);
//                guiStuff.updateUserBlockAmountLabel(bidAmount,
//                        true);
//
//                bid = new Bid(bidAmount, houseID, currentItemSelected,
//                        bidMessageEnum);
//                guiStuff.updateBidHistoryTextArea(bid.toString());
//                break;
//            case REJECT:
//                bidStatusAlert =
//                        new Alert(Alert.AlertType.WARNING);
//                bidStatusAlert.setTitle(bidMessageEnum.name());
//                bidStatusAlert.setContentText("Sorry, your bid of $"
//                        + bidAmount + " on item "
//                        + currentItemSelected.getTreeItemTitle()
//                        + " was rejected...Better luck next time!");
//
//                bid = new Bid(bidAmount, houseID, currentItemSelected,
//                        bidMessageEnum);
//                guiStuff.updateBidHistoryTextArea(bid.toString());
//                break;
//            default:
//                bidStatusAlert =
//                        new Alert(Alert.AlertType.ERROR);
//                bidStatusAlert.setTitle(bidMessageEnum.name());
//                bidStatusAlert.setContentText("System error..." +
//                        "Wrong message given by network...");
//
//                bid = new Bid(bidAmount, houseID, currentItemSelected,
//                        ERROR);
//                guiStuff.updateBidHistoryTextArea(bid.toString());
//                break;
//        }
//
//        bidStatusAlert.show();
    }

    private void askBid(Bid currentBid) {
        double bidAmount = currentBid.getBidAmount();
        int itemID = currentBid.getItem().getItemId();
        int houseID = currentBid.getHouseID();

        List<String> bidArgs = Arrays.asList(Integer.toString(userID),
                Double.toString(bidAmount),
                Integer.toString(itemID),
                Integer.toString(houseID));

        String bidMessage = createMessageString(BID,
                bidArgs);

        System.out.println();
        System.out.println("Asking for bid with bid amount: " + bidAmount +
                ", item ID: " + itemID + ", House ID: " + houseID);

        houseWriter.println(bidMessage);
    }

    private void getBid(FullMessage bidFullMessage) {
        MessageEnum bidMessageEnum =
                bidFullMessage.getMessageEnum();
        List<String> bidArgs = bidFullMessage.getMessageArgs();

        int houseID = Integer.parseInt(bidArgs.get(0));
        int currentItemID = Integer.parseInt(bidArgs.get(1));

        Alert bidStatusAlert;

        // FIXME: get the bid with the given item ID...
        Bid currentBid = Bid.getBidFromItemID(
                currentBidList, currentItemID);

        assert currentBid != null;
        double bidAmount = currentBid.getBidAmount();

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

//                currentBid = new Bid(bidAmount, houseID, currentItemSelected,
//                        bidMessageEnum);
                guiStuff.updateBidHistoryTextArea(currentBid.toString());
                break;
            case REJECT:
                bidStatusAlert =
                        new Alert(Alert.AlertType.WARNING);
                bidStatusAlert.setTitle(bidMessageEnum.name());
                bidStatusAlert.setContentText("Sorry, your bid of $"
                        + bidAmount + " on item "
                        + currentItemSelected.getTreeItemTitle()
                        + " was rejected...Better luck next time!");

//                currentBid = new Bid(bidAmount, houseID, currentItemSelected,
//                        bidMessageEnum);
                guiStuff.updateBidHistoryTextArea(currentBid.toString());
                break;
            default:
                bidStatusAlert =
                        new Alert(Alert.AlertType.ERROR);
                bidStatusAlert.setTitle(bidMessageEnum.name());
                bidStatusAlert.setContentText("System error...\n" +
                        "Something went wrong with the message...\n" +
                        "Received message: " + bidFullMessage);

//                currentBid = new Bid(bidAmount, houseID, currentItemSelected,
//                        ERROR);
                guiStuff.updateBidHistoryTextArea(currentBid.toString());
                break;
        }

        bidStatusAlert.show();
    }

    private void getAcceptBid(FullMessage bidFullMessage) {
        MessageEnum bidMessageEnum =
                bidFullMessage.getMessageEnum();
        List<String> bidArgs = bidFullMessage.getMessageArgs();

        int houseID = Integer.parseInt(bidArgs.get(0));
        int currentItemID = Integer.parseInt(bidArgs.get(1));

        Alert bidStatusAlert;

        // FIXME: get the bid with the given item ID...
        Bid currentBid = Bid.getBidFromItemID(
                currentBidList, currentItemID);
//        synchronized (currentBidList) {
//            currentBidList.remove(currentBid);
//        }

        assert currentBid != null;
        // FIXME: IMPORTANT
        currentBid.updateBidMessageEnum(bidMessageEnum);

        double bidAmount = currentBid.getBidAmount();

//        bidStatusAlert =
//                new Alert(
//                        Alert.AlertType.CONFIRMATION);
//        bidStatusAlert.setTitle(bidMessageEnum.name());
//        bidStatusAlert.setContentText("Your bid of $"
//                + bidAmount + " on item "
//                + currentItemSelected.getTreeItemTitle()
//                + " was accepted!");
//
//        bidStatusAlert.show();

        showAlert(Alert.AlertType.CONFIRMATION, bidMessageEnum.name()
                , "Your bid of $"
                        + bidAmount + " on item "
                        + currentItemSelected.getTreeItemTitle()
                        + " was accepted!");

        userBankAccount.removeFunds(bidAmount);

        //guiStuff.updateUserAccountBalanceLabel(bidAmount);
        guiStuff.updateUserBlockAmountLabel(bidAmount,
                false);

//                currentBid = new Bid(bidAmount, houseID, currentItemSelected,
//                        bidMessageEnum);
        guiStuff.updateBidHistoryTextArea(currentBid.toString());

        System.out.println();
        System.out.println("Received bid successfully");
    }

    private void getRejectBid(FullMessage bidFullMessage) {
        MessageEnum bidMessageEnum =
                bidFullMessage.getMessageEnum();
        List<String> bidArgs = bidFullMessage.getMessageArgs();

        int houseID = Integer.parseInt(bidArgs.get(0));
        int currentItemID = Integer.parseInt(bidArgs.get(1));

        Alert bidStatusAlert;

        // FIXME: get the bid with the given item ID...
        Bid currentBid = Bid.getBidFromItemID(
                currentBidList, currentItemID);
        synchronized (currentBidList) {
            currentBidList.remove(currentBid);
        }

        assert currentBid != null;
        // FIXME: IMPORTANT
        currentBid.updateBidMessageEnum(bidMessageEnum);

        double bidAmount = currentBid.getBidAmount();

//        bidStatusAlert =
//                new Alert(Alert.AlertType.WARNING);
//        bidStatusAlert.setTitle(bidMessageEnum.name());
//        bidStatusAlert.setContentText("Sorry, your bid of $"
//                + bidAmount + " on item "
//                + currentItemSelected.getTreeItemTitle()
//                + " was rejected...Better luck next time!");
//
//        bidStatusAlert.show();

        showAlert(Alert.AlertType.WARNING,
                bidMessageEnum.name(), "Sorry, your bid of $"
                + bidAmount + " on item "
                + currentItemSelected.getTreeItemTitle()
                + " was rejected...Better luck next time!");

//                currentBid = new Bid(bidAmount, houseID, currentItemSelected,
//                        bidMessageEnum);
        userBankAccount.addFunds(bidAmount);

        guiStuff.addFundsToBalanceLabel(bidAmount);
        guiStuff.updateUserBlockAmountLabel(bidAmount,
                false);

        guiStuff.updateBidHistoryTextArea(currentBid.toString());

        System.out.println();
        System.out.println("Received bid successfully");
    }

    private void initializeBankConnection() {
        System.out.println();
        System.out.println("Initializing the bank connection...");

        try {
            bankSocket = new Socket(bankHostName, bankPort);
            bankWriter = new PrintWriter(bankSocket.getOutputStream(),
                    true);
            bankReader = new BufferedReader(
                    new InputStreamReader(bankSocket.getInputStream()));

            // initialize the bank reader listener and action listener
//            bankReaderListener =
//                    new UserGUIReaderListener(bankSocket, bankReader,
//                            bankWriter,
//                            bankMessagesActionList);
//            bankReaderListener.start();
//
//            bankActionListener =
//                    new UserGUIActionListener(bankSocket, bankReader,
//                            bankWriter, bankMessagesActionList);
//            bankActionListener.start();

            // FIXME: replace above with timers instead...
            bankReaderTimer = new UserGUIReaderTimer(bankSocket,
                    bankReader,
                    bankWriter,
                    bankMessagesActionList);

            bankActionTimer = new UserGUIActionTimer(bankSocket,
                    bankReader,
                    bankWriter, bankMessagesActionList);

            System.out.println("Connection successful");

            System.out.println();
            System.out.println("Printing username to bank: " + username);
            bankWriter.println("user;" + username); // custom username
        } catch (IOException e) {
            System.out.println("Connection failed...");
            System.out.println();
            System.out.println(e.getMessage());
        }
    }

    private void initializeHouseConnection(String houseHostName) {
        System.out.println();
        System.out.println("Initializing auction house connection" +
                " with: \n" + currentAuctionHouseUser + "...");

        try {
            houseSocket = new Socket(houseHostName,
                    currentAuctionHouseUser.getHousePort());
            houseWriter = new PrintWriter(houseSocket.getOutputStream(),
                    true);
            houseReader = new BufferedReader(
                    new InputStreamReader(houseSocket.getInputStream()));

            System.out.println();
            System.out.println("Checking the house socket " + houseSocket);
            System.out.println("Checking the house reader " + houseReader);
            System.out.println("Checking the house writer " + houseWriter);
            System.out.println("Sending the user ID to the house...");
            houseWriter.println(userID);
            System.out.println("Completed");

            // initialize the house reader listener and action listener
            // TODO: make sure to clear listeners and action list when making
            //  new connection...
//            houseReaderListener = new UserGUIReaderListener(houseSocket,
//                    houseReader, houseWriter,
//                    houseMessagesActionList);
//            houseReaderListener.start();
//
//            houseActionListener = new UserGUIActionListener(houseSocket,
//                    houseReader, houseWriter,
//                    houseMessagesActionList);
//            houseActionListener.start();

            // FIXME: replace above with timers instead...
            houseReaderTimer = new UserGUIReaderTimer(houseSocket,
                    houseReader, houseWriter,
                    houseMessagesActionList);

            houseActionTimer = new UserGUIActionTimer(houseSocket,
                    houseReader, houseWriter,
                    houseMessagesActionList);

            System.out.println("Connection successful");

//            while (true) {
//                if (houseReader.ready()) {
//                    String houseMessage = houseReader.readLine();
//                    System.out.println();
//                    System.out.println("Message from house: " + houseMessage);
//                    break;
//                }
//            }
        } catch (IOException e) {
            System.out.println("Connection failed...");
            System.out.println();
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void askHouseItemList() {
        // TODO: move the clear to when the command is processed...
        //houseItemList.clear();

        int houseID = currentAuctionHouseUser.getHouseID();

        System.out.println();
        System.out.println("Asking for the house item list from "
                + currentAuctionHouseUser);

        houseWriter.println(MessageEnum.createMessageString(GET_ITEMS,
                new ArrayList<>()));
    }

    private void clearItemsFromAllHouseTreeItem() {
//        Platform.runLater(() -> {
//            for (CustomAuctionHouseTreeItem houseTreeItem : houseTreeItemList) {
//                houseTreeItem.getChildren().clear();
//            }
//        });

        for (CustomAuctionHouseTreeItem houseTreeItem : houseTreeItemList) {
            houseTreeItem.getChildren().clear();
        }
    }

    private void getHouseItemList(FullMessage houseItemListFullMessage) {
        houseItemList.clear();

        // clear the items in the tree view of the current auction house
        clearItemsFromAllHouseTreeItem();

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

        System.out.println("The item list for \n" + currentAuctionHouseUser
                + "\nwas setup successfully");

        // alert for items
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Items Notification");
//        alert.setContentText("Successfully initialized " + itemCount +
//                " items from house id: " + houseID);
//        alert.show();

        showAlert(Alert.AlertType.CONFIRMATION,
                "Items Notification",
                "Successfully initialized " + itemCount +
                " items from house id: " + houseID);
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
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Items Notification");
//        alert.setContentText("Successfully initialized " + itemCount +
//                " items from house id: " + houseID);
//
//        alert.show();

        showAlert(Alert.AlertType.CONFIRMATION,
                "Items Notification",
                "Successfully initialized " + itemCount +
                " items from house id: " + houseID);
    }

    private void itemMousePress(MouseEvent mouseEvent, Item item) {
        currentItemSelected = item;

        System.out.println("Item type: Tree Cell...");
        System.out.println("Current item selected: " + item);

        guiStuff.updateCurrentItemSelectedLabel(item);
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
                    housesArgs.get(i)), housesArgs.get(i + 1), housePort));
        }

        updateHouseIDsList();
        updateHouseListTreeView();
    }

    // get the list of auction houses
    private void askAuctionHouseList() {
        System.out.println();
        System.out.println("Asking for the auction house list...");

        List<String> getHousesArgs = new ArrayList<>();
        getHousesArgs.add(Integer.toString(userID));
        String getHousesMessage =
                MessageEnum.createMessageString(GET_HOUSES,
                        getHousesArgs);

        bankWriter.println(getHousesMessage);
    }

    private void updateHouseIDsList() {
        houseIDsList.clear();

        for (AuctionHouseUser auctionHouseUser : entireHousesList) {
            int houseID = auctionHouseUser.getHouseID();
            houseIDsList.add(houseID);
        }
    }

    // FIXME: move...
    // get the user id
//    private void getUserID() {
////        while (true) {
////            try {
////                if (bankReader.ready()) {
////                    String userIDMessage = bankReader.readLine();
////                    List<String> userIDArgs =
////                            MessageEnum.parseMessageArgs(userIDMessage);
////                    userID = Integer.parseInt(userIDArgs.get(0));
////
////                    // account ID is the same as the user ID
////                    userBankAccount = new BankAccount(INITIAL_BALANCE,
////                            userID);
////                    break;
////                }
////            } catch (IOException e) {
////                System.out.println(e.getMessage());
////            }
////        }
//
//        System.out.println();
//        System.out.println("Setting up user ID...");
//
//        FullMessage userIDMessage = getFullMessageFromReader(
//                bankReader);
//        List<String> userIDArgs =
//                userIDMessage.getMessageArgs();
//
//        userID = Integer.parseInt(userIDArgs.get(0));
//
//        // account ID is the same as the user ID
//        userBankAccount = new BankAccount(INITIAL_BALANCE,
//                userID);
//    }

    // get the user id
    private void getUserID(FullMessage userIDMessage) {
        // FIXME: just test for GUI instantiation in separate thread
//        CustomAlert customAlert =
//                new CustomAlert(Alert.AlertType.CONFIRMATION, "Something",
//                        "Something");
//        Platform.runLater(customAlert.showAlert());

        System.out.println();
        System.out.println("Setting up user ID...");

        List<String> userIDArgs =
                userIDMessage.getMessageArgs();

        userID = Integer.parseInt(userIDArgs.get(0));

        // account ID is the same as the user ID
        userBankAccount = new BankAccount(INITIAL_BALANCE,
                userID);

        // FIXME: changed where the labels are initialized
        //Platform.runLater(this::initializeLabels);
        initializeLabels();

        System.out.println("Finished...user ID: " + userID);
        System.out.println("Initialized bank account with initial balance: $"
                + INITIAL_BALANCE);

        // FIXME: moved from initialize()
        askAuctionHouseList();
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

        if (userID <= 0) {
            showAlert(Alert.AlertType.ERROR, "Exit Error",
                    "Sorry, you cannot" +
                    " exit at this " +
                    "time. Please wait after the user is initialized first " +
                    "before exiting...");
            return;
        }

        if (bankWriter == null) {
            showAlert(Alert.AlertType.ERROR, "Hit and Dash...",
                    "Sorry, " +
                    "please wait until the connection with the bank is " +
                    "successfully connected before leaving...");
            return;
        }

        if (houseWriter == null) {
            bankWriter.println(
                    MessageEnum.createMessageString(EXIT,
                            exitBankArgs));

            FullMessage exitBankMessage = getFullMessageFromReader(
                    bankReader);

            MessageEnum exitBankMessageEnum = exitBankMessage.getMessageEnum();

            if (exitBankMessageEnum == CAN_EXIT) {
                // get a handle to the stage
//                Stage stage = (Stage) userExitButton.getScene().getWindow();
//                stage.close();

                // FIXME: might cause an error after the stage is closed...
//            CustomAlert customAlert =
//                    new CustomAlert(Alert.AlertType.CONFIRMATION,
//                            "Exit " +
//                            "Program Successful",
//                            "The auction house program " +
//                            "was exited successfully.");
//            customAlert.show();

                showAlert(Alert.AlertType.CONFIRMATION,
                        "Exit " +
                                "Program Successful",
                        "The auction house program " +
                                "was exited successfully.");

                checkBankExit = true;
            } else {
//            Alert exitAlert = new Alert(Alert.AlertType.ERROR);
//            exitAlert.setTitle("Exit Auction House Error");
//            exitAlert.setContentText("Sorry, you are unable to exit due to " +
//                    "various reasons (bids still in progress)...");
//
//            exitAlert.show();

                showAlert(Alert.AlertType.ERROR,
                        "Exit Auction House Error",
                        "Sorry, you are unable to exit due to " +
                                "various reasons (bids still in progress)...");

                event.consume();
            }
        } else {
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
//                Stage stage = (Stage) userExitButton.getScene().getWindow();
//                stage.close();

                // FIXME: might cause an error after the stage is closed...
//            CustomAlert customAlert =
//                    new CustomAlert(Alert.AlertType.CONFIRMATION,
//                            "Exit " +
//                            "Program Successful",
//                            "The auction house program " +
//                            "was exited successfully.");
//            customAlert.show();

                showAlert(Alert.AlertType.CONFIRMATION,
                        "Exit " +
                                "Program Successful",
                        "The auction house program " +
                                "was exited successfully.");

                checkBankExit = true;
                checkHouseExit = true;
            } else {
//            Alert exitAlert = new Alert(Alert.AlertType.ERROR);
//            exitAlert.setTitle("Exit Auction House Error");
//            exitAlert.setContentText("Sorry, you are unable to exit due to " +
//                    "various reasons (bids still in progress)...");
//
//            exitAlert.show();

                showAlert(Alert.AlertType.ERROR,
                        "Exit Auction House Error",
                        "Sorry, you are unable to exit due to " +
                                "various reasons (bids still in progress)...");

                event.consume();
            }
        }
    }

    private class CustomTreeCell extends TreeCell<String> {
        // FIXME
        private ContextMenu connectContextMenu = new ContextMenu();

        public CustomTreeCell() {
//            TreeItem<String> treeItem = getTreeItem();
//
//            System.out.println();
//            System.out.println("Entering constructor of Tree Cell");
//            System.out.println("Tree item " + treeItem);

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

        // FIXME: added synchronized to update item
        @Override
        public void updateItem(String itemString, boolean empty) {
            // Maybe this was why there was some inaccurate
            // updating...(EVERYTHING has to be in the runLater())
//            Platform.runLater(() -> {
//                super.updateItem(itemString, empty) ;
//
//                if (empty) {
//                    setText(null);
//                } else {
//                    setText(itemString);
//
//                    TreeItem<String> treeItem = getTreeItem();
//
////                System.out.println();
////                System.out.println("Updating tree item...");
////                System.out.println("Tree item " + treeItem);
//
//                    // maybe this will work...
//                    // FIXME: add if(connectContextMenu.getItems().isEmpty())
//                    if (treeItem instanceof CustomAuctionHouseTreeItem) {
//                        if (connectContextMenu.getItems().isEmpty()) {
//                            //System.out.println("Auction House: Tree Cell...");
//
//                            MenuItem connectMenuItem = new MenuItem("Connect to this Auction " +
//                                    "House");
//                            connectContextMenu.getItems().add(connectMenuItem);
//
////                        System.out.println();
////                        System.out.println("Setting the context menu in the " +
////                                "update");
//
//                            setContextMenu(connectContextMenu);
//
//                            connectMenuItem.setOnAction(event -> {
//                                // send request to connect to the chosen auction house
////                        String[] houseTreeItemArgs = treeItem.getValue().split(" ");
////                        String houseID = houseTreeItemArgs[1];
//
//                                // FIXME: changed how the houseID is gotten
//                                CustomAuctionHouseTreeItem currentHouseTreeItem =
//                                        (CustomAuctionHouseTreeItem) treeItem;
//                                AuctionHouseUser auctionHouseUser =
//                                        currentHouseTreeItem.getAuctionHouseUser();
//                                int parseHouseID =
//                                        auctionHouseUser.getHouseID();
//
//                                for (CustomAuctionHouseTreeItem houseTreeItem : houseTreeItemList) {
//                                    if (houseTreeItem.checkID(parseHouseID)) {
//                                        currentAuctionHouseTreeItem = houseTreeItem;
//                                        currentAuctionHouseUser =
//                                                currentAuctionHouseTreeItem.getAuctionHouseUser();
//                                        break;
//                                    }
//                                }
//
//                                //TODO: update label, get items
////                        AuctionHouseUser auctionHouseUser =
////                                currentAuctionHouseTreeItem.getAuctionHouseUser();
//
//                                String houseHostName = currentAuctionHouseUser.getHouseHostName();
//
//                                initializeHouseConnection(houseHostName);
//
//                                //updateHouseItemList();
//                                askHouseItemList();
//
//                                guiStuff.updateCurrentAuctionHouseLabel(
//                                        currentAuctionHouseUser);
//                            });
//                        } else {
////                        System.out.println();
////                        System.out.println("Auction house connect context " +
////                                "menu already initialized");
//                        }
//                    } else if (treeItem instanceof CustomItemTreeItem) {
//                        System.out.println("Item type: Tree Cell...");
//
//                        CustomItemTreeItem customItemTreeItem =
//                                (CustomItemTreeItem) treeItem;
//
//                        Item item = customItemTreeItem.getItem();
//
//                        System.out.println("Current item selected: " + item);
//
//                        this.setOnMousePressed(event -> itemMousePress(event,
//                                item));
//
//                        guiStuff.updateCurrentItemSelectedLabel(item);
//
////                    if (treeItem.isLeaf()
////                            && getTreeItem().getParent() != null){
////
////                    } else {
////                        System.out.println();
////                        System.out.println("No update...");
////                    }
//                    }
//
////                if (treeItem instanceof CustomAuctionHouseTreeItem) {
////                    System.out.println();
////                    System.out.println("Setting the context menu in the " +
////                            "update");
////
////                    setContextMenu(connectContextMenu);
////                }
//                }
//            });

            super.updateItem(itemString, empty) ;

            if (empty) {
                setText(null);
            } else {
                setText(itemString);

                TreeItem<String> treeItem = getTreeItem();

//                System.out.println();
//                System.out.println("Updating tree item...");
//                System.out.println("Tree item " + treeItem);

                // maybe this will work...
                // FIXME: add if(connectContextMenu.getItems().isEmpty())
                if (treeItem instanceof CustomAuctionHouseTreeItem) {
                    if (connectContextMenu.getItems().isEmpty()) {
                        //System.out.println("Auction House: Tree Cell...");

                        MenuItem connectMenuItem = new MenuItem("Connect to this Auction " +
                                "House");
                        connectContextMenu.getItems().add(connectMenuItem);

//                        System.out.println();
//                        System.out.println("Setting the context menu in the " +
//                                "update");

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

                            initializeHouseConnection(houseHostName);

                            //updateHouseItemList();
                            askHouseItemList();

                            guiStuff.updateCurrentAuctionHouseLabel(
                                    currentAuctionHouseUser);
                        });
                    } else {
//                        System.out.println();
//                        System.out.println("Auction house connect context " +
//                                "menu already initialized");
                    }
                } else if (treeItem instanceof CustomItemTreeItem) {
                    CustomItemTreeItem customItemTreeItem =
                            (CustomItemTreeItem) treeItem;

                    Item item = customItemTreeItem.getItem();


                    this.setOnMousePressed(event -> itemMousePress(event,
                            item));

                    //guiStuff.updateCurrentItemSelectedLabel(item);

//                    if (treeItem.isLeaf()
//                            && getTreeItem().getParent() != null){
//
//                    } else {
//                        System.out.println();
//                        System.out.println("No update...");
//                    }
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

    private class UserGUIActionTimer {
        private Timer timer;
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private final List<FullMessage> fullMessagesActionList;

        public UserGUIActionTimer(Socket socket, BufferedReader reader,
                                  PrintWriter writer,
                                  List<FullMessage> fullMessagesActionList) {
            timer = new Timer();

            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
            this.fullMessagesActionList = fullMessagesActionList;

            timer.scheduleAtFixedRate(new UserGUIActionTimerTask(),
                    0, 250);
        }

        private class UserGUIActionTimerTask extends TimerTask {

            @Override
            public void run() {
                Platform.runLater(() -> {
                    synchronized (fullMessagesActionList) {
                        while (!fullMessagesActionList.isEmpty()) {
                            FullMessage currentFullMessage =
                                    fullMessagesActionList.remove(0);
                            MessageEnum messageEnum =
                                    currentFullMessage.getMessageEnum();
                            List<String> messageArgs =
                                    currentFullMessage.getMessageArgs();

                            // only two action listeners: bank or current house
                            // connected to
                            if (socket == bankSocket) {
                                handleBankCommand(messageEnum,
                                        currentFullMessage);
                            } else {
                                handleHouseCommand(messageEnum,
                                        currentFullMessage);
                            }
                        }

                        // TODO: check exit condition
                        // FIXME: edit logic...to only bank
                        System.out.println(checkBankExit);

                        if (checkBankExit) {
                            // get a handle to the stage
                            // maybe this was why the stage wasn't closing...
//                        Platform.runLater(() -> {
//                            Stage stage = (Stage) userExitButton.getScene().getWindow();
//                            stage.close();
//                        });
                            System.out.println("Before the stage is exited...");

                            Stage stage = (Stage) userExitButton.getScene().getWindow();
                            stage.close();

                            // FIXME: might cause an error after the stage is closed...
//                        CustomAlert customAlert =
//                                new CustomAlert(Alert.AlertType.CONFIRMATION,
//                                        "Exit " +
//                                                "Program Successful",
//                                        "The auction house program " +
//                                                "was exited successfully.");
//                        customAlert.show();

                            System.out.println("Before the alert...");

                            showAlert(Alert.AlertType.CONFIRMATION,
                                    "Exit " +
                                            "Program Successful",
                                    "The auction house program " +
                                            "was exited successfully.");

                            // FIXME: test
                            showAlert(Alert.AlertType.ERROR,
                                    "Test", "");

                            // TODO: stop the (action) timers...
                            System.out.println("Before closing actions...");

                            stopRunning();
                            if (houseActionTimer != null) {
                                houseActionTimer.stopRunning();
                            }

                            // TODO: ALSO THE READER TIMERS
                            System.out.println("Before closing readers...");

                            bankReaderTimer.stopRunning();
                            if (houseReaderTimer != null) {
                                houseReaderTimer.stopRunning();
                            }

                            closeStreams();

                            System.out.println("Before closing stream...");

                            closeStreams();

                            System.out.println("Exiting program...");
                            System.exit(0);
                        }
                    }
                });
            }
        }

        private void closeStreams() {
            try {
                bankReader.close();
                bankWriter.close();

                if (houseReader != null) {
                    houseReader.close();
                }

                if (houseWriter != null) {
                    houseWriter.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        public void stopRunning() {
            timer.cancel();
            timer.purge();
        }
    }

    private class UserGUIActionListener extends Thread {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private final List<FullMessage> fullMessagesActionList;
        private boolean run;

        public UserGUIActionListener(Socket socket,
                                     BufferedReader reader,
                                     PrintWriter writer,
                                     List<FullMessage> fullMessagesActionList) {
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
            this.fullMessagesActionList = fullMessagesActionList;
            this.run = true;
        }

        @Override
        public void run() {
            while (run) {
                Platform.runLater(() -> {
                    synchronized (fullMessagesActionList) {
                        while (!fullMessagesActionList.isEmpty()) {
                            FullMessage currentFullMessage =
                                    fullMessagesActionList.remove(0);
                            MessageEnum messageEnum =
                                    currentFullMessage.getMessageEnum();
                            List<String> messageArgs =
                                    currentFullMessage.getMessageArgs();

                            // only two action listeners: bank or current house
                            // connected to
                            if (socket == bankSocket) {
                                handleBankCommand(messageEnum,
                                        currentFullMessage);
                            } else {
                                handleHouseCommand(messageEnum,
                                        currentFullMessage);
                            }
                        }

                        // TODO: check exit condition
                        if (checkBankExit && checkHouseExit) {
                            // get a handle to the stage
                            // maybe this was why the stage wasn't closing...
//                        Platform.runLater(() -> {
//                            Stage stage = (Stage) userExitButton.getScene().getWindow();
//                            stage.close();
//                        });

                            System.out.println("Before the stage is exited...");

                            Stage stage = (Stage) userExitButton.getScene().getWindow();
                            stage.close();

                            // FIXME: might cause an error after the stage is closed...
//                        CustomAlert customAlert =
//                                new CustomAlert(Alert.AlertType.CONFIRMATION,
//                                        "Exit " +
//                                                "Program Successful",
//                                        "The auction house program " +
//                                                "was exited successfully.");
//                        customAlert.show();

                            System.out.println("Before the alert...");

                            showAlert(Alert.AlertType.CONFIRMATION,
                                    "Exit " +
                                            "Program Successful",
                                    "The auction house program " +
                                            "was exited successfully.");

                            // FIXME: test
                            showAlert(Alert.AlertType.ERROR, "Test", "");

                            // TODO: stop the (action) listeners...
                            System.out.println("Before closing actions...");

                            stopRunning();
                            houseActionTimer.stopRunning();

                            // TODO: ALSO THE READER LISTENER
                            System.out.println("Before closing readers...");

                            bankReaderListener.stopRunning();
                            houseReaderListener.stopRunning();

                            System.out.println("Before closing stream...");

                            closeStreams();

                            System.out.println("Exiting program...");
                            System.exit(0);
                        }
                    }
                });
            }
        }

        public void stopRunning() {
            run = false;
        }

        private void closeStreams() {
            try {
                bankReader.close();
                bankWriter.close();
                houseReader.close();
                houseWriter.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void handleBankCommand(MessageEnum messageEnum,
                                   FullMessage currentFullMessage) {
        switch (messageEnum) {
            case HOUSE_LIST:
                getAuctionHouseList(currentFullMessage);
                break;
            case OUTBID:
                getOutBidMessage(currentFullMessage);
                break;
            case WINNER:
                getWinnerMessage(currentFullMessage);
                break;
            case ITEM_WON:
                getItemWonMessage(currentFullMessage);
                break;
            case LOGIN:
                getUserID(currentFullMessage);
                break;
            case CAN_EXIT:
                getBankCanExitMessage(currentFullMessage);
                break;
            case ERROR:
                getErrorMessage(currentFullMessage);
                break;
            default:
                getUnknownMessage(currentFullMessage);
        }
    }

    private void handleHouseCommand(MessageEnum messageEnum,
                                    FullMessage currentFullMessage) {
        switch (messageEnum) {
            case ITEMS:
                getHouseItemList(currentFullMessage);
                break;
            case ACCEPT:
                getAcceptBid(currentFullMessage);
                break;
            case REJECT:
                getRejectBid(currentFullMessage);
                break;
            case ITEM:
                // TODO: don't think I implemented this...
                break;
            case CAN_EXIT:
                getHouseCanExitMessage(currentFullMessage);
                break;
            case ERROR:
                getErrorMessage(currentFullMessage);
                break;
            default:
                getUnknownMessage(currentFullMessage);
        }
    }

    private void getOutBidMessage(FullMessage currentFullMessage) {
        List<String> outBidArgs = currentFullMessage.getMessageArgs();

        int houseID = Integer.parseInt(outBidArgs.get(0));
        int itemID = Integer.parseInt(outBidArgs.get(1));
        int outBidderID = Integer.parseInt(outBidArgs.get(2));
        double newBidAmount = Double.parseDouble(outBidArgs.get(3));

        synchronized (currentBidList) {
            currentBidList.remove(Bid.getBidFromItemID(currentBidList,
                    itemID));
        }

        //Alert outBidAlert = new Alert(Alert.AlertType.WARNING);
        MessageEnum messageEnum = currentFullMessage.getMessageEnum();
//        CustomAlert outBidAlert = new CustomAlert(Alert.AlertType.WARNING,
//                messageEnum.name(), "Alert from House ID: " + houseID +
//                "\nSorry, you have been outbid by User ID: " + outBidderID +
//                " on the Item ID: " + itemID + " with a new bid amount of $" + newBidAmount);
//        outBidAlert.show();

        showAlert(Alert.AlertType.WARNING,
                messageEnum.name(), "Alert from House ID: " + houseID +
                        "\nSorry, you have been outbid by User ID: " + outBidderID +
                        " on the Item ID: " + itemID + " with a new bid amount of $" + newBidAmount);

        // TODO: update bid history
        guiStuff.updateBidHistoryTextArea(Bid.getAlternateBidString(
                messageEnum, houseID, itemID,
                newBidAmount));
    }

    private void getWinnerMessage(FullMessage currentFullMessage) {
        List<String> outBidArgs = currentFullMessage.getMessageArgs();

        int houseID = Integer.parseInt(outBidArgs.get(0));
        String itemName = outBidArgs.get(1);
        int itemID = Integer.parseInt(outBidArgs.get(2));
        double newBidAmount = Double.parseDouble(outBidArgs.get(3));

        synchronized (currentBidList) {
            currentBidList.remove(Bid.getBidFromItemID(currentBidList,
                    itemID));
        }

        //Alert outBidAlert = new Alert(Alert.AlertType.WARNING);
        MessageEnum messageEnum = currentFullMessage.getMessageEnum();
//        CustomAlert outBidAlert = new CustomAlert(Alert.AlertType.CONFIRMATION,
//                messageEnum.name(), "Alert from House ID: " + houseID +
//                "\nCongratulations, you have won the following item!\n" + itemName +
//                "with Item ID: " + itemID + " with a final bid amount of $" + newBidAmount);
//        outBidAlert.show();

        showAlert(Alert.AlertType.CONFIRMATION,
                messageEnum.name(), "Alert from House ID: " + houseID +
                        "\nCongratulations, you have won the following item!\n" + itemName +
                        "with Item ID: " + itemID + " with a final bid amount of $" + newBidAmount);

        // TODO: update bid history
        guiStuff.updateBidHistoryTextArea(Bid.getAlternateBidString(
                messageEnum, houseID, itemName, itemID,
                newBidAmount));
    }

    private void getItemWonMessage(FullMessage currentFullMessage) {
        List<String> outBidArgs = currentFullMessage.getMessageArgs();

        int houseID = Integer.parseInt(outBidArgs.get(0));
        String itemName = outBidArgs.get(1);
        int itemID = Integer.parseInt(outBidArgs.get(2));
        String winnerUsername = outBidArgs.get(3);
        double newBidAmount = Double.parseDouble(outBidArgs.get(4));

        synchronized (currentBidList) {
            currentBidList.remove(Bid.getBidFromItemID(currentBidList,
                    itemID));
        }

        //Alert outBidAlert = new Alert(Alert.AlertType.WARNING);
        MessageEnum messageEnum = currentFullMessage.getMessageEnum();
//        CustomAlert outBidAlert = new CustomAlert(Alert.AlertType.CONFIRMATION,
//                messageEnum.name(), "Alert from House ID: " + houseID +
//                "\nUnfortunately, the following item is no longer in " +
//                "auction with the winner " + winnerUsername + "...\n" + itemName +
//                "with Item ID: " + itemID + " with a final bid amount of $"
//                + newBidAmount);
//        outBidAlert.show();

        showAlert(Alert.AlertType.CONFIRMATION,
                messageEnum.name(), "Alert from House ID: " + houseID +
                        "\nUnfortunately, the following item is no longer in " +
                        "auction with the winner " + winnerUsername + "...\n" + itemName +
                        "with Item ID: " + itemID + " with a final bid amount of $"
                        + newBidAmount);

        // TODO: update bid history

        guiStuff.updateBidHistoryTextArea(Bid.getAlternateBidString(
                messageEnum, houseID, itemName,
                itemID, winnerUsername,
                newBidAmount));
    }

    private void getErrorMessage(FullMessage currentFullMessage) {
        MessageEnum messageEnum = currentFullMessage.getMessageEnum();

//        CustomAlert errorAlert = new CustomAlert(Alert.AlertType.ERROR,
//                messageEnum.name(),
//                "The following error message was received" +
//                "...\n" + currentFullMessage);
//        errorAlert.show();

        showAlert(Alert.AlertType.ERROR, messageEnum.name(),
                "The following error message was received" +
                        "...\n" + currentFullMessage);
    }

    private void getUnknownMessage(FullMessage currentFullMessage) {
        //MessageEnum messageEnum = currentFullMessage.getMessageEnum();
//        CustomAlert unknownAlert = new CustomAlert(Alert.AlertType.ERROR,
//                "UNKNOWN",
//                "The following unknown message was received" +
//                        "...\n" + currentFullMessage);
//        unknownAlert.show();

        showAlert(Alert.AlertType.ERROR, "UNKNOWN",
                "The following unknown message was received" +
                        "...\n" + currentFullMessage);
    }

    private void getBankCanExitMessage(FullMessage currentFullMessage) {
        MessageEnum messageEnum = currentFullMessage.getMessageEnum();

//        CustomAlert bankExitAlert = new CustomAlert(
//                Alert.AlertType.CONFIRMATION,
//                messageEnum.name() + " Bank",
//                "You can now exit the bank.");
//        bankExitAlert.showAlert();

        showAlert(
                Alert.AlertType.CONFIRMATION,
                messageEnum.name() + " Bank",
                "You are now disconnected with the " +
                        "You can now exit the bank.");

        // TODO: clear listeners and action list
        bankReaderListener.stopRunning();
        bankReaderListener = null;
        bankActionListener.stopRunning();
        bankActionListener = null;

        bankMessagesActionList.clear();

        checkBankExit = true;
    }

    private void getHouseCanExitMessage(FullMessage currentFullMessage) {
        List<String> houseCanExitArgs = currentFullMessage.getMessageArgs();
        int houseID = Integer.parseInt(houseCanExitArgs.get(0));

//        MessageEnum messageEnum = currentFullMessage.getMessageEnum();
//        CustomAlert bankExitAlert = new CustomAlert(
//                Alert.AlertType.CONFIRMATION,
//                messageEnum.name() + " Auction House",
//                "You are now disconnected with the " +
//                        "current auction house.");
//        bankExitAlert.show();

        MessageEnum messageEnum = currentFullMessage.getMessageEnum();
        showAlert(
                Alert.AlertType.CONFIRMATION,
                messageEnum.name() + " Auction House",
                "You are now disconnected with the " +
                        "current auction house.");

        // TODO: update labels...
        currentAuctionHouseUser = null;

        guiStuff.resetLabel(currentAuctionHouseLabel);
        //guiStuff.resetLabel(currentAuctionHouseLabel)

        // TODO: current item selected and item list...
        currentItemSelected = null;
        guiStuff.resetLabel(currentItemSelectedLabel);
        //guiStuff.resetLabel(currentItemSelectedLabel);

        // TODO: clear the children (items) of ALL HOUSES
        clearItemsFromAllHouseTreeItem();

        // TODO: clear listeners and action list
//        houseReaderListener.stopRunning();
//        houseReaderListener = null;
//        houseActionListener.stopRunning();
//        houseActionListener = null;

        // TODO: close the house streams
        try {
            houseReader.close();
            houseWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        houseMessagesActionList.clear();

        // TODO: clear any other stuff
        currentBidList.clear();
        houseItemList.clear();

        checkHouseExit = true;

        // TODO: maybe refresh the house list afterwards?
        askAuctionHouseList();
    }

    private void getAuctionHouseList(FullMessage houseListMessage) {
        int numArgs = 3;

        List<String> housesArgs = houseListMessage.getMessageArgs();

        System.out.println();
        System.out.println("House args:");
        System.out.println(housesArgs);

        entireHousesList.clear();
        for (int i = 0; i < housesArgs.size(); i += numArgs) {
            entireHousesList.add(new AuctionHouseUser(Integer.parseInt(
                    housesArgs.get(i)), housesArgs.get(i + 1),
                    Integer.parseInt(housesArgs.get(i + 2))));
        }

        updateHouseIDsList();
        updateHouseListTreeView();

        System.out.println();
        System.out.println("Finished...");
        System.out.println("Auction house list:");
        String houseListString = "";
        for (AuctionHouseUser auctionHouseUser : entireHousesList) {
            System.out.println(auctionHouseUser);
            houseListString += auctionHouseUser.toString() + "\n";
        }

        showAlert(Alert.AlertType.CONFIRMATION,
                "Auction House List Loaded " +
                "Successfully", houseListString);

        System.out.println();
        System.out.println("Auction House List setup successful");
    }

    private void showAlert(Alert.AlertType alertType, String titleAlert,
                               String contextStringAlert) {


//        Platform.runLater(() -> {
//            CustomAlert bankExitAlert = new CustomAlert(
//                    alertType,
//                    titleAlert,
//                    contextStringAlert);
//            bankExitAlert.show();
//        });

        CustomAlert bankExitAlert = new CustomAlert(
                alertType,
                titleAlert,
                contextStringAlert);
        bankExitAlert.show();
    }
}
