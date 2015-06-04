package model;


public class Schedule
{
    private String id;
    private String sid;
    private String content;
    private int order;
    private boolean isDone;

    public Schedule()
    {

    }

    public String GetID()
    {
        return this.id;
    }

    public void SetID(String value)
    {
        this.id=value;
    }

    public String GetSID()
    {
        return this.sid;
    }

    public void SetSID(String value)
    {
        this.sid=value;
    }

    public String GetContent()
    {
        return this.content;
    }

    public void GetContent(String value)
    {
        this.content=value;
    }

    public int GetOrder()
    {
        return this.order;
    }

    public void SetOrder(int value)
    {
        this.order=value;
    }

    public boolean GetIsDone()
    {
        return this.isDone;
    }

    public void SetIsDone(boolean value)
    {
        this.isDone=value;
    }


}
