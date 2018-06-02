package team.smartwaiter.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpPostRequest extends HttpRequest {
    protected static final String REQUEST_METHOD = "POST";

    @Override
    protected String doInBackground(String... strings) {
        String stringUrl = strings[0];
        String params = strings[1];
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

            connection.setDoOutput(true);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(params);
            outputStream.flush();
            outputStream.close();


            InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());

            BufferedReader in = new BufferedReader(streamReader);
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
