package xam.cross.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import xam.cross.order.dto.ProductDto;
import xam.cross.order.entity.OrderEntity;
import xam.cross.order.entity.OrderStatus;
import xam.cross.order.repository.OrderRepository;
import xam.cross.order.request.NewOrderRequest;
import xam.cross.order.response.GetOrderResponse;
import xam.cross.order.response.OrderedProduct;
import xam.cross.order.response.OrderCreatedResponse;
import xam.cross.order.response.ProcessOrderResponse;
import xam.cross.order.service.KafkaEventProducer;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaEventProducer kafkaEventProducer;

    @PostMapping
    @Transactional
    public OrderCreatedResponse createNewOrder(@RequestBody final NewOrderRequest newOrderRequest) {
        final OrderCreatedResponse orderCreatedResponse = new OrderCreatedResponse();
        System.out.println("NewOrderRequest: " + newOrderRequest);
        newOrderRequest.getProductQuantityMapping().forEach((productId, productQuantity) -> {
            ProductDto product = restTemplate.getForObject("http://host.docker.internal:8000/product/" + productId, ProductDto.class);
            System.out.println("product: " + product);
            final OrderedProduct newOrderedProduct = new OrderedProduct();

            if (product != null && product.getInventory() >= productQuantity) {
                newOrderedProduct.setId(productId);
                newOrderedProduct.setQuantity(productQuantity);
                newOrderedProduct.setInitialised(true);
                orderCreatedResponse.getOrders().add(newOrderedProduct);
            } else if (product == null) {
                newOrderedProduct.setId(productId);
                newOrderedProduct.setInitialised(false);
                newOrderedProduct.setQuantity(0);
                newOrderedProduct.setMessage("Product not available anymore");
                orderCreatedResponse.getOrders().add(newOrderedProduct);
            } else if (product.getInventory() < productQuantity) {
                newOrderedProduct.setId(productId);
                newOrderedProduct.setQuantity(0);
                newOrderedProduct.setInitialised(false);
                newOrderedProduct.setMessage("Insufficient inventory, only have " + product.getInventory() + " products left. Please, update your order");
                orderCreatedResponse.getOrders().add(newOrderedProduct);
            }
        });
        final OrderEntity newOrderEntity = new OrderEntity();
        newOrderEntity.setCreatedAt(LocalDateTime.now());
        newOrderEntity.setUpdatedAt(LocalDateTime.now());
        newOrderEntity.setStatus(OrderStatus.CREATED);
        newOrderEntity.getItems().putAll(
                orderCreatedResponse.getOrders().stream().collect(
                        Collectors.toMap(OrderedProduct::getId, OrderedProduct::getQuantity)
                ));
        orderRepository.save(newOrderEntity);
        kafkaEventProducer.sendEvent(newOrderEntity.getId(), "ORDER_CREATED");
        orderCreatedResponse.setId(newOrderEntity.getId());
        return orderCreatedResponse;
    }

    @GetMapping("/{id}")
    public GetOrderResponse getOrder(@PathVariable("id") String id) {
        Optional<OrderEntity> orderOptional = orderRepository.findById(id);
        if (orderOptional.isPresent()) {
            final OrderEntity orderEntity = orderOptional.get();
            final GetOrderResponse getOrderResponse = new GetOrderResponse();
            getOrderResponse.setOrderId(orderEntity.getId());
            getOrderResponse.getOrderedProducts().putAll(orderEntity.getItems());
            getOrderResponse.setStatus(orderEntity.getStatus());
            return getOrderResponse;
        }
        final GetOrderResponse getOrderResponseError = new GetOrderResponse();
        getOrderResponseError.setOrderId(id);
        getOrderResponseError.setMessage("Order not found");
        return getOrderResponseError;
    }

    @PatchMapping("/{id}")
    @Transactional
    public ProcessOrderResponse processOrder(@PathVariable("id") String id) {
        final ProcessOrderResponse processOrderResponse = new ProcessOrderResponse();
        final Optional<OrderEntity> orderOptional = orderRepository.findById(id);
        if (orderOptional.isPresent()) {
            final OrderEntity orderEntity = orderOptional.get();
            if (orderEntity.getStatus() == OrderStatus.CREATED) {
                orderEntity.setStatus(OrderStatus.PROCESSING);
                processOrderResponse.setId(orderEntity.getId());
                processOrderResponse.setStatus(OrderStatus.PROCESSING);
                orderRepository.save(orderEntity);
                kafkaEventProducer.sendEvent(orderEntity.getId(), "ORDER_PROCESSING");
            } else {
                processOrderResponse.setId(orderEntity.getId());
                processOrderResponse.setMessage("Order already processed");
            }
        } else {
            processOrderResponse.setId(id);
            processOrderResponse.setMessage("Order not found");
        }
        return processOrderResponse;
    }
}
