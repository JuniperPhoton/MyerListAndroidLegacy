package model;

import java.util.ArrayList;

import helper.ContextUtil;
import helper.SerializerHelper;


public class ScheduleList
{
    public static ArrayList<Schedule> TodosList;
    public static ArrayList<Schedule> DeletedList;

    public static void SetUpSavedData()
    {
        TodosList=new ArrayList<>();
        DeletedList=new ArrayList<>();

        ArrayList<Schedule> deletedContent= SerializerHelper.DeSerializeFromFile(ContextUtil.getInstance(), SerializerHelper.deletedFileName);
        if(deletedContent!=null)
        {
            DeletedList=deletedContent;
        }
    }
}
