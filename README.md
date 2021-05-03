## Project 5 - Distributed Auction

#### Pun Chhetri, Isha Chauhan, John Cooper, John Tran

*Note: Auxiliary notes can be found at the bottom of the
README*

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



### User



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