package xam.cross.order.response;

import xam.cross.order.entity.OrderStatus;

import java.util.HashMap;
import java.util.Map;

public class GetOrderResponse {
    private String orderId;
    private OrderStatus status;
    private String message;
    private Map<String, Integer> orderedProducts = new HashMap<>();

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Integer> getOrderedProducts() {
        return orderedProducts;
    }
}
