package util;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import model.ToDo;


public class ToDoListReference {
    public static ArrayList<ToDo> TodosList;
    public static ArrayList<ToDo> DeletedList;
    public static ArrayList<ToDo> StagedList;

    public static void restoreData() {
        TodosList = new ArrayList<>();
        DeletedList = new ArrayList<>();
        StagedList = new ArrayList<>();

        Type type = new TypeToken<ArrayList<ToDo>>() {}.getType();

        ArrayList<ToDo> deletedContent = SerializerHelper.deSerializeFromFile(
                type,
                AppExtension.getInstance(),
                SerializerHelper.deletedFileName);
        if (deletedContent != null) {
            DeletedList = deletedContent;
        }
        ArrayList<ToDo> stagedContent = SerializerHelper.deSerializeFromFile(
                type,
                AppExtension.getInstance(),
                SerializerHelper.stagedFileName);
        if (stagedContent != null) {
            StagedList = stagedContent;
        }
    }
}
