package user;

public class BankAccount {
    private double balance;
    private int accountID;
    private double blockedAmount;

    public void addFunds(double fund) {
        balance += fund;
    }

    // maybe take in a bid object...
    public void executeBid(Bid bid) {
        double bidAmount = bid.getBidAmount();

        if (Double.compare(balance, bidAmount) < 0) {
            // throw some exception
        } else {
            balance -= bidAmount;
            blockedAmount += bidAmount;
        }
    }
}
