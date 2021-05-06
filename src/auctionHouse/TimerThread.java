package auctionHouse;

public class TimerThread extends Thread{
    private long startTime;
    private final int DURATION = 30;
    private int itemId;

    public TimerThread(int itemId) {
        this.itemId = itemId;
    }

    @Override
    public void run() {
        boolean run = true;
        startTime = System.nanoTime();
        while(run) {
            if((System.nanoTime() - startTime) > DURATION * 1_000_000_000) {
                run = false;
                AuctionHouse.timerExpired(itemId);
            }
        }
    }

    public synchronized void resetTimer() {
        startTime = System.nanoTime();
    }
}
