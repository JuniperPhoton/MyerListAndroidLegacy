package model;

import java.util.ArrayList;

import helper.ContextUtil;
import helper.SerializerHelper;


public class ToDoListHelper
{
    public static ArrayList<ToDo> TodosList;
    public static ArrayList<ToDo> DeletedList;

    public static void SetUpSavedData()
    {
        TodosList=new ArrayList<>();
        DeletedList=new ArrayList<>();

        ArrayList<ToDo> deletedContent= SerializerHelper.DeSerializeFromFile(ContextUtil.getInstance(), SerializerHelper.deletedFileName);
        if(deletedContent!=null)
        {
            DeletedList=deletedContent;
        }
    }
}
