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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class ApiController {

    private final String DEFAULT_URL = "http://86.82.103.122:8080";

    public void Print() {
        test();

    }

    public JSONArray getMenu() {
        String RequestedUrl = DEFAULT_URL + "/api/menu/";
        String result;
        JSONArray jsonArray;
        HttpRequest request = new HttpGetRequest();

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

    public JSONObject test() {
        String RequestedUrl = DEFAULT_URL + "/api/orders/";
        String result;
        String params;
        JSONObject jsonObject;
        HttpRequest request = new HttpPostRequest();

        //Create a date:

        Date mydate = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String mydateStr = df.format(mydate);

        params = "tablenumber=666&datetime="+mydateStr+"";

        try {
            result = request.execute(RequestedUrl, params).get();
            Object json = new JSONTokener(result).nextValue();
            if(json instanceof JSONObject) {
                jsonObject = new JSONObject(result);
                System.out.println(jsonObject);
                return jsonObject;
            }
            else if (json instanceof JSONArray) {
                return null;
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return null;
    }


}
