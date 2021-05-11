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

/**
 * Controller for the UserGUI
 *
 * Has all the functionality (taking in GUI elements from the FXML file using
 * SceneBuilder) to process and update any commands and display them on the
 * user display
 */
public class UserGUIController {
    // All GUI elements from the FXML file
    @FXML
    Pane pane;

    @FXML
    Label usernameLabel;

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

    private String bankHostName = bankHostNameInput;
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

    // deprecated: moved from using threads to timers
    private UserGUIReaderListener bankReaderListener;
    private UserGUIActionListener bankActionListener;

    private UserGUIReaderTimer bankReaderTimer;
    private UserGUIActionTimer bankActionTimer;

    // deprecated: moved from using threads to timers
    private UserGUIReaderListener houseReaderListener;
    private UserGUIActionListener houseActionListener;

    private UserGUIReaderTimer houseReaderTimer;
    private UserGUIActionTimer houseActionTimer;

    // initializes everything in the window
    @FXML
    public void initialize() throws InterruptedException {
        System.out.println("Starting up the user program...");
        System.out.println("Initializing...");

        // starts the connection with the bank
        initializeBankConnection();

        // Creates the root of the tree view (containing the list of houses
        // and the list of items for the connected house)
        rootTreeItem = new TreeItem<>(
                "List of available " +
                "Auction Houses");
        rootTreeItem.setExpanded(true);

        houseItemTreeView = new TreeView<>();
        houseItemTreeView.setRoot(rootTreeItem);

        // creates the cell factory for the tree items (defined below)
        houseItemTreeView.setCellFactory(param -> new CustomTreeCell());

        pane.getChildren().add(houseItemTreeView);

        // initializes every GUI element so it can be updated throughout the
        // program
        guiStuff = new GuiStuff(usernameLabel,
                userIDAccountLabel,
                userAccountBalanceLabel,
                currentAuctionHouseLabel,
                currentItemSelectedLabel,
                userBidAmountTextField,
                bidHistoryLabel,
                userBlockAmountLabel,
                bidHistoryTextArea);

        // sets the actions for the various buttons

        // exit buttons
        bidButton.setOnAction(event -> bidButtonOnAction());

        userExitButton.setOnAction(this::exitRequestCheck);

        exitHouseButton.setOnAction(this::exitHouseButtonOnAction);

        // same as general exit button, but button more explicit to disconnect
        // with the bank specifically
        exitBankButton.setOnAction(this::exitRequestCheck);

        // refresh buttons
        refreshHouseListButton.setOnAction(event -> askAuctionHouseList());

        refreshItemListButton.setOnAction(event -> askHouseItemList());
    }

    // initializes the labels on startup
    private void initializeLabels() {
        guiStuff.updateUsernameLabel(username);
        guiStuff.updateUserIDAccountLabel(userID);
        guiStuff.updateUserAccountBalanceLabel(
                userBankAccount.getBalance());

        guiStuff.resetLabel(currentAuctionHouseLabel);
        guiStuff.resetLabel(currentItemSelectedLabel);
        guiStuff.updateUserBlockAmountLabel(0.00, true);
    }

    // asks to house for the user to exit
    private void exitHouseButtonOnAction(Event event) {
        System.out.println();
        System.out.println("Breaking connection with House ID: "
                + currentAuctionHouseUser.getHouseID() + "...");

        List<String> houseExitArgs = Collections.singletonList(
                Integer.toString(userID));
        houseWriter.println(MessageEnum.createMessageString(EXIT,
                houseExitArgs));
    }

    // sets up the bid to be sent to the house
    private void bidButtonOnAction() {
        if (currentItemSelected == null) {
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
        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Bid in Progress",
                    "Sorry, you cannot place multiple " +
                            "bids on the same item until your bid " +
                            "was processed...");

            return;
        }

