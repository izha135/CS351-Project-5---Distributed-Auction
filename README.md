## Project 5 - Distributed Auction

#### Pun Chhetri, Isha Chauhan, John Cooper, John Tran

*Note: Auxiliary notes can be found at the bottom of the
README*

### Instructions

There will be three different jar files: one for each part of the Auction House program. Below, we'll list the command line arguments for each jar and how to run it.

`Bank`: 

```
java -jar Bank.jar bankPort
```

where `bankPort` is the port for sockets to connect to with the `Bank`.

`.jar` : `Bank.jar`

`Auction House`:

```
java -jar AuctionHouse.jar bankHostName bankPort housePort
```

`.jar`: `AuctionHouse.jar`

where `bankHostName` is the host name of the bank, `bankPort` is the port number of the bank, and `housePort` is the port number of the house we're trying to run on

`User`:

```
java -jar User.jar bankHostName bankPort someUsername
```

`.jar`: `User.jar`

where `bankHostName` is the host name of the bank, `bankPort` is the port number of the bank, and `someUsername` is any username to be displayed for the user (can be viewed by other others with certain bid updates...)


### The Bank

This object is responsible for managing the monetary
transactions for this distributed auction.

#### Bank

This object is the core of The Bank. It contains all
the bank accounts and socket information to send and
receive messages from the users and auction houses.
Receiving messages is not a multi-threaded process,
but instead it relies on the BufferedReaders to act
as a BlockingQueue (of sorts) for the messages being
received. These are executed iteratively. This does
not seem to be an issue for the scale that this code
will be run and tested on. Multithreading seems a little
overkill when this iterative process works well and
quickly.

*All if the messages that can be sent or received by
the bank are found in the docs folder*

We have also decided that the bank will be responsible
for creating the items that can be sold by the auction
houses in order to avoid duplicate items being sold in
different auction houses.

#### BankDisplay

This is a nice graphical display showing most actions
occurring throughout the auctioning. Displays all items
for sale, what auction houses are currently running, and
more. It makes visualizing the process much easier.

#### BankDisplayThread

An auxiliary thread to run the GUI at the same time
as the bank.

#### Bank Listener

This object is responsible for listening for sockets
joining the server. It expects to read in either a
"user" message, or a "house" message to differentiate
between the two. This is the only busy waiting in the
entire code. For a small example like this, I (John C)
find this to be fine, and if this was going on a larger,
more important system, these would be split onto threads
waiting for that message, and would timeout after a
few seconds if this message wasn't received.

This object will also add new ``SocketInfo`` objects
to the lists that the ``Bank`` iterates over.

### Auction House

This package contains all the logic behind the Auction 
Houses for this project. 

#### AuctionHouse

This class is the core of the Auction House. This
allows for multiple users to connect to it, and also
connects to the one static ``Bank``. Facilitates all
interactions with the items being auctioned. Creates
some asynchronous messages whenever an item was sold.
The ``Bank`` is responsible for informing users
of when an item has been won and is no longer up
for bidding.

#### AuctionHouseListener

Listens for incoming requests to join the auction
from the users. Once joined, the new socket info
is added to the list of users the ``AuctionHouse``
iterates through, checking for new messages. As 
this project is small, putting each of these 
listeners on a separate thread seems a little
overkill (to me, John C). 

#### TimerThread

This is a thread that counts down for thirty
seconds once it starts. These timers can always
be restarted. A timer is created for each item
being sold once any user has bid on it. This
timer executes a function in its parent 
``AuctionHouse`` to indicate the item needs
to be sold.

### User

#### Controller and App

As done with one of my (John T) previous project, the user implements a controller/app design where an FXML file is used to generate the design of the user GUI and everything is controlled and updated via the controller, and the app loads everything and sets up the window (scene).

#### Initial Design with Listeners via Threads

Originally, a similar approach to John C was used where `Listener` classes were created that extended the `Thread` class (I've heard it's bad design...) and a
```java
while(true) {
    // some code
}
```
block was used to actively check commands (messages) sent from the bank and house. This design created some lag on the user end (constantly checking under the true condition without any pauses) and this was ultimately scrapped for the user and to find another way that was more flexible in implementation and control over when the checking would occur during the runtime of the user program. This leads to the next section...

#### Final Design with Timers

This resulted in a little overhaul of the user (besides when the methods had to be split up into handling the input and output (executing the action) separately by adding a `ReaderListener` to process any messages received from the user and a `ActionListener` to execute those actions). Basically, the moment when the messages were processed and sent out (to the respective server &mdash; bank or house) and the moment when they were executed were "decoupled" and this allowed the user display to not freeze when waiting on a response.

However, there were some noticeable slowdowns with the `Listener` approach and a `Timer` was used instead of a separate thread. When thinking back to the Modulo Times Table project (and the Mazes project too), we used an `AnimationTimer` (a timer, nonetheless) to display the successive states and there wasn't a reason to not use some sort of timer again this time (and the added bonus of setting the time between successive checks to `250 milliseconds`). The core concept was the same with the `Listeners`, but as mentioned above, this allowed for more flexibility and cleaner checking since timer objects already run on their own separate threads.

