package user.userGUI;

import common.BankAccount;
import common.HouseIDItemList;
import common.Item;
import common.MessageEnum;
import commonGUI.CustomTreeItem;
import commonGUI.GuiStuff;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static common.MessageEnum.GET_HOUSES;
import static common.MessageEnum.GET_ITEMS;
import static bank.BankListener.*;

public class UserGUIController {
    @FXML
    Pane pane;

    @FXML
    Label userIDAccountLabel;

    @FXML
    Label userAccountBalanceLabel;

    private GuiStuff guiStuff;
    private TreeItem<String> rootTreeItem;
    private List<CustomTreeItem> houseTreeItemList;

    private String bankHostName = "localhost";
    private int port = 3030;
    private int userID;
    private BankAccount userBankAccount;
    private List<Integer> houseIDsList;
    private List<HouseIDItemList> entireHousesList = new ArrayList<>();

    private Socket bankSocket;
    private PrintWriter bankWriter;
    private BufferedReader bankReader;

    private Socket houseSocket;
    private PrintWriter houseWriter;
    private BufferedReader houseReader;

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
        rootTreeItem = new TreeItem<>("Auction Houses");

        for (int houseID : houseIDsList) {
            //TreeItem<String> houseTreeView = new TreeItem<>(houseID);
            CustomTreeItem houseTreeView =
                    new CustomTreeItem("House ID: " + houseID, houseID);
            rootTreeItem.getChildren().add(houseTreeView);
        }

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

        houseIDsList = new ArrayList<>();

        for (String housesArg : housesArgs) {
            houseIDsList.add(Integer.parseInt(housesArg));
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
