package team.smartwaiter.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Serializer {
    public static ArrayList<String> convertMenu(JSONArray jsonArray, String wantedValue) {
        ArrayList<String> l = new ArrayList<>();

        if(jsonArray == null)
            return l;

        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                l.add(jsonArray.getJSONObject(i).get(wantedValue).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return l;
    }

    public static HashMap<String, String> menuItemInformation(JSONObject jsonObject) {
        HashMap<String, String> hm = new HashMap<String, String>();

        if(jsonObject == null)
            return hm;
        try {
            hm.put("name", jsonObject.get("name").toString());
            hm.put("allergy", jsonObject.get("allergy").toString());
            hm.put("description", jsonObject.get("description").toString());
            hm.put("price", jsonObject.get("price").toString());
            hm.put("type", jsonObject.get("type").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return hm;
    }

    public static int orderID(JSONObject jsonObject) {
        int orderID = -1;

        if(jsonObject == null)
            return orderID;
        try {
            orderID = (int) jsonObject.get("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return orderID;
    }

    public static double orderSum(JSONObject jsonObject) {
        double sum = -1;

        if(jsonObject == null)
            return -1;
        try {
            if (jsonObject.get("sum") instanceof Integer) {
                Integer value = (Integer) jsonObject.get("sum");
                sum = value.doubleValue();
            }
            else {
                sum = (double) jsonObject.get("sum");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sum;
    }

    public static HashMap<Integer, String> MenuItems(JSONArray jsonArray) {
        HashMap<Integer, String> hm = new HashMap<Integer, String>();

        if(jsonArray == null)
            return hm;

        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                hm.put(((int) jsonArray.getJSONObject(i).get("id")), jsonArray.getJSONObject(i).get("name").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return hm;
    }

    public static HashMap<String, Integer> orderLines(JSONObject jsonObject) {
        HashMap<String, Integer> result = new HashMap();
        ApiController controller = new ApiController();
        HashMap menu = Serializer.MenuItems(controller.getMenu());

        if (jsonObject == null) {
            return result;
        }
        try {
            JSONArray array = jsonObject.getJSONArray("lines");
            for(int i = 0; i < array.length(); i++) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    String id = (String) menu.get(object.getInt("menuitem_id"));
                    result.put(id, object.getInt("amount"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
