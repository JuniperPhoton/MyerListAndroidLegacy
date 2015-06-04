package model;


public class ScheduleUser
{
    private int sid;
    private String email;
    private String password;

    public ScheduleUser()
    {

    }

    public int GetSID()
    {
        return this.sid;
    }

    public void SetSID(int value)
    {
        this.sid=value;
    }

    public String GetEmail()
    {
        return this.email;
    }

    public void SetEmail(String value)
    {
        this.email=value;
    }

    public String GetPassword()
    {
        return this.password;
    }

    public void SetPassworrd(String value)
    {
        this.password=value;
    }
}
