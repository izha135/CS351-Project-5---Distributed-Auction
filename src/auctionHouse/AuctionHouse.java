package auctionHouse;

import common.BankAccount;
import common.Item;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.HashMap;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class AuctionHouse extends Serializable {

        private String name;
        private int publicID = 6000;
        private String ahKey; // Requested and received from Auction Central
        private HashMap<Integer, Item> items; //Item ID as key for the item.
        private HashMap<Integer, Timeline> itemTimers;

        private int itemCounter = 0;

        private Item soldItem;


        /**
         * AuctionHouse()
         * Creates an AuctionHouse that has three random items for sale.
         *
         * @param name Name for this AuctionHouse
         */
    public AuctionHouse(String name)
        {
            this.name = name;
            items = new HashMap<>();
            //pendingHolds = new HashSet();
            populateItems();
        }

        /**
         * populateItems()
         *
         * This method fills the auctionHouse with 3 random items. A given item will be initialized with a -1 for it's
         * ahID and will be initialized to an actual valid ahID after the auction house registers with auction central.
         */
        public void populateItems()
        {
            for (int i = 0; i < 3; ++i)
            {
                Item item = ItemDB.getRandomItem();
                item.setItemId(itemCounter);
                items.put(itemCounter, item);
                itemCounter++;
            }
        }

        /**
         * getItemsAsString()
         * @return A String of item toString()'s separated by '\n' characters.
         */
        public String getItemsAsString()
        {
            String output = "";
            ArrayList<Item> itemsAsList = new ArrayList<Item>(items.values());
            for(int i = 0; i < itemsAsList.size(); ++i)
            {
                output += itemsAsList.get(i).toString() + "\n";
            }
            return output;
        }

        public HashMap<Integer, Item> getItems()
        {
            return items;
        }

        /**
         * setIDs()
         *
         * Sets the publicID and auction house key of this auction house.
         *
         * @param publicID ID given to AH from the auction central
         * @param ahKey Key given to aH from the auction central
         */
        public void setIDs(int publicID, String ahKey)
        {
            this.publicID = publicID;
            this.setAhKey(ahKey);
            ArrayList<Item> itemsAsList = new ArrayList<Item>(items.values());
            for(int i = 0; i < itemsAsList.size(); ++i)
            {
                itemsAsList.get(i).setItemId(this.publicID);
            }
        }

        public int getPublicID()
        {
            return publicID;
        }

        public String getName()
        {
            return name;
        }

        /**
         * placeBid()
         * Called by an Agent to place a bid (or by a Client when a PLACE_BID Message is received)
         * @param biddingID      BIDDING_ID of the Agent who wishes to place a bid
         * @param amount         Amount the bidder wishes to bid.
         * @param itemID         ID of the item the bidder wishes to bid on
         * @param auctionHouseID the ID of this auction house (needed by Client)
         * @return true if Client should move ahead and request a hold to be placed.
         *         false if something went wrong (the Agent bid too little,) in which case the Client can send
         *         a bidResponse REJECT Message, not this AuctionHouse's ID, the item doesn't exist here)
         *         right back to the Agent.
         */
        public boolean placeBid(String biddingID, double amount, int itemID, int auctionHouseID)
        {
            //Safechecking
            if(!(auctionHouseID==publicID))
            {
                //Not the right AuctionHouse USER OUTPUT
                System.err.println(toString()+" received a placeBid request for auctionHouseID "+auctionHouseID+" which does" +
                        "not match its public ID "+publicID+". Returning.");
                return false;
            }
            Item item = items.get(itemID);
            if (item == null)
            {
                //That item isn't for sale here USER OUTPUT
                System.err.println("Bidding ID " + biddingID + " tried to bid on " + itemID + ", which is not an item in " +
                        name + ". Returning");
                return false;
            }




        /*public String processHoldResponse(String biddingID, double amount, int itemID, Timeline timer)
        {
            Item item = items.get(itemID);
            String prevBidWinner = item.getCurrentHighestBidderID();
            item.setCurrentBidAndBidder(amount, biddingID);
            setBidTimer(itemID, timer);
            return prevBidWinner;
        }*/

        /**
         * @param itemID    ID of the item stored in the timer that is called when the item is sold.
         * Called when a 'winning' item timer goes off.
         * @return true if AuctionHouse still has items
         *          false if AuctionHouse is out of items and needs to close.
         */
        /*public boolean itemSold(int itemID)
        {
            Item itemSold = items.remove(itemID);
            return !items.isEmpty();
            //return "Item "+itemSold.getItemName()+" has been sold for $"+itemSold.getCurrentBid()+"!";
        }*/

        /*@Override
        public String toString()
        {
            return "Name: " +  name + " Public ID: " + publicID;
        }*/


        private void setSoldItem(Item soldItem)
        {
            this.soldItem = soldItem;
        }

        public Item getSoldItem()
        {
            // TODO: Set to null somewhere in logic of set/get/onfinished
            return soldItem;
        }

        public String getAhKey()
        {
            return ahKey;
        }

        public void setAhKey(String ahKey)
        {
            this.ahKey = ahKey;
        }


        private class PendingBidRequest
        {
            public final String BIDDING_ID;
            public final double AMOUNT;
            public final int ITEM_ID;

            public PendingBidRequest(String bID, double amt, int iID)
            {
                BIDDING_ID = bID;
                AMOUNT = amt;
                ITEM_ID = iID;
            }

            @Override
            public boolean equals(Object obj)
            {
                if(obj instanceof PendingBidRequest)
                {
                    PendingBidRequest br = (PendingBidRequest)obj;
                    return this.BIDDING_ID == br.BIDDING_ID && this.AMOUNT == br.AMOUNT && this.ITEM_ID == br.ITEM_ID;
                }
                else return false;
            }

            //TODO: override hash to use in Set.
        }

        /**
         * itemDB inner Class
         *
         * Reads a static file to populate items into an array.
         * Has a method getRandomItem() to grab a random copy of one of these items.
         * This design is opposed to hosting a real SQL database and sending updates to it. Instead we have a file.
         */
        private static class ItemDB
        {
            private static ArrayList<Item> items;

            // Static initializer that always loads the filelist
            static
            {
                items = new ArrayList<>();

                try
                {
                    InputStream inputFile = ItemDB.class.getResourceAsStream("ItemList.txt");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile, "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        // This allows for commenting in the ItemList
                        if (!line.startsWith("//") && !line.trim().isEmpty())
                        {
                            // The ItemList is fragile, be careful editing it.
                            String[] elements = line.split(",");
                            String itemName = elements[0];
                            String imgPath = elements[1];
                            Double minimumBid = Double.valueOf(elements[2]);
                            items.add(new Item(itemName, imgPath, minimumBid));
                        }
                    }
                    inputFile.close();
                }
                catch (IOException e)
                {
                    System.out.println(e.getMessage());
                }
            }

            /**
             * getRandomItem()
             *
             * Picks a random item from the itemList gotten from ItemList.txt and then returns a copy of it.
             * If it were to return the actual memory object then we would have comparison conflicts elsewhere in
             * the code base. Item name's don't have to be unique but their memory addresses have to be.
             *
             * @return Copy of a random item
             */
            private static Item getRandomItem()
            {
                return new Item(items.get(ThreadLocalRandom.current().nextInt(0, items.size())));
            }
        }

       /* try {
            Socket socket = new Socket(hostName, port);
        //     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        //     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));){

        PrintWriter writer = new PrintWriter(System.out, true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        }
        catch (Exception e) {
          e.printStackTrace();
        }
*/
    }
    /*static class SocketInfo{
        final Socket socket;
        final PrintWriter writer;
        final BufferedReader reader;
        final int portAddress;
        final String hostName;
        final BankAccount account;

        public SocketInfo(Socket socket, PrintWriter writer, BufferedReader reader, int portAddress, String hostName,
                          BankAccount account) {
            this.socket = socket;
            this.writer = writer;
            this.reader = reader;
            this.portAddress = portAddress;
            this.hostName = hostName;
            this.account = account;

        }*/


