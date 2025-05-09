package xam.cross.order.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import xam.cross.order.entity.OrderEntity;

public interface OrderRepository extends MongoRepository<OrderEntity, String> {
}
