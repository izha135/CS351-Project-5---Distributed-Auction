package user.userGUI;

import common.BankAccount;
import common.AuctionHouseUser;
import common.Item;
import common.MessageEnum;
import commonGUI.CustomAuctionHouseTreeItem;
import commonGUI.GuiStuff;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static common.MessageEnum.GET_HOUSES;
import static bank.BankListener.*;
import static common.MessageEnum.GET_ITEMS;

public class UserGUIController {
    @FXML
    Pane pane;

    @FXML
    Label userIDAccountLabel;

    @FXML
    Label userAccountBalanceLabel;

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

    private GuiStuff guiStuff;
    private CustomAuctionHouseTreeItem rootTreeItem;
    private List<CustomAuctionHouseTreeItem> houseTreeItemList = new ArrayList<>();

    private String bankHostName = "localhost";
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

    private CustomAuctionHouseTreeItem currentAuctionHouseTreeItem;
    private CustomAuctionHouseTreeItem currentItemTreeItem;

    private class ConnectTreeCell extends TreeCell<String> {
        private ContextMenu connectContextMenu = new ContextMenu();

        public ConnectTreeCell() {
            MenuItem connectMenuItem = new MenuItem("Connect to this Auction " +
                    "House");
            connectContextMenu.getItems().add(connectMenuItem);
            connectMenuItem.setOnAction(event -> {
                // send request to connect to the chosen auction house
                TreeItem<String> treeItem = getTreeItem();
                String[] houseTreeItemArgs = treeItem.getValue().split(" ");
                String houseID = houseTreeItemArgs[1];
                int parseHouseID = Integer.parseInt(houseID);

                for (CustomAuctionHouseTreeItem houseTreeItem : houseTreeItemList) {
                    if (houseTreeItem.checkID(parseHouseID)) {
                        currentAuctionHouseTreeItem = houseTreeItem;
                        break;
                    }
                }

                //FIXME: update label, get items
                AuctionHouseUser auctionHouseUser =
                        currentAuctionHouseTreeItem.getAuctionHouseUser();
                String houseHostName = auctionHouseUser.getHouseHostName();
                initializeHouseConnection(houseHostName);

                updateHouseItemList();
            });
        }
    }

    @FXML
    public void initialize() {
        //String hostName = "localhost";
        //int port = 3030;

        //try (Socket socket = new Socket(hostName, port);
        //     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        //     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));){

        // FIXME: now with two separate IO, maybe use a file or something
        //  (for the house) for testing...
        initializeBankConnection();
        setUserID();

        updateAuctionHouseList();

        // FIXME: do something with the houses list
        // default id of -1 at the root
        rootTreeItem = new CustomAuctionHouseTreeItem("List of available Auction Houses",
                null);
        rootTreeItem.setExpanded(true);

        houseTreeItemList.clear();

        for (AuctionHouseUser auctionHouseUser : entireHousesList) {
            //TreeItem<String> houseTreeView = new TreeItem<>(houseID);
            int houseID = auctionHouseUser.getHouseID();
            CustomAuctionHouseTreeItem houseTreeView =
                    new CustomAuctionHouseTreeItem("House ID: " + houseID,
                            auctionHouseUser);
            rootTreeItem.getChildren().add(houseTreeView);
            houseTreeItemList.add(houseTreeView);
        }

//        for (int houseID : houseIDsList) {
//            //TreeItem<String> houseTreeView = new TreeItem<>(houseID);
//            CustomAuctionHouseTreeItem houseTreeView =
//                    new CustomAuctionHouseTreeItem("House ID: " + houseID, houseID, auctionHouseUser);
//            rootTreeItem.getChildren().add(houseTreeView);
//            houseTreeItemList.add(houseTreeView);
//        }

        TreeView<String> treeView = new TreeView<>();
        treeView.setRoot(rootTreeItem);

        pane.getChildren().add(treeView);

        guiStuff = new GuiStuff(userIDAccountLabel,
                userAccountBalanceLabel);

        //}
        //catch (Exception e) {
        //    e.printStackTrace();
        //}
    }

