package util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.google.gson.reflect.TypeToken;
import com.juniperphoton.jputils.SerializerHelper;
import com.juniperphoton.myerlistandroid.R;

import java.lang.reflect.Type;
import java.util.ArrayList;

import common.AppExtension;
import model.ToDo;
import model.ToDoCategory;
import widget.WidgetProvider;

public class GlobalListLocator {
    public static ArrayList<ToDo> TodosList;
    public static ArrayList<ToDo> DeletedList;
    public static ArrayList<ToDo> StagedList;
    public static ArrayList<ToDoCategory> CategoryList;

    public static boolean onUpdateCateList;

    /**
     * 更新小部件
     */
    public static void updateWidget() {
        int widgetIDs[] = AppWidgetManager.getInstance(AppExtension.getInstance())
                .getAppWidgetIds(new ComponentName(AppExtension.getInstance(), WidgetProvider.class));
        for (int id : widgetIDs) {
            AppWidgetManager.getInstance(AppExtension.getInstance()).notifyAppWidgetViewDataChanged(id, R.id.widget_list_lv);
        }
    }

    /**
     * 更新待办事项
     *
     * @param toDo 待办事项
     */
    public static void updateContent(ToDo toDo) {
        int index = getToDoById(toDo.getID());
        if (index != -1) {
            ToDo todo = TodosList.get(index);
            todo.setContent(toDo.getContent());
            todo.setCate(toDo.getCate());
            todo.setIsDone(toDo.getIsDone());
        }
        saveData();
    }

    /**
     * 删除待办事项
     */
    public static void deleteToDo(String id) {
        int index = getToDoById(id);
        if (index != -1) {
            TodosList.remove(index);
            saveData();
        }
    }

    private static int getToDoById(String id) {
        int index = -1;
        for (int i = 0; i < TodosList.size(); i++) {
            if (TodosList.get(i).getID().equals(id)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 序列化数据到文件保存
     */
    public static void saveData() {
        //updateWidget();
        SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.TodosList, SerializationName.TODOS_FILE_NAME);
        SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.CategoryList, SerializationName.CATES_FILE_NAME);
        SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.StagedList, SerializationName.STAGED_FILE_NAME);
        SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.DeletedList, SerializationName.DELETED_FILE_NAME);
    }

    public static void clearData() {
        AppExtension.getInstance().deleteFile(SerializationName.TODOS_FILE_NAME);
        AppExtension.getInstance().deleteFile(SerializationName.CATES_FILE_NAME);
        AppExtension.getInstance().deleteFile(SerializationName.STAGED_FILE_NAME);
        AppExtension.getInstance().deleteFile(SerializationName.DELETED_FILE_NAME);
    }

    /**
     * 在类别列表插入所有、已删除和自定义项
     *
     * @param categoryList 类别
     */
    public static void makeAndUpdateCategoryList(ArrayList<ToDoCategory> categoryList) {
        categoryList.add(0,
                new ToDoCategory(AppExtension.getInstance().getString(R.string.cate_default), 0,
                        ContextCompat.getColor(AppExtension.getInstance(), R.color.MyerListBlue)));
        categoryList.add(categoryList.size(),
                new ToDoCategory(AppExtension.getInstance().getString(R.string.cate_deleted), -1,
                        ContextCompat.getColor(AppExtension.getInstance(), R.color.DeletedColor)));
        categoryList.add(categoryList.size(),
                new ToDoCategory(AppExtension.getInstance().getString(R.string.cate_per), -2,
                        Color.WHITE));
        CategoryList = categoryList;
    }

    /**
     * 返回自定义类别的类别项目
     *
     * @return 类别项目
     */
    public static ArrayList<ToDoCategory> makeCategoryListForPersonalizaion() {
        ArrayList<ToDoCategory> list = new ArrayList<>();
        for (ToDoCategory cate : CategoryList) {
            if (cate.getID() > 0) {
                list.add(cate);
            }
        }
        list.add(0, new ToDoCategory("PlaceHolder", -3, Color.TRANSPARENT));
        return list;
    }

    /**
     * 根据 ID 返回类别
     *
     * @param id ID
     * @return 类别
     */
    public static ToDoCategory getCategoryByCateID(int id) {
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

    /**
     * 反序列化所有数据
     */
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
