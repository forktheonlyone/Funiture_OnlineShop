package com.example.funitureOnlineShop.order;

import com.example.funitureOnlineShop.core.security.CustomUserDetails;
import com.example.funitureOnlineShop.core.utils.ApiUtils;
import com.example.funitureOnlineShop.orderCheck.OrderCheckDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders/save")
    public ResponseEntity<?> save (@AuthenticationPrincipal CustomUserDetails customUserDetails){
        OrderResponse.FindByIdDTO findByIdDTO = orderService.save(customUserDetails.getUser());
        return ResponseEntity.ok(ApiUtils.success(findByIdDTO));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        OrderResponse.FindByIdDTO findByIdDTO = orderService.findById(id);

        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(findByIdDTO);
        return ResponseEntity.ok(ApiUtils.success(apiResult));
    }

    @PostMapping("/orders/delete")
    public ResponseEntity<?> delete(Long orderId){
        orderService.delete(orderId);
        ApiUtils.ApiResult<?> apiResult = ApiUtils.success(null);
        return ResponseEntity.ok(ApiUtils.success(apiResult));
    }

    @GetMapping("/orders/ordercheck/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        OrderCheckDto orderCheckDtos =orderService.findOrderChecks(id);

        return ResponseEntity.ok(orderCheckDtos);
    }

    @PostMapping("/orders/{id}/return")
    public ResponseEntity<String> processReturn(@PathVariable Long id) {
        orderService.findById(id);
        orderService.processReturn(id);
        return ResponseEntity.ok("반품 처리가 완료되었습니다.");
    }
}
