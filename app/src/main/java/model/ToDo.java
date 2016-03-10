package model;


import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class ToDo implements Serializable {
    private String id;
    private String sid;
    private String content = "";
    private int cate = 0;
    private boolean isDone;

    public ToDo(String content, boolean isDone) {
        this.content = content;
        this.isDone = isDone;
    }

    public ToDo() {

    }

    public String getID() {
        return this.id;
    }

    public void setID(String value) {
        this.id = value;
    }

    public String getSID() {
        return this.sid;
    }

    public void setSID(String value) {
        this.sid = value;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String value) {
        this.content = value;
    }


    public boolean getIsDone() {
        return this.isDone;
    }

    public void setIsDone(boolean value) {
        this.isDone = value;
    }

    public void setCate(int cate) {
        this.cate = cate;
    }

    public int getCate() {
        return this.cate;
    }

    public static ArrayList<ToDo> parseJsonObjFromArray(JSONArray array) {
        ArrayList<ToDo> listToReturn = new ArrayList<ToDo>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject item = array.getJSONObject(i);
                    ToDo newItem = parseJsonObjToObj(item);
                    listToReturn.add(newItem);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return listToReturn;
    }

    public static ToDo parseJsonObjToObj(JSONObject jsonObject) {
        try {
            ToDo newItem = new ToDo();
            newItem.setID(jsonObject.getString("id"));
            newItem.setSID(jsonObject.getString("sid"));
            newItem.setContent(jsonObject.getString("content"));
            newItem.setIsDone(jsonObject.getString("isdone").equals("1"));
            newItem.setCate(Integer.parseInt(jsonObject.getString("cate")));
            return newItem;
        }
        catch (Exception e) {
            return null;
        }

    }

    public static ArrayList<ToDo> setOrderByString(ArrayList<ToDo> oriList, String orderList) {
        ArrayList<ToDo> listToReturn = new ArrayList<>();
        String[] orders = orderList.split(",");
        for (int i = 0; i < orders.length; i++) {
            if (orders[i].equals("") || orders[i].equals(" ")) {
                continue;
            }
            String currentOrder = orders[i];
            for (ToDo s : oriList) {
                if (s.getID().equals(currentOrder)) {
                    listToReturn.add(s);
                    oriList.remove(s);
                    break;
                }
            }
        }
        for (ToDo s : oriList) {
            listToReturn.add(s);
        }
        return listToReturn;
    }

    public static String getOrderString(ArrayList<ToDo> list) {
        StringBuilder builder = new StringBuilder();
        for (ToDo s : list) {
            builder.append(s.getID());
            builder.append(',');
        }
        return builder.toString();
    }

}
