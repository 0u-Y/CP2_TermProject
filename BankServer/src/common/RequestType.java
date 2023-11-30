package common;



public enum RequestType {
    VIEW("계좌조회", 10),
    TRANSFER("계좌이체", 20),
    DEPOSIT("입금", 30),
    WITHDRAW("출금", 40),
    LOGIN("로그인", 50),
    BANK_INFO("은행 정보", 99);

    private String name;
    private int number;

    RequestType(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
