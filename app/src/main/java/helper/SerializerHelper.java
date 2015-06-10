package helper;

import android.content.Context;

import com.google.gson.Gson;
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

import model.Schedule;

public class SerializerHelper
{
    public static final String todosFileName="MyTodos.txt";
    public static  void toStringAndSave(Context context,Object o,Type type)
    {
        try
        {
            Gson gson=new Gson();
            String jsonString=gson.toJson(o, type);
            byte[] bytes=jsonString.getBytes();

            FileOutputStream outputStream=context.openFileOutput(todosFileName,context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static <T>  T readFromFile(Class<T> c,Context context)
    {
        try
        {
            FileInputStream inputStream=context.openFileInput(todosFileName);
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
            T list=gson.fromJson(byteArrayOutputStream.toString(), new TypeToken<T>(){}.getType());
            return list;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }


}
