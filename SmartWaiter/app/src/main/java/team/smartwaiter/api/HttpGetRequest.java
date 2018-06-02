package team.smartwaiter.api;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetRequest extends HttpRequest {
    protected static final String REQUEST_METHOD = "GET";

    @Override
    protected String doInBackground(String... strings) {
        String stringUrl = strings[0];
        String result;
        String inputLine;

        try {
            URL url = new URL(stringUrl);

            HttpURLConnection connection =(HttpURLConnection) url.openConnection();

            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setRequestProperty("User-Agent", REQUEST_AGENT);
            connection.setRequestProperty("Accept", REQUEST_ACCEPT);

            connection.connect();

            InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            streamReader.close();

            result = response.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            result = null;
        }

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
