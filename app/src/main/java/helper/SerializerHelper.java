package helper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.juniper.myerlistandroid.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import activity.MainActivity;
import model.Schedule;
import service.ListWidgetService;
import widget.ListWidgetProvider;

public class SerializerHelper
{
    public static final String todosFileName="MyTodos.txt";
    public static final String deletedFileName="Deleted.txt";

    public static  void SerializeToFile(Context context, Object o, String fileName)
    {
        try
        {
            Gson gson=new Gson();
            String jsonString=gson.toJson(o, o.getClass());
            byte[] bytes=jsonString.getBytes();

            FileOutputStream outputStream=context.openFileOutput(fileName, context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static  ArrayList<Schedule> DeSerializeFromFile(Context context, String fileName)
    {
        try
        {
            FileInputStream inputStream=context.openFileInput(fileName);
            byte[] bytes=new byte[1024];
            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            int pos=0;
            while ((pos=inputStream.read(bytes))!=-1)
            {
                byteArrayOutputStream.write(bytes,0,pos);
            }

            inputStream.close();
            byteArrayOutputStream.close();

            Gson gson=new Gson();
            GsonBuilder builder = new GsonBuilder();
            gson = builder.enableComplexMapKeySerialization().create();
            Type listType = new TypeToken<ArrayList<Schedule>>() {}.getType();

            ArrayList<Schedule> list=gson.fromJson(byteArrayOutputStream.toString(), listType);

            return list;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }


}
