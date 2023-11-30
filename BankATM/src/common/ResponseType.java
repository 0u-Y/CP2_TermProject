package common;




public enum ResponseType {
    SUCCESS("성공", 200),
    INSUFFICIENT("잔액부족", 400),
    WRONG_PASSWORD("비밀번호오류", 401),
    WRONG_ACCOUNT_NO("계좌번호오류", 402),
    FAILURE("로그인실패", 404);

    private String name;
    private int number;

    ResponseType(String name, int number) {
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
