package util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

import model.ToDo;

public class SerializerHelper {
    public static final String todosFileName = "MyTodos.txt";
    public static final String deletedFileName = "Deleted.txt";
    public static final String stagedFileName = "Staged.txt";
    public static final String catesFileName="Cates.txt";

    public static void serializeToFile(Context context, Object o, String fileName) {
        try {
            Gson gson = new Gson();
            String jsonString = gson.toJson(o, o.getClass());
            byte[] bytes = jsonString.getBytes();

            FileOutputStream outputStream = context.openFileOutput(fileName, context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T deSerializeFromFile(Type type,Context context, String fileName) {
        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int pos = 0;
            while ((pos = inputStream.read(bytes)) != -1) {
                byteArrayOutputStream.write(bytes, 0, pos);
            }

            inputStream.close();
            byteArrayOutputStream.close();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.enableComplexMapKeySerialization().create();

            T result = gson.fromJson(byteArrayOutputStream.toString(), type);

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
