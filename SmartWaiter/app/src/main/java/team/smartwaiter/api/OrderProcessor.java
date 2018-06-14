package team.smartwaiter.api;

import org.json.JSONObject;

import java.util.HashMap;

import team.smartwaiter.storage.OrderDataSingleton;

public class OrderProcessor {
    private ApiController controller;

    public OrderProcessor() {
        this.controller = new ApiController();
    }

    public void createNewOrderLine(HashMap<String, Integer> hashMap) {
        int orderID = OrderDataSingleton.getInstance().getOrderID();

        for (String key : hashMap.keySet()) {
            int menuItemID = linkNameWithID(key);

            //Check is 0, if so, the name was not found and we should reject the orderline for now.
            if (menuItemID != 0)
                controller.postOrderLine(hashMap.get(key), menuItemID, orderID);
        }
    }

    private int linkNameWithID(String name) {
        HashMap<Integer, String> hm = Serializer.menuItems(controller.getMenu());
        int keyFound = 0;
        for (int key : hm.keySet()) {
            if (hm.get(key).equals(name)) {
                keyFound = key;
            }

        }
        return keyFound;
    }

    public double getOrderSum() {
        int orderID = OrderDataSingleton.getInstance().getOrderID();

        JSONObject jsonObject = controller.getOrderTotal(orderID);

        return Serializer.orderSum(jsonObject);
    }

    public HashMap<String, Integer> getOrderLines() {
        int orderID = OrderDataSingleton.getInstance().getOrderID();

        JSONObject jsonObject = controller.getOrderLinesAPI(1);

        return Serializer.orderLines(jsonObject);
    }
}
