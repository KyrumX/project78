package team.smartwaiter.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ApiController {

    private final String DEFAULT_URL = "http://86.82.103.122:8080";

    public void Print() {

        //Ralph, please take a look at my trial code here:
        HashMap<Integer, String> hm = Serializer.menuItems(getMenu()); // <-- Link the menu item names with ids
        System.out.println(hm);
        int keyV = 0;
        for (int key : hm.keySet()) {
            if (hm.get(key).equals("fries")) { // <-- Instead of fries you should do the menu item input you've received
                keyV = key;
            }

        }

        //Here you should check wether keyV is 0, if it is the product has not been found, it should not happen because Selims code checks wether an item exists before going on

        JSONObject o = getMenuItemDetails(keyV); // <-- Pull the JSON data from the database
        HashMap hm2 = Serializer.menuItemInformation(o);// <-- Transfrom the JSON to usefull data, a hashmap in this case

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
                System.out.println("PRINTING JSON ARRAY--*");
                System.out.println(jsonArray);
                return jsonArray;
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getMenuItemDetails(int id) {
        String RequestedUrl = DEFAULT_URL + "/api/menu/" + id;
        String result;
        JSONObject jsonObject;
        HttpRequest request = new HttpGetRequest();

        try {
            result = request.execute(RequestedUrl).get();
            Object json = new JSONTokener(result).nextValue();
            if(json instanceof JSONObject) {
                jsonObject = new JSONObject(result);
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
  
    public JSONObject postOrder() {
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

    public JSONObject postOrderLine(int amount, int id, int currentOrderID) {
        String RequestedUrl = DEFAULT_URL + "/api/orderlines/";
        String result;
        String params;
        JSONObject jsonObject;
        HttpRequest request = new HttpPostRequest();

        //Create parameters:
        params = "amount=" + amount + "&menuitem=" + id + "&orderid=" + currentOrderID;

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

    public JSONObject getOrderTotal(int currentOrderID) {
        String RequestedUrl = DEFAULT_URL + "/api/orders/sum/";
        String result;
        JSONObject jsonObject;
        HttpRequest request = new HttpGetRequest();

        RequestedUrl = RequestedUrl += currentOrderID;

        try {
            result = request.execute(RequestedUrl).get();
            Object json = new JSONTokener(result).nextValue();
            if(json instanceof JSONObject) {
                jsonObject = new JSONObject(result);
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

    public JSONArray getItemsThatGoWellWith(int itemID) {
        String RequestedUrl = DEFAULT_URL + "/api/goeswellwith/";
        String result;
        JSONArray jsonArray;
        HttpRequest request = new HttpGetRequest();

        RequestedUrl = RequestedUrl += itemID;

        try {
            result = request.execute(RequestedUrl).get();
            Object json = new JSONTokener(result).nextValue();
            if(json instanceof JSONArray) {
                jsonArray = new JSONArray(result);
                return jsonArray;
            }
            else if (json instanceof JSONObject) {
                return null;
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return null;
    }


}
