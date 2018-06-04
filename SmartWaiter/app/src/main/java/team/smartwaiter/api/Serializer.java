package team.smartwaiter.api;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class Serializer {
    public static ArrayList<String> ConvertMenu(JSONArray jsonArray) {
        ArrayList<String> l = new ArrayList<>();

        if(jsonArray == null)
            return l;

        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                l.add(jsonArray.getJSONObject(i).get("name").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return l;
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
}
