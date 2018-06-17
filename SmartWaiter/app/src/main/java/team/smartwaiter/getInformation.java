package team.smartwaiter;

import org.json.JSONObject;

import java.util.HashMap;
import team.smartwaiter.api.Serializer;
import team.smartwaiter.api.ApiController;


public class getInformation {
    static ApiController apiController = new ApiController();

    public static Integer getData(String input) {
        HashMap<Integer, String> hm = Serializer.MenuItems(apiController.getMenu()); // <-- Link the menu item names with ids
        System.out.println(hm);
        int keyV = 0;
        for (int key : hm.keySet()) {
            if (hm.get(key).equals(input)) { // <-- Instead of fries you should do the menu item input you've received
                keyV = key;
            }

        }
        return keyV;
    }

    public static String showInformation(String input, String n) {
        JSONObject o = apiController.getMenuItemDetails(getData(input));
        System.out.println(o);
        HashMap hm2 = Serializer.menuItemInformation(o);
        System.out.println(hm2);
        String value = hm2.get(n).toString();
        return value;
    }

}
