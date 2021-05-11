/**
 * CS 351L Project 5 - Distributed Auction Houses
 * Pun Chhetri, Isha Chauhan, John Cooper, John Tran
 *
 * The thread that is created for each item. This thread
 * acts as a timer, which invokes a method in the auction
 * house once that timer 'goes off'.
 */

package auctionHouse;

public class TimerThread extends Thread{
    // The value for the start time of the timer
    private long startTime;
    // The number of seconds the timer runs for
    private final int DURATION = 30;
    // The item id for the item being sold
    private int itemId;

    /**
     * Create a timer for a specific item
     * @param itemId The id of the specific item
     */
    public TimerThread(int itemId) {
        this.itemId = itemId;
    }

    @Override
    public void run() {
        boolean run = true;
        startTime = System.nanoTime();
        while(run) {
            if((System.nanoTime() - startTime) > DURATION * 1_000_000_000l) {
                run = false;
                AuctionHouse.timerExpired(itemId);
            }
        }
    }

    /**
     * Starts the timer back at the initial duration remaining
     */
    public synchronized void resetTimer() {
        startTime = System.nanoTime();
    }
}
