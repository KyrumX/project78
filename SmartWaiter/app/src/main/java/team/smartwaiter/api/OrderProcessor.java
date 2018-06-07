package team.smartwaiter.api;

import java.util.HashMap;
import java.util.Hashtable;

public class OrderProcessor {
    private ApiController controller;

    public OrderProcessor() {
        this.controller = new ApiController();
    }

    public void createNewOrder() {

    }

    public void createNewOrderLine(HashMap<String, Integer> hashMap) {
        for (String key : hashMap.keySet()) {
            int menuItemID = linkNameWithID(key);

            //Check is 0, if so, the name was not found and we should reject the orderline for now.
            if (menuItemID != 0)
                controller.postOrderLine(hashMap.get(key), menuItemID, 0);
        }
    }

    private int linkNameWithID(String name) {
        HashMap<Integer, String> hm = Serializer.MenuItems(controller.getMenu());
        int keyFound = 0;
        for (int key : hm.keySet()) {
            if (hm.get(key).equals(name)) {
                keyFound = key;
            }

        }
        return keyFound;
    }
}
