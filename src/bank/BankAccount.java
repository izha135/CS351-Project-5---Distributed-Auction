package bank;

public class BankAccount {
    private double balance;
    private final int accountID;
    //private List<Request> requests;
    private double remainingBalance;

    public BankAccount(double initalBalance, int accountId) {
        this.balance = initalBalance;
        this.accountID = accountId;
        //requests = new LinkedList<>();
        remainingBalance = initalBalance;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getAccountID() {
        return accountID;
    }

    public double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public void removeFunds(double change) {
        remainingBalance -= change;
    }

    public void addFunds(double change) {
        remainingBalance += change;
    }

    //class Request {
//
    //}
}
