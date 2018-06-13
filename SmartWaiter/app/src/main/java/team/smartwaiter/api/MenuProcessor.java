package team.smartwaiter.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MenuProcessor {
    private ApiController controller;

    public MenuProcessor() {
        this.controller = new ApiController();
    }

    public ArrayList<String> goesWellWith(int itemID) {
        ArrayList<String> result = new ArrayList<>();

        JSONArray jsonArray = controller.getItemsThatGoWellWith(itemID);

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                result.add(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
