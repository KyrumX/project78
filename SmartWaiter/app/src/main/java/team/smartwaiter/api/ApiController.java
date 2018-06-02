package team.smartwaiter.api;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ApiController {

    private final String DEFAULT_URL = "http://86.82.103.122:8080";

    public void Print() throws IOException {
        getMenu();

    }

    public JSONArray getMenu() {
        String RequestedUrl = DEFAULT_URL + "/api/menu/";
        String result;
        JSONArray jsonArray;
        HttpGetRequest request = new HttpGetRequest();

        try {
            result = request.execute(RequestedUrl).get();
            Object json = new JSONTokener(result).nextValue();
            if(json instanceof JSONObject) {
                return null;
            }
            else if (json instanceof JSONArray) {
                jsonArray = new JSONArray(result);
                return jsonArray;
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return null;
    }


}
