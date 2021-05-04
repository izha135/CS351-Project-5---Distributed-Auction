package auctionHouse;

public class TimerThread extends Thread{
    private long startTime;
    private final int DURATION = 30;
    private AuctionHouse house;
    private int itemId;

    public TimerThread(AuctionHouse house, int itemId) {
        startTime = System.nanoTime();
        this.house = house;
        this.itemId = itemId;
    }

    @Override
    public void run() {
        boolean run = true;
        while(run) {
            if((System.nanoTime() - startTime) > DURATION * 1_000_000_000) {
                run = false;
                house.timerExpired(itemId);
            }
        }
    }

    public synchronized void resetTimer() {
        startTime = System.nanoTime();
    }
}