#### Overview of User GUI and Some Comments

Finally, the GUI elements of the user should be pretty obvious throughout the code, but I (John T) opted for a `TreeView` approach where the list of houses currently connected would be listed under some root `TreeItem` object and the list of items (for the currently connected house) would be listed under the house. This was a late change, but the entire tree view should now be expanded on default (hopefully) so the user doesn't have to spend time pulling down the trees to see things.

First, let's discuss the opening of the program. The connection to the bank, if successful, will take a few seconds and an alert saying that the list of houses were successfully loaded will display when everything on the display has finished updating (more details can be seen in the DEBUG statements printed out in System out). After that, the connection with a house can be achieved with a right clock on a house and clicking on the `Connect to this Auction House` or a message similar to it.

Once successful, another alert will display when the items were successfully loaded. At this point, the fun part begins and bidding can start. Before I go into the details of bidding, it would be helpful to describe the display of the user (without actual images) as best I can. So, there should be a few labels on the top center of the display and these display some important information about the user, like the `username`, `account balance`, `blocked amount`, etc. and should update correctly as the program progresses.

One important thing to note is while bidding, only one bid on the same item can occur at a time (an error alert will pop up otherwise) and this is passively checked by the user program. This helps updating the user balance on all ends much easier since bidding on the same item would require further checking on the item and the amount that was bid.

Next, we can discuss the left side of the display and this is the meat of the interaction. This contains the tree view of the houses and items and as mentioned above, connecting to a house just needs a right click. However, for selected an item, there will be helpful labels on the right side of the display to show the currently selected item (just by clicking on the corresponding tree item &mdash; sort of like a dropdown) and will contain some key buttons, which will be discussed shortly. Here, we can also see the current house connection as well as the `TextField` to enter the bid amount. It should be pretty obvious, but all the user needs to do is select an item first (can be checked by the label updating) and entering a bid amount that is at least the amount given in the dropdown of the item (`Item bid`, if I remember correctly) and pressing the bid button and letting the program do its thing.

