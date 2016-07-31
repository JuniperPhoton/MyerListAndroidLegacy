package util;

import android.graphics.Color;

import com.google.gson.reflect.TypeToken;
import com.juniperphoton.jputils.SerializerHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;

import model.ToDo;
import model.ToDoCategory;

public class GlobalListLocator {
    public static ArrayList<ToDo> TodosList;
    public static ArrayList<ToDo> DeletedList;
    public static ArrayList<ToDo> StagedList;
    public static ArrayList<ToDoCategory> CategoryList;

    public static ArrayList<ToDoCategory> makeCategoryListForPersonalizaion() {
        ArrayList<ToDoCategory> list = new ArrayList<>();
        for (ToDoCategory cate : CategoryList) {
            if (cate.getID() > 0) {
                list.add(cate);
            }
        }
        list.add(0,new ToDoCategory("PlaceHolder",-3, Color.TRANSPARENT));
        return list;
    }

    public static ToDoCategory GetCategoryByCateID(int id) {
        if (CategoryList == null) {
            return null;
        }
        ToDoCategory foundCate = null;
        for (ToDoCategory cate : CategoryList) {
            if (cate.getID() == id) {
                foundCate = cate;
            }
        }
        if (foundCate == null) {
            return CategoryList.get(0);
        }
        return foundCate;
    }

    public static void restoreData() {
        TodosList = new ArrayList<>();
        DeletedList = new ArrayList<>();
        StagedList = new ArrayList<>();

        Type type = new TypeToken<ArrayList<ToDo>>() {
        }.getType();

        ArrayList<ToDo> deletedContent = SerializerHelper.deSerializeFromFile(
                type,
                AppExtension.getInstance(),
                SerializationName.DELETED_FILE_NAME);
        if (deletedContent != null) {
            DeletedList = deletedContent;
        }
        ArrayList<ToDo> stagedContent = SerializerHelper.deSerializeFromFile(
                type,
                AppExtension.getInstance(),
                SerializationName.STAGED_FILE_NAME);
        if (stagedContent != null) {
            StagedList = stagedContent;
        }

        Type type2 = new TypeToken<ArrayList<ToDoCategory>>() {
        }.getType();

        ArrayList<ToDoCategory> categories = SerializerHelper.deSerializeFromFile(
                type2,
                AppExtension.getInstance(),
                SerializationName.CATES_FILE_NAME);
        if (categories != null) {
            CategoryList = categories;
        }
    }
}
