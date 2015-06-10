package model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Schedule implements Serializable
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

    public String getID()
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

    public static Schedule parseJsonObjToObj(JSONObject jsonObject)
    {
        try
        {
            Schedule newMemo=new Schedule();
            JSONObject info=jsonObject.getJSONObject("ScheduleInfo");
            newMemo.setID(info.getString("id"));
            newMemo.setSID(info.getString("sid"));
            newMemo.setContent(info.getString("content"));
            newMemo.setIsDone(info.getString("isdone").equals("1"));

            return  newMemo;
        }
        catch (Exception e)
        {
            return null;
        }

    }

    public static List<Schedule> setOrderByString(List<Schedule> oriList,String orderList)
    {
        List<Schedule> listToReturn=new ArrayList<>();
        String[] orders=orderList.split(",");
        for(int i=0;i<orders.length;i++)
        {
            if(orders[i].equals("") || orders[i].equals(" "))
            {
                continue;
            }
            String currentOrder=orders[i];
            for(Schedule s:oriList)
            {
                if(s.getID().equals(currentOrder))
                {
                    listToReturn.add(s);
                    oriList.remove(s);
                    break;
                }
            }
        }
        for(Schedule s:oriList)
        {
            listToReturn.add(s);
        }
        return  listToReturn;
    }


}
