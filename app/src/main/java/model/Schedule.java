package model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Schedule
{
    private String id;
    private String sid;
    private String content;
    private int order;
    private boolean isDone;

    public Schedule(String content,boolean isDone)
    {

    }

    public Schedule()
    {

    }

    public String GetID()
    {
        return this.id;
    }

    public void setID(String value)
    {
        this.id=value;
    }

    public String getSID()
    {
        return this.sid;
    }

    public void setSID(String value)
    {
        this.sid=value;
    }

    public String getContent()
    {
        return this.content;
    }

    public void setContent(String value)
    {
        this.content=value;
    }

    public int getOrder()
    {
        return this.order;
    }

    public void setOrder(int value)
    {
        this.order=value;
    }

    public boolean getIsDone()
    {
        return this.isDone;
    }

    public void setIsDone(boolean value)
    {
        this.isDone=value;
    }

    public static List<Schedule> parseJsonObjFromArray(JSONArray array)
    {
        List<Schedule> listToReturn=new ArrayList<Schedule>();
        if(array!=null)
        {
            for(int i=0;i<array.length();i++)
            {
                try
                {
                    JSONObject item=array.getJSONObject(i);
                    Schedule newSchedule=new Schedule();
                    newSchedule.setID(item.getString("id"));
                    newSchedule.setSID(item.getString("sid"));
                    newSchedule.setContent(item.getString("content"));
                    newSchedule.setIsDone(!item.getString("isdone").equals("0"));
                    listToReturn.add(newSchedule);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return listToReturn;
    }


}
