package user;

import common.HouseIDItemList;
import common.Item;
import common.MessageEnum;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static common.MessageEnum.*;

public class User {
    private String userName;
    private static int userID;
    private static List<HouseIDItemList> entireHousesList = new ArrayList<>();

    // FIXME: move variables out into member variables...
    public static void main(String[] args) {
        String hostName = "localhost";
        int port = 3030;

        //try (Socket socket = new Socket(hostName, port);
        //     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        //     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));){

        // FIXME: use socket above
        try (PrintWriter writer = new PrintWriter(System.out, true);
             BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(System.in))) {
            // FIXME: maybe incorporate GUI later...

            // get the user id
            while (true) {
                if (reader.ready()) {
                    String userIDMessage = reader.readLine();
                    List<String> userIDArgs =
                            MessageEnum.parseMessageArgs(userIDMessage);
                    userID = Integer.parseInt(userIDArgs.get(0));
                    break;
                }
            }

            // get the list of auction houses
            List<String> getHousesArgs = new ArrayList<>();
            getHousesArgs.add(Integer.toString(userID));
            String getHousesMessage =
                    MessageEnum.createMessageString(GET_HOUSES,
                            getHousesArgs);
            writer.write(getHousesMessage);

            List<String> housesArgs;
            while (true) {
                if (reader.ready()) {
                    String housesMessage = reader.readLine();
                    housesArgs =
                            MessageEnum.parseMessageArgs(housesMessage);
                    break;
                }
            }

            // get the list of items for EACH AUCTION HOUSE
            List<String> houseIDs = new ArrayList<>(housesArgs);
            for (String houseID : houseIDs) {
                List<String> getItemArgs = new ArrayList<>();
                getItemArgs.add(Integer.toString(userID));
                getItemArgs.add(houseID);
                String getItemsMessage =
                        MessageEnum.createMessageString(GET_ITEMS,
                                getItemArgs);
                writer.write(getItemsMessage);
            }

            int houseCount = houseIDs.size();
            int itemListCounter = 0;
            List<Integer> itemCountList = new ArrayList<>();
            while (true) {
                if (itemListCounter < houseCount) {
                    if (reader.ready()) {
                        String itemsMessage = reader.readLine();
                        List<String> itemsArgs =
                                MessageEnum.parseMessageArgs(itemsMessage);
                        int currentItemCount =
                                Integer.parseInt(itemsArgs.get(0));
                        itemCountList.add(currentItemCount);

                        itemsArgs.remove(0);

                        List<Item> itemList = new ArrayList<>();
                        int itemArgIndex = 0;
                        for (int i = 0; i < currentItemCount; i++) {
                            itemArgIndex = i * 4;
                            itemList.add(new Item(itemsArgs.get(itemArgIndex),
                                    Integer.parseInt(itemsArgs.get(itemArgIndex + 1)),
                                    Integer.parseInt(itemsArgs.get(itemArgIndex + 2)),
                                    itemsArgs.get(itemArgIndex + 3)));
                        }

                        entireHousesList.add(new HouseIDItemList(Integer
                                .parseInt(houseIDs.get(itemListCounter)),
                                itemList));

                        itemListCounter++;
                    }
                } else {
                    break;
                }
            }

            // FIXME: do something with the houses list
            // FIXME: print out the houses/items (only when using actual socket)

            for (HouseIDItemList houseIDItemList : entireHousesList) {
                System.out.println(houseIDItemList);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }


        //}
        //catch (Exception e) {
        //    e.printStackTrace();
        //}
    }

    private void placeBid(int bidAmount, int itemID, int houseID) {

    }

    private void promptInitialMessage() {
        System.out.println();
        System.out.println("Welcome to the Auction House simulation!");
        System.out.println("Please ");
    }
}
