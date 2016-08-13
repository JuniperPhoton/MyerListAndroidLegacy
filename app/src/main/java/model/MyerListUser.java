package model;


public class MyerListUser {
    private int msid;
    private String memail;
    private String mpassword;

    public MyerListUser() {

    }

    public int getSID() {
        return this.msid;
    }

    public void setSID(int value) {
        this.msid = value;
    }

    public String getEmail() {
        return this.memail;
    }

    public void setEmail(String value) {
        this.memail = value;
    }

    public String getPassword() {
        return this.mpassword;
    }

    public void setPassworrd(String value) {
        this.mpassword = value;
    }
}
