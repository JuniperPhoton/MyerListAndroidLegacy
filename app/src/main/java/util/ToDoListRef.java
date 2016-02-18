package util;

import java.util.ArrayList;

import model.ToDo;


public class ToDoListRef {
    public static ArrayList<ToDo> TodosList;
    public static ArrayList<ToDo> DeletedList;
    public static ArrayList<ToDo> StagedList;

    public static void RestoreData() {
        TodosList = new ArrayList<>();
        DeletedList = new ArrayList<>();
        StagedList = new ArrayList<>();

        ArrayList<ToDo> deletedContent = SerializerHelper.DeSerializeFromFile(AppExtension.getInstance(), SerializerHelper.deletedFileName);
        if (deletedContent != null) {
            DeletedList = deletedContent;
        }
        ArrayList<ToDo> stagedContent = SerializerHelper.DeSerializeFromFile(AppExtension.getInstance(), SerializerHelper.stagedFileName);
        if (stagedContent != null) {
            StagedList = stagedContent;
        }
    }
}
