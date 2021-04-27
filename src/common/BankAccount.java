/**
 * CS 351L Project 5 - Distributed Auction Houses
 * Pun Chhetri, Isha Chauhan, John Cooper, John Tran
 *
 * Data-structure object that contains the information pertaining to
 * a user's or house's bank account
 */

package common;

public class BankAccount {
    // The current balance of the account
    private double balance;
    // The unique identifying number of the account
    private final int accountID;
    // The balance quantity not currently blocked by the bank
    private double remainingBalance;

    /**
     * @param initialBalance The balance to start the account at
     * @param accountId The unique identifying value of the account
     */
    public BankAccount(double initialBalance, int accountId) {
        this.balance = initialBalance;
        this.accountID = accountId;
        remainingBalance = initialBalance;
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

    /**
     * Decreases the current balance by a specified amount
     * @param change The amount to decrease the balance by
     */
    public void removeFunds(double change) {
        remainingBalance -= change;
    }

    /**
     * Increases the current balance by a specified amount
     * @param change The amount to increase the balance by
     */
    public void addFunds(double change) {
        remainingBalance += change;
    }
}
