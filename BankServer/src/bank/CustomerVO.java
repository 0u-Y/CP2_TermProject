package bank;

import java.io.Serializable;


public class CustomerVO implements Serializable {
    private String id;
    private String name;
    private String password;
    private String address;
    private String phone;
    private AccountVO account;


    public CustomerVO() {
    }

    public CustomerVO(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public CustomerVO(String id, String name, String password, AccountVO account) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.account = account;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AccountVO getAccount() {
        return account;
    }

    public void setAccount(AccountVO account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "CustomerVO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", account=" + account +
                '}';
    }
}
