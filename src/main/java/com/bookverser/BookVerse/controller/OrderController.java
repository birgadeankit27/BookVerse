
package com.bookverser.BookVerse.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookverser.BookVerse.dto.AdminOrderResponseDto;
import com.bookverser.BookVerse.dto.BulkOrderStatusUpdateRequest;
import com.bookverser.BookVerse.dto.OrderDTO;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.OrderSummaryDto;
import com.bookverser.BookVerse.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // =========================================================
    // CUSTOMER: GET ORDER BY ID
    // =========================================================

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long orderId) {

        OrderResponseDto response =
                orderService.getOrderById(orderId);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // CUSTOMER: GET MY ORDERS
    // =========================================================

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException(
                    "Unauthorized: Please login");
        }

        List<OrderResponseDto> orders =
                orderService.getMyOrders(
                        userDetails.getUsername());

        return ResponseEntity.ok(orders);
    }

    // =========================================================
    // CUSTOMER: REQUEST RETURN
    // =========================================================

    @PatchMapping("/{orderId}/return")
    public ResponseEntity<OrderResponseDto> requestReturn(
            @PathVariable Long orderId) {

        OrderResponseDto response =
                orderService.requestReturn(orderId);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // CUSTOMER / ADMIN: CANCEL ORDER
    // =========================================================

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {

        OrderDTO updatedOrder =
                orderService.cancelOrder(
                        orderId,
                        userId,
                        isAdmin);

        return ResponseEntity.ok(updatedOrder);
    }

    // =========================================================
    // ADMIN: GET ORDER BY ID
    // =========================================================

    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOrderResponseDto> getOrderByIdForAdmin(
            @PathVariable Long orderId) {

        AdminOrderResponseDto response =
                orderService.getOrderByAdminId(orderId);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ADMIN: GET ALL ORDERS
    // =========================================================

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderSummaryDto>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        LocalDate from =
                fromDate != null
                        ? LocalDate.parse(fromDate)
                        : null;

        LocalDate to =
                toDate != null
                        ? LocalDate.parse(toDate)
                        : null;

        Page<OrderSummaryDto> orders =
                orderService.getAllOrders(
                        status,
                        from,
                        to,
                        customerId,
                        pageable);

        return ResponseEntity.ok(orders);
    }

    // =========================================================
    // ADMIN: UPDATE SINGLE ORDER STATUS
    // =========================================================

    @PatchMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");

        OrderDTO response =
                orderService.updateOrderStatus(
                        orderId,
                        status);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ADMIN: BULK UPDATE ORDER STATUS
    // =========================================================

    @PatchMapping("/admin/orders/status/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponseDto>> bulkUpdateOrderStatus(
            @Valid @RequestBody BulkOrderStatusUpdateRequest request) {

        List<OrderResponseDto> response =
                orderService.bulkUpdateOrderStatus(request);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ADMIN: GENERATE INVOICE PDF
    // =========================================================

    @GetMapping("/{orderId}/invoice")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateInvoicePdf(
            @PathVariable Long orderId) {

        byte[] pdfBytes =
                orderService.generateInvoicePdf(orderId);

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF);

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename("invoice_" + orderId + ".pdf")
                        .build());

        return new ResponseEntity<>(
                pdfBytes,
                headers,
                HttpStatus.OK);
    }
}