    private void initializeBankConnection() {
        try {
            bankSocket = new Socket(bankHostName, port);
            bankWriter = new PrintWriter(bankSocket.getOutputStream(),
                    true);
            bankReader = new BufferedReader(
                    new InputStreamReader(bankSocket.getInputStream()));

            // account ID is the same as the user ID
            userBankAccount = new BankAccount(INITIAL_BALANCE,
                    userID);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void initializeHouseConnection(String houseHostName) {
        try {
            houseSocket = new Socket(houseHostName, port);
            houseWriter = new PrintWriter(houseSocket.getOutputStream());
            houseReader = new BufferedReader(
                    new InputStreamReader(houseSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateHouseItemList() {
        houseItemList.clear();

        houseWriter.write(MessageEnum.createMessageString(GET_ITEMS,
                new ArrayList<>()));

        while (true) {
            try {
                if (houseReader.ready()) {
                    String itemsMessage = houseReader.readLine();
                    List<String> itemsArgsList =
                            MessageEnum.parseMessageArgs(itemsMessage);
                    int houseID = Integer.parseInt(itemsArgsList.get(0));
                    int itemCount = Integer.parseInt(itemsArgsList.get(1));

                    itemsArgsList.remove(0);
                    itemsArgsList.remove(0);

                    int itemArgIndex = 0;
                    for (int i = 0; i < itemCount; i++) {
                        itemArgIndex = i * 4;
                        houseItemList.add(new Item(itemsArgsList.get(itemArgIndex),
                                Integer.parseInt(itemsArgsList.get(itemArgIndex + 1)),
                                Integer.parseInt(itemsArgsList.get(itemArgIndex + 2)),
                                itemsArgsList.get(itemArgIndex + 3)));
                    }

                    // add items to the current house tree item


                    // alert for items
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Items Notification");
                    alert.setContentText("Successfully initialized " + itemCount +
                            " items from house id: " + houseID);
                    break;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

//    private void oldUpdateAllHouseItemList() {
//        for (String houseID : houseIDsList) {
//            List<String> getItemArgs = new ArrayList<>();
//            getItemArgs.add(Integer.toString(userID));
//            getItemArgs.add(houseID);
//            String getItemsMessage =
//                    MessageEnum.createMessageString(GET_ITEMS,
//                            getItemArgs);
//            bankWriter.write(getItemsMessage);
//        }
//
//        int houseCount = houseIDsList.size();
//        int itemListCounter = 0;
//        List<Integer> itemCountList = new ArrayList<>();
//        while (true) {
//            if (itemListCounter < houseCount) {
//                if (bankReader.ready()) {
//                    String itemsMessage = bankReader.readLine();
//                    List<String> itemsArgs =
//                            MessageEnum.parseMessageArgs(itemsMessage);
//                    int currentItemCount =
//                            Integer.parseInt(itemsArgs.get(0));
//                    itemCountList.add(currentItemCount);
//
//                    itemsArgs.remove(0);
//
//                    List<Item> itemList = new ArrayList<>();
//                    int itemArgIndex = 0;
//                    for (int i = 0; i < currentItemCount; i++) {
//                        itemArgIndex = i * 4;
//                        itemList.add(new Item(itemsArgs.get(itemArgIndex),
//                                Integer.parseInt(itemsArgs.get(itemArgIndex + 1)),
//                                Integer.parseInt(itemsArgs.get(itemArgIndex + 2)),
//                                itemsArgs.get(itemArgIndex + 3)));
//                    }
//
//                    entireHousesList.add(new HouseIDItemList(Integer
//                            .parseInt(houseIDsList.get(itemListCounter)),
//                            itemList));
//
//                    itemListCounter++;
//                }
//            } else {
//                break;
//            }
//        }
//
//    for (HouseIDItemList houseIDItemList : entireHousesList) {
//        System.out.println(houseIDItemList);
//    }
//    }

    // get the list of auction houses
    private void updateAuctionHouseList() {
        List<String> getHousesArgs = new ArrayList<>();
        getHousesArgs.add(Integer.toString(userID));
        String getHousesMessage =
                MessageEnum.createMessageString(GET_HOUSES,
                        getHousesArgs);
        bankWriter.write(getHousesMessage);

        List<String> housesArgs;
        while (true) {
            try {
                if (bankReader.ready()) {
                    String housesMessage = bankReader.readLine();
                    housesArgs =
                            MessageEnum.parseMessageArgs(housesMessage);
                    break;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        entireHousesList.clear();
        for (int i = 0; i < housesArgs.size(); i += 2) {
            entireHousesList.add(new AuctionHouseUser(Integer.parseInt(
                    housesArgs.get(i)), housesArgs.get(i + 1)));
        }

        updateHouseIDsList();
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
        while (true) {
            try {
                if (bankReader.ready()) {
                    String userIDMessage = bankReader.readLine();
                    List<String> userIDArgs =
                            MessageEnum.parseMessageArgs(userIDMessage);
                    userID = Integer.parseInt(userIDArgs.get(0));
                    break;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void updateLabels() {

    }
}