        // FIXME: broken up the bid for the listeners...
        askBid(currentBid);
    }

    // keeps track of the current item to be bid (on mouse press)
    private void itemSetOnMousePress(MouseEvent mouseEvent, Item item) {
        currentItemSelected = item;

        System.out.println("Item type: Tree Cell...");
        System.out.println("Current item selected: " + item);

        guiStuff.updateCurrentItemSelectedLabel(item);
    }

    // initializes the connection with the bank by initializing the socket,
    // writer, reader, and timers associated with the bank
    // initial message to the bank of printing out user;someUsername
    private void initializeBankConnection() {
        System.out.println();
        System.out.println("Initializing the bank connection...");

        try {
            bankSocket = new Socket(bankHostName, bankPort);
            bankWriter = new PrintWriter(bankSocket.getOutputStream(),
                    true);
            bankReader = new BufferedReader(
                    new InputStreamReader(bankSocket.getInputStream()));

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

    // initializes the connection with the house by initializing the socket,
    // writer, reader, and timers associated with the house
    // initial message to the bank of printing out the user ID to the house
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
        } catch (IOException e) {
            System.out.println("Connection failed...");
            System.out.println();
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // asks the house for the specified bid
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

    // processes an accept bid message
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

        assert currentBid != null;
        // FIXME: IMPORTANT
        currentBid.updateBidMessageEnum(bidMessageEnum);

        double bidAmount = currentBid.getBidAmount();

        showAlert(Alert.AlertType.CONFIRMATION, bidMessageEnum.name()
                , "Your bid of $"
                        + bidAmount + " on item "
                        + currentItemSelected.getTreeItemTitle()
                        + " was accepted!");

        userBankAccount.removeFunds(bidAmount);

        guiStuff.removeFundsFromBalanceLabel(bidAmount);
        guiStuff.updateUserBlockAmountLabel(bidAmount,
                true);

        guiStuff.updateBidHistoryTextArea(currentBid.toString());

        System.out.println();
        System.out.println("Received bid successfully");
    }

    // processes a rejected bid message
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

        showAlert(Alert.AlertType.WARNING,
                bidMessageEnum.name(), "Sorry, your bid of $"
                + bidAmount + " on item "
                + currentItemSelected.getTreeItemTitle()
                + " was rejected...Better luck next time!");

        guiStuff.updateBidHistoryTextArea(currentBid.toString());

        System.out.println();
        System.out.println("Received bid successfully");
    }

    // asks the house for the current item list of the house
    private void askHouseItemList() {
        System.out.println();
        System.out.println("Current auction connection: "
                + currentAuctionHouseUser);

        int houseID = currentAuctionHouseUser.getHouseID();

        System.out.println();
        System.out.println("Asking for the house item list from "
                + currentAuctionHouseUser);

        houseWriter.println(MessageEnum.createMessageString(GET_ITEMS,
                new ArrayList<>()));
    }

    // clears the items from the tree view of every house
    private void clearItemsFromAllHouseTreeItem() {
        for (CustomAuctionHouseTreeItem houseTreeItem : houseTreeItemList) {
            houseTreeItem.getChildren().clear();
        }
    }

    // processes the house items given from the house (updates the GUI as well)
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

            itemRootTreeItem.getChildren().add(
                    new CustomItemTreeItem(
                            item.toString(), item));
            itemRootTreeItem.setExpanded(true);

            currentAuctionHouseTreeItem.getChildren().add(
                    itemRootTreeItem);
        }

        System.out.println("The item list for \n" + currentAuctionHouseUser
                + "\nwas setup successfully");

        showAlert(Alert.AlertType.CONFIRMATION,
                "Items Notification",
                "Successfully initialized " + itemCount +
                " items from house id: " + houseID);
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

    // processes the house list given by the bank (also updates the GUI)
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
        System.out.println("Refreshing Items list...");

        for (AuctionHouseUser auctionHouseUser : entireHousesList) {
            if (auctionHouseUser.equals(currentAuctionHouseUser)) {
                currentAuctionHouseUser = auctionHouseUser;
                break;
            }
        }

        for (CustomAuctionHouseTreeItem customAuctionHouseTreeItem :
                houseTreeItemList) {
            if (customAuctionHouseTreeItem.equals(currentAuctionHouseTreeItem)) {
                currentAuctionHouseTreeItem = customAuctionHouseTreeItem;
                break;
            }
        }

        System.out.println();
        System.out.println("Current auction house: " + currentAuctionHouseUser);

        if (currentAuctionHouseUser != null) {
            askHouseItemList();
        }

        System.out.println();
        System.out.println("Auction House List setup successful");
    }

    // updates a separate list of house IDs for the availabel houses
    private void updateHouseIDsList() {
        houseIDsList.clear();

        for (AuctionHouseUser auctionHouseUser : entireHousesList) {
            int houseID = auctionHouseUser.getHouseID();
            houseIDsList.add(houseID);
        }
    }

    // updates the tree view with the current list of houses (for the GUI)
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
            houseTreeView.setExpanded(true);

            rootTreeItem.getChildren().add(houseTreeView);
            houseTreeItemList.add(houseTreeView);

            System.out.println(houseTreeView);
        }
    }

    // processes the message containing the dynamically allocated user ID
    // generated by the bank
    private void getUserID(FullMessage userIDMessage) {
        System.out.println();
        System.out.println("Setting up user ID...");

        List<String> userIDArgs =
                userIDMessage.getMessageArgs();

        userID = Integer.parseInt(userIDArgs.get(0));

        // account ID is the same as the user ID
        userBankAccount = new BankAccount(INITIAL_BALANCE,
                userID);

        initializeLabels();

        System.out.println("Finished...user ID: " + userID);
        System.out.println("Initialized bank account with initial balance: $"
                + INITIAL_BALANCE);

        // FIXME: moved from initialize()
        askAuctionHouseList();
    }

    // FIXME: should be deprecated
    // kind of ran out of time, but this should have been handled by the
    // timers instead
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

    // FIXME: used the getFullMessageFromReader() above and should be updated
    //  -- didn't have enough time
    // processes the exit request by the user and updates the boolean "check"
    // variables according to the message received
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
                showAlert(Alert.AlertType.CONFIRMATION,
                        "Exit " +
                                "Program Successful",
                        "The auction house program " +
                                "was exited successfully.");

                checkBankExit = true;
            } else {
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
                showAlert(Alert.AlertType.CONFIRMATION,
                        "Exit " +
                                "Program Successful",
                        "The auction house program " +
                                "was exited successfully.");

                checkBankExit = true;
                checkHouseExit = true;
            } else {
                showAlert(Alert.AlertType.ERROR,
                        "Exit Auction House Error",
                        "Sorry, you are unable to exit due to " +
                                "various reasons (bids still in progress)...");

                event.consume();
            }
        }
    }

    /**
     * Constructor for the custom tree cell (based on the tree items)
     *
     * If the tree item is for the house, then
     */
    private class CustomTreeCell extends TreeCell<String> {
        // FIXME
        private ContextMenu connectContextMenu = new ContextMenu();

        // Default constructor
        public CustomTreeCell() {

        }

        // FIXME: added synchronized to update item
        @Override
        public void updateItem(String itemString, boolean empty) {
            // Maybe this was why there was some inaccurate
            super.updateItem(itemString, empty) ;

            if (empty) {
                setText(null);
            } else {
                setText(itemString);

                TreeItem<String> treeItem = getTreeItem();

                // maybe this will work...
                // FIXME: add if(connectContextMenu.getItems().isEmpty())
                if (treeItem instanceof CustomAuctionHouseTreeItem) {
                    if (connectContextMenu.getItems().isEmpty()) {
                        MenuItem connectMenuItem = new MenuItem("Connect to this Auction " +
                                "House");
                        connectContextMenu.getItems().add(connectMenuItem);

                        setContextMenu(connectContextMenu);

                        connectMenuItem.setOnAction(event -> {
                            // send request to connect to the chosen auction house

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
                            String houseHostName = currentAuctionHouseUser.getHouseHostName();

                            initializeHouseConnection(houseHostName);

                            askHouseItemList();

                            guiStuff.updateCurrentAuctionHouseLabel(
                                    currentAuctionHouseUser);
                        });
                    }
                } else if (treeItem instanceof CustomItemTreeItem) {
                    CustomItemTreeItem customItemTreeItem =
                            (CustomItemTreeItem) treeItem;

                    Item item = customItemTreeItem.getItem();


                    this.setOnMousePressed(event -> itemSetOnMousePress(event,
                            item));
                }
            }
        }
    }

    /**
     * Replaced the UserGUIActionListener below: will actively check the list
     * of commands to process every 250 milliseconds and execute any commands
     * (corresponding methods given above), if there are any, and also checks
     * if the exit conditions for the user has been met (logic condensed to
     * if the bank allows the user to exit since the bank has information of
     * any bids that are still happening for ALL users)
     */
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

        public Timer getTimer() {
            return timer;
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
                        //System.out.println(checkBankExit);

                        if (checkBankExit) {
                            if (timer == bankActionTimer.getTimer()) {
                                // get a handle to the stage
                                // maybe this was why the stage wasn't closing...

                                System.out.println("Before the stage is exited...");

                                Stage stage = (Stage) userExitButton.getScene().getWindow();
                                stage.close();

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

                                System.out.println("Before closing stream...");

                                closeStreams();

                                System.out.println("Before the alert...");

                                showFinalAlert(Alert.AlertType.CONFIRMATION,
                                        "Exit " +
                                                "Program Successful",
                                        "The auction house program " +
                                                "was exited successfully.");
                            }
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

    /**
     * SHOULD BE DEPRECATED: has basically the same function as the
     * UserGUIActionListener above, except using its own thread with a very
     * bad idea of a while(true) loop...(constantly checking the sockets that
     * may result in poor performance)
     */
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

//                            bankReaderListener.stopRunning();
//                            houseReaderListener.stopRunning();

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

    // handles any bank commands (specified in the doc with all the commands)
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

    // handles any house commands (specified in the doc with all the commands)
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

    // processes an outbid message
    private void getOutBidMessage(FullMessage currentFullMessage) {
        List<String> outBidArgs = currentFullMessage.getMessageArgs();

        int houseID = Integer.parseInt(outBidArgs.get(0));
        int itemID = Integer.parseInt(outBidArgs.get(1));
        int outBidderID = Integer.parseInt(outBidArgs.get(2));
        double newBidAmount = Double.parseDouble(outBidArgs.get(3));
        int lastBidderID = Integer.parseInt(outBidArgs.get(4));

        Bid currentBid = Bid.getBidFromItemID(currentBidList,
                itemID);
        synchronized (currentBidList) {
            currentBidList.remove(currentBid);
        }

        MessageEnum messageEnum = currentFullMessage.getMessageEnum();

        if (lastBidderID == userID) {
            assert currentBid != null;
            double bidAmount = currentBid.getBidAmount();

            userBankAccount.addFunds(bidAmount);

            guiStuff.addFundsToBalanceLabel(bidAmount);
            guiStuff.updateUserBlockAmountLabel(bidAmount,
                    false);

            showAlert(Alert.AlertType.WARNING,
                    messageEnum.name(), "Alert from House ID: " + houseID +
                            "\nSorry, you have been outbid by User ID: " + outBidderID +
                            " on the Item ID: " + itemID + " with a new bid " +
                            "amount of $" + newBidAmount);
        }

        // TODO: update bid history
        guiStuff.updateBidHistoryTextArea(Bid.getAlternateBidString(
                messageEnum, houseID, itemID,
                newBidAmount, lastBidderID));
    }

    // processes a winner message
    private void getWinnerMessage(FullMessage currentFullMessage) {
        List<String> outBidArgs = currentFullMessage.getMessageArgs();

        int houseID = Integer.parseInt(outBidArgs.get(0));
        String itemName = outBidArgs.get(1);
        int itemID = Integer.parseInt(outBidArgs.get(2));
        double newBidAmount = Double.parseDouble(outBidArgs.get(3));

        Bid currentBid = Bid.getBidFromItemID(currentBidList,
                itemID);

        assert currentBid != null;
        double bidAmount = currentBid.getBidAmount();

        guiStuff.updateUserBlockAmountLabel(bidAmount,
                false);

        synchronized (currentBidList) {
            currentBidList.remove(Bid.getBidFromItemID(currentBidList,
                    itemID));
        }

        MessageEnum messageEnum = currentFullMessage.getMessageEnum();

        showAlert(Alert.AlertType.CONFIRMATION,
                messageEnum.name(), "Alert from House ID: " + houseID +
                        "\nCongratulations, you have won the following item!\n" + itemName +
                        "with Item ID: " + itemID + " with a final bid amount of $" + newBidAmount);

        // TODO: update bid history
        guiStuff.updateBidHistoryTextArea(Bid.getAlternateBidString(
                messageEnum, houseID, itemName, itemID,
                newBidAmount));
    }

    // processes an item won message
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

        MessageEnum messageEnum = currentFullMessage.getMessageEnum();

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

    // processes any error message
    private void getErrorMessage(FullMessage currentFullMessage) {
        MessageEnum messageEnum = currentFullMessage.getMessageEnum();

        showAlert(Alert.AlertType.ERROR, messageEnum.name(),
                "The following error message was received" +
                        "...\n" + currentFullMessage);
    }

    // processes any unknown messages that doesn't follow the specified
    // commands in the doc
    private void getUnknownMessage(FullMessage currentFullMessage) {
        showAlert(Alert.AlertType.ERROR, "UNKNOWN",
                "The following unknown message was received" +
                        "...\n" + currentFullMessage);
    }

    // processes the message that the user can exit from the bank
    private void getBankCanExitMessage(FullMessage currentFullMessage) {
        MessageEnum messageEnum = currentFullMessage.getMessageEnum();

        showAlert(
                Alert.AlertType.CONFIRMATION,
                messageEnum.name() + " Bank",
                "You are now disconnected with the " +
                        "You can now exit the bank.");

        // TODO: clear listeners and action list
//        bankReaderListener.stopRunning();
//        bankReaderListener = null;
//        bankActionListener.stopRunning();
//        bankActionListener = null;

        bankMessagesActionList.clear();

        checkBankExit = true;
    }

    // processes the message that the user can exit from the house
    private void getHouseCanExitMessage(FullMessage currentFullMessage) {
        List<String> houseCanExitArgs = currentFullMessage.getMessageArgs();
        int houseID = Integer.parseInt(houseCanExitArgs.get(0));

        MessageEnum messageEnum = currentFullMessage.getMessageEnum();
        showAlert(
                Alert.AlertType.CONFIRMATION,
                messageEnum.name() + " Auction House",
                "You are now disconnected with the " +
                        "current auction house.");

        // TODO: update labels...
        currentAuctionHouseUser = null;

        guiStuff.resetLabel(currentAuctionHouseLabel);

        // TODO: current item selected and item list...
        currentItemSelected = null;
        guiStuff.resetLabel(currentItemSelectedLabel);

        // TODO: clear the children (items) of ALL HOUSES
        clearItemsFromAllHouseTreeItem();

        // TODO: clear listeners and action list
//        houseReaderListener.stopRunning();
//        houseReaderListener = null;
//        houseActionListener.stopRunning();
//        houseActionListener = null;

        houseReader = null;
        houseWriter = null;

        houseMessagesActionList.clear();

        // TODO: clear any other stuff
        currentBidList.clear();
        houseItemList.clear();

        checkHouseExit = true;

        // TODO: maybe refresh the house list afterwards?
        askAuctionHouseList();
    }

    // wrapper method to show a custom alert (easier initialization)
    private void showAlert(Alert.AlertType alertType, String titleAlert,
                               String contextStringAlert) {
        CustomAlert customAlert = new CustomAlert(
                alertType,
                titleAlert,
                contextStringAlert);
        customAlert.show();
    }

    // wrapper method to show a custom alert (easier initialization) when the
    // user is about to leave the program (extra function to the showAlert()
    // method above where the close of the final alert will also force close
    // the program -- hopefully all the resources were close properly...)
    private void showFinalAlert(Alert.AlertType alertType, String titleAlert,
                           String contextStringAlert) {
        CustomAlert customAlert = new CustomAlert(
                alertType,
                titleAlert,
                contextStringAlert);
        customAlert.show();

        // might not be the best way to handle the end of the program
        // need to make sure everything is closed and resources are released...
        customAlert.setOnCloseRequest(event -> {
            System.out.println();
            System.out.println("Exiting program...");
            System.exit(0);
        });
    }
}
