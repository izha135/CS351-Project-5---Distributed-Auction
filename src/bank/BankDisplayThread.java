package bank;

public class BankDisplayThread extends Thread {
    BankDisplay bankDisplay;
    private boolean run;

    public BankDisplayThread() {
        bankDisplay = new BankDisplay();
    }

    @Override
    public void run() {
        bankDisplay.createDisplay();
    }

    public BankDisplay getBankDisplay() {
        return bankDisplay;
    }
}