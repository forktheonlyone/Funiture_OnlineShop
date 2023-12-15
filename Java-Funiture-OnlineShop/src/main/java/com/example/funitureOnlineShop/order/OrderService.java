package com.example.funitureOnlineShop.order;

import com.example.funitureOnlineShop.cart.Cart;
import com.example.funitureOnlineShop.cart.CartRepository;
import com.example.funitureOnlineShop.core.error.exception.Exception404;
import com.example.funitureOnlineShop.core.error.exception.Exception500;
import com.example.funitureOnlineShop.order.orderstatus.OrderStatus;
import com.example.funitureOnlineShop.order.orderstatus.OrderStatusRepository;
import com.example.funitureOnlineShop.order.orderstatus.OrderStatusDTO;
import com.example.funitureOnlineShop.user.User;
import com.example.funitureOnlineShop.order.item.Item;
import com.example.funitureOnlineShop.order.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    private final OrderStatusRepository orderStatusRepository;
    // 결제 시도시 작동
    @Transactional
    public OrderResponse.FindByIdDTO save(User user) {
        //장바구니 조회
        List<Cart> cartList = cartRepository.findAllByUserId(user.getId());

        if(cartList.isEmpty()){
            throw new Exception404("장바구니에 상품 내역이 존재하지 않습니다.");
        }

        // 주문 생성
        Order order = Order.builder().user(user).build();
        order = orderRepository.save(order);

        // OrderStatus 생성 및 주문 연결


        // 아이템 저장
        List<Item> itemList = new ArrayList<>();
        for(Cart cart : cartList){
            Item item = Item.builder()
                    .option(cart.getOption())
                    .order(order)
                    .quantity(cart.getQuantity())
                    .price(cart.getOption().getPrice() * cart.getQuantity())
                    .build();

            itemList.add(item);
        }
        OrderStatus orderStatus = new OrderStatus(order, false); // 주문 상태 초기값: false
        orderStatusRepository.save(orderStatus);

        try{
            itemRepository.saveAll(itemList);
            OrderStatusDTO.OrderStatusUpdateRequest UpdateRequest = new OrderStatusDTO.OrderStatusUpdateRequest(order.getId(), true);
            // 주문 완료 시 OrderStatus 값을 변경하여 주문 상태를 완료로 설정
            orderStatusRepository.save(orderStatus);
        } catch (Exception e){
            throw new Exception500("주문 생성중 오류가 발생하였습니다.");
        }
        return new OrderResponse.FindByIdDTO(order, itemList);
    }


    public OrderResponse.FindByIdDTO findById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(
            () -> new Exception404("해당주문 내역을 찾을 수 없습니다."+ id));

        List<Item> itemList = itemRepository.findAllByOrderId(id);
        return new OrderResponse.FindByIdDTO(order,itemList);
    }

    // 주문 상태를 업데이트하는 메소드
    private void updateOrderStatus(OrderStatusDTO.OrderStatusUpdateRequest updateRequest) {
        Long orderId = updateRequest.getOrderId();
        boolean isOrdered = updateRequest.isOrdered();

        // 주문 ID로 주문 상태를 찾습니다.
        OrderStatus orderStatus = orderStatusRepository.findByOrderId(orderId)
                .orElseThrow(() -> new Exception404("주문 상태를 찾을 수 없습니다. 주문 ID: " + orderId));

        // 주문 상태를 업데이트합니다.
        orderStatus.setOrdered(isOrdered);
        orderStatusRepository.save(orderStatus);
    }

    @Transactional
    public void delete(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new Exception404("주문을 찾을 수 없습니다."));

        List<Item> itemsToDelete = itemRepository.findAllByOrderId(orderId);

        try {
            itemRepository.deleteAll(itemsToDelete);
            orderRepository.delete(order);
        } catch (Exception e) {
            throw new Exception500("주문 및 주문 항목 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
