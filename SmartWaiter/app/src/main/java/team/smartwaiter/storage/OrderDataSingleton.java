package team.smartwaiter.storage;

import team.smartwaiter.api.ApiController;
import team.smartwaiter.api.Serializer;

public class OrderDataSingleton {
    private static OrderDataSingleton single_instance = new OrderDataSingleton();

    private boolean constructedOrder = false;
    private int orderID;

    private static ApiController controller = new ApiController();

    private OrderDataSingleton() {
    }

    public static OrderDataSingleton getInstance() {
        return single_instance;
    }

    public void setConstructedOrder(boolean constructedOrder) {
        constructedOrder = constructedOrder;
    }

    public int getOrderID() {

        return orderID;
    }

    public void receiveOrderID() {
        orderID = Serializer.OrderID(controller.postOrder());

        constructedOrder = true;
    }

    public void update() {
        if (!constructedOrder) {
            receiveOrderID();
        }
        System.out.println(orderID);
    }
}
