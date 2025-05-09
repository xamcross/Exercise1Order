package xam.cross.order.response;

import java.util.ArrayList;
import java.util.List;

public class OrderCreatedResponse
{
    private String id;
    private List<OrderedProduct> orderedProducts = new ArrayList<>();

    public List<OrderedProduct> getOrders() {
        return orderedProducts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
