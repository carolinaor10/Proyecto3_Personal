package com.project.demo.rest.order;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.HttpResponse;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.order.Order;
import com.project.demo.logic.entity.order.OrderRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderRestController {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    // /orders/user/{userId}
    // /orders/user/20

    // /orders/user/1/orders?page=1&size=10
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllByUser (@PathVariable Long userId,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           HttpServletRequest request) {
        Optional<User> foundUser = userRepository.findById(userId);
        if(foundUser.isPresent()) {


            Pageable pageable = PageRequest.of(page, size);
            Page<Order> ordersPage = orderRepository.getOrderByUserId(userId, pageable);

            return new GlobalResponseHandler().handleResponse("Order created successfully",
                    ordersPage.getContent(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> addOrderToUser(@PathVariable Long userId, @RequestBody Order order, HttpServletRequest request) {
        Optional<User> foundUser = userRepository.findById(userId);
        if(foundUser.isPresent()) {
            order.setUser(foundUser.get());
            Order savedOrder = orderRepository.save(order);
            return new GlobalResponseHandler().handleResponse("Order created successfully",
                    savedOrder, HttpStatus.CREATED, request);
        } else {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(@PathVariable Long orderId, @RequestBody Order order, HttpServletRequest request) {
        Optional<Order> foundOrder = orderRepository.findById(orderId);
        if(foundOrder.isPresent()) {
            order.setId(foundOrder.get().getId());
            order.setUser(foundOrder.get().getUser());
            orderRepository.save(order);
            return new GlobalResponseHandler().handleResponse("Order updated successfully",
                    order, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Order id " + orderId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<?> pathOrder(@PathVariable Long orderId, @RequestBody Order order, HttpServletRequest request) {
        Optional<Order> foundOrder = orderRepository.findById(orderId);
        if(foundOrder.isPresent()) {
            if(order.getTotal() != null) foundOrder.get().setTotal(order.getTotal());
            if(order.getDescription() != null) foundOrder.get().setDescription(order.getDescription());
            orderRepository.save(foundOrder.get());
            return new GlobalResponseHandler().handleResponse("Order updated successfully",
                    foundOrder.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Order id " + orderId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Optional<Order> foundOrder = orderRepository.findById(orderId);
        if(foundOrder.isPresent()) {
            Optional<User> user = userRepository.findById(foundOrder.get().getUser().getId());
            user.get().getOrders().remove(foundOrder.get());
            orderRepository.deleteById(foundOrder.get().getId());
            return new GlobalResponseHandler().handleResponse("Order deleted successfully",
                    foundOrder.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Order id " + orderId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

}
