package bank;

import common.AccountType;

import java.io.Serializable;
import java.sql.Date;


public class AccountVO implements Serializable {
    private String owner;
    private String accountNo;
    private AccountType type;
    private long balance;
    private Date openDate;

    public AccountVO() {
    }

    public AccountVO(String owner, String accountNo, AccountType type, long balance, Date openDate) {
        this.owner = owner;
        this.accountNo = accountNo;
        this.type = type;
        this.balance = balance;
        this.openDate = openDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    @Override
    public String toString() {
        return "Account{" +
                "owner='" + owner + '\'' +
                ", accountNo='" + accountNo + '\'' +
                ", type=" + type +
                ", balance=" + balance +
                ", openDate=" + openDate +
                '}';
    }
}
