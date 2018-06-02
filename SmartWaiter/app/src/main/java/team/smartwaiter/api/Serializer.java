package team.smartwaiter.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
}