As with other updates, there should be an alert that would describe the status of the bid (`ACCEPT` or `REJECT`) and further updates as the bid session continues. For example, there could be alerts with `OUTBID`, `WINNER`, or `ITEM WON` according to actions taken by other users and the timer on the item (30 seconds window to bid before it's auctioned off). More will be discussed in the "Bank" section, but there should be a brief description for each alert.

Now onto the buttons: there will be a set of refresh and exit buttons (there's also an `Exit Program` button at the button of the top center labels) that should be pretty self-explanatory. These buttons help to keep the display updated on the latest state of the program and giving the user some way to manually interact with the program itself other than just bidding on an item.

Finally, there should be a `Bid History Text Area` at the bottom of the right hand side and this updates with every message relating to a bid. Any updates (even if the specified user wasn't directly involved) will be displayed here and will be some command at the beginning of each entry to display what kind of update it was. This serves as a way for the user to see a brief list of the bid history and to bid on items accordingly (if they so choose).

With the overview of the display out of the way, we can get back to bidding. Once, the user placed a bid, it's pretty much a free for all and it's up to the user to bid on the items they want and how much they want to spend. Once, the user has had their fill, the exit buttons will be the last course of action and there's an option to leave the house (and maybe join another house afterwards) or leave the bank (which is effectively the same as the `Exit Program` button, but this is more of an all purpose button that will check both the current house connected and the bank). The program will give a final alert when the user is able to exit, and once the alert is closed, the program should end. That's pretty much all there is to it.

#### Final Design with Timers

This resulted in a little overhaul of the user (besides when the methods had to be split up into handling the input and output (executing the action) separately by adding a `ReaderListener` to process any messages received from the user and a `ActionListener` to execute those actions). Basically, the moment when the messages were processed and sent out (to the respective server &mdash; bank or house) and the moment when they were executed were "decoupled" and this allowed the user display to not freeze when waiting on a response.

However, there were some noticeable slowdowns with the `Listener` approach and a `Timer` was used instead of a separate thread. When thinking back to the Modulo Times Table project (and the Mazes project too), we used an `AnimationTimer` (a timer, nonetheless) to display the successive states and there wasn't a reason to not use some sort of timer again this time (and the added bonus of setting the time between successive checks to `250 milliseconds`). The core concept was the same with the `Listeners`, but as mentioned above, this allowed for more flexibility and cleaner checking since timer objects already run on their own separate threads.

#### Overview of User GUI and Some Comments

Finally, the GUI elements of the user should be pretty obvious throughout the code, but I (John T) opted for a `TreeView` approach where the list of houses currently connected would be listed under some root `TreeItem` object and the list of items (for the currently connected house) would be listed under the house. This was a late change, but the entire tree view should now be expanded on default (hopefully) so the user doesn't have to spend time pulling down the trees to see things.

First, let's discuss the opening of the program. The connection to the bank, if successful, will take a few seconds and an alert saying that the list of houses were successfully loaded will display when everything on the display has finished updating (more details can be seen in the DEBUG statements printed out in System out). After that, the connection with a house can be achieved with a right clock on a house and clicking on the `Connect to this Auction House` or a message similar to it.

Once successful, another alert will display when the items were successfully loaded. At this point, the fun part begins and bidding can start. Before I go into the details of bidding, it would be helpful to describe the display of the user (without actual images) as best I can. So, there should be a few labels on the top center of the display and these display some important information about the user, like the `username`, `account balance`, `blocked amount`, etc. and should update correctly as the program progresses.

One important thing to note is while bidding, only one bid on the same item can occur at a time (an error alert will pop up otherwise) and this is passively checked by the user program. This helps updating the user balance on all ends much easier since bidding on the same item would require further checking on the item and the amount that was bid.

Next, we can discuss the left side of the display and this is the meat of the interaction. This contains the tree view of the houses and items and as mentioned above, connecting to a house just needs a right click. However, for selected an item, there will be helpful labels on the right side of the display to show the currently selected item (just by clicking on the corresponding tree item &mdash; sort of like a dropdown) and will contain some key buttons, which will be discussed shortly. Here, we can also see the current house connection as well as the `TextField` to enter the bid amount. It should be pretty obvious, but all the user needs to do is select an item first (can be checked by the label updating) and entering a bid amount that is at least the amount given in the dropdown of the item (`Item bid`, if I remember correctly) and pressing the bid button and letting the program do its thing.

As with other updates, there should be an alert that would describe the status of the bid (`ACCEPT` or `REJECT`) and further updates as the bid session continues. For example, there could be alerts with `OUTBID`, `WINNER`, or `ITEM WON` according to actions taken by other users and the timer on the item (30 seconds window to bid before it's auctioned off). More will be discussed in the "Bank" section, but there should be a brief description for each alert.

Now onto the buttons: there will be a set of refresh and exit buttons (there's also an `Exit Program` button at the button of the top center labels) that should be pretty self-explanatory. These buttons help to keep the display updated on the latest state of the program and giving the user some way to manually interact with the program itself other than just bidding on an item.

Finally, there should be a `Bid History Text Area` at the bottom of the right hand side and this updates with every message relating to a bid. Any updates (even if the specified user wasn't directly involved) will be displayed here and will be some command at the beginning of each entry to display what kind of update it was. This serves as a way for the user to see a brief list of the bid history and to bid on items accordingly (if they so choose).

With the overview of the display out of the way, we can get back to bidding. Once, the user placed a bid, it's pretty much a free for all and it's up to the user to bid on the items they want and how much they want to spend. Once, the user has had their fill, the exit buttons will be the last course of action and there's an option to leave the house (and maybe join another house afterwards) or leave the bank (which is effectively the same as the `Exit Program` button, but this is more of an all purpose button that will check both the current house connected and the bank). The program will give a final alert when the user is able to exit, and once the alert is closed, the program should end. That's pretty much all there is to it.

### Common

These objects are either used by multiple packages,
or might be useful for multiple packages to user in
the future.

#### BankAccount

This object contains the relevant information that
a bank account would have. These include the name on
the account, the funds in the account, the funds that
are blocked off, and the account id.

#### HouseIDItemList

A list of all the items in the house.

#### Item

An object containing all the information in an item.
This has a name, a description, the current highest
bid, etc.

#### MessageEnum

This enumeration enumerates all the different messages
that are being sent over the network. Has a nice
``toString`` method making constructing messages
easier. Also has a parsing method allowing for strings
to be converted to this enum for ease of use.

### Common GUI

These are elements used by the ``User`` GUI that also
might be useful in the future.

### Auxiliary

We decided to split the work up mostly by the different
main parts of this project. John C was mostly responsible
for the ``Bank``. John T was mostly responsible for the
``User``. Pun and Isha were mostly responsible for the
``AuctionHouse``. We certainly did work on some parts of
the project not in our sections of focus as well.

All communication protocols can be found in the docs
folder.

- Some of the classes in the project extend `Serializable`, but that marker wasn't used as we decided to stick only with strings when sending stuff through the sockets (since the messages could be parsed out on either end of communication and objects could be created on the ends, instead of writing and sending objects)

- As with other projects, only the really important methods were commented (whereas `getter` and `setter` methods and relatively simple and short methods should be apparent). Especially with this project, all of the methods were broken up into their functionality and the names were pretty useful in this case. The important thing for the user, though, was how the methods were generally grouped into `ask` and `get` methods (another prefix besides `get` would probably been better so not to be confused with `getter` methods) where the `ask` methods would prompt the respective writer to write a given message and the `get` would execute the response of a given message.