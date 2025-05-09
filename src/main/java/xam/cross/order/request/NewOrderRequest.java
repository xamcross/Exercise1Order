package xam.cross.order.request;

import java.util.HashMap;
import java.util.Map;

public class NewOrderRequest {

    private Map<String, Integer> productQuantityMapping = new HashMap<>();

    public NewOrderRequest() {
    }

    public Map<String, Integer> getProductQuantityMapping() {
        return productQuantityMapping;
    }

    public void setProductQuantityMapping(Map<String, Integer> productQuantityMapping) {
        this.productQuantityMapping = productQuantityMapping;
    }

    @Override
    public String toString() {
        return "NewOrderRequest{" +
                "productQuantityMapping=" + productQuantityMapping.toString() +
                '}';
    }
}
