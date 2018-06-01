package team.smartwaiter.api;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiController {

    private final String DEFAULT_URL = "http://86.82.103.122:8080";

    public void Print() throws IOException {
        getMenu();
    }

    public String getMenu() throws IOException {
        String RequestedUrl = DEFAULT_URL + "/api/menu/";
        System.out.print(RequestedUrl);

        URL object = new URL(RequestedUrl);
        HttpURLConnection connection = (HttpURLConnection) object.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        //TODO: DO SOMETHING WITH RESPONSE CODE, MAYBE JUST IGNORE BECAUSE HE TOLD US TO SIMULATE IT ANYWAY?

        System.out.println("Sending 'GET' request to URL : " + RequestedUrl);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        try {
            JSONArray result = new JSONArray(response.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response.toString();
    }

}
