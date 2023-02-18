package com.marketcollection.domain.order.service;

import com.marketcollection.domain.cart.repository.CartItemRepository;
import com.marketcollection.domain.cart.service.CartService;
import com.marketcollection.domain.item.Item;
import com.marketcollection.domain.item.ItemImage;
import com.marketcollection.domain.item.repository.ItemImageRepository;
import com.marketcollection.domain.item.repository.ItemRepository;
import com.marketcollection.domain.member.Member;
import com.marketcollection.domain.member.repository.MemberRepository;
import com.marketcollection.domain.order.Order;
import com.marketcollection.domain.order.OrderItem;
import com.marketcollection.domain.order.dto.*;
import com.marketcollection.domain.order.repository.OrderItemRepository;
import com.marketcollection.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class OrderService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final CartService cartService;
    private final ItemImageRepository itemImageRepository;

    public OrderDto setOrderInfo(String memberId, OrderRequestDto orderRequestDto) {
        OrderDto orderDto = new OrderDto();
        Member member = memberRepository.findByEmail(memberId).orElseThrow(EntityNotFoundException::new);
        orderDto.setMemberInfo(member);

        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        List<OrderItemRequestDto> orderItemRequestDtos = orderRequestDto.getOrderItemRequestDtos();
        for (OrderItemRequestDto orderItemRequestDto : orderItemRequestDtos) {
            Item item = itemRepository.findById(orderItemRequestDto.getItemId()).orElseThrow(EntityNotFoundException::new);

            OrderItemDto orderItemDto = new OrderItemDto(item.getId(), item.getItemName(), item.getSalePrice(),
                    orderItemRequestDto.getCount());
            orderItemDtos.add(orderItemDto);
        }
            orderDto.setOrderItemDtos(orderItemDtos);

        return orderDto;
    }

    public Long order(String memberId, OrderDto orderDto) {
        Member member = memberRepository.findByEmail(memberId).orElseThrow(EntityNotFoundException::new);
        member.updateOrderInfo(orderDto.getPhoneNumber(), orderDto.getZipCode(), orderDto.getAddress(), orderDto.getDetailAddress());

        List<OrderItemDto> orderItemDtos = orderDto.getOrderItemDtos();
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDto orderItemDto : orderItemDtos) {
            Item item = itemRepository.findById(orderItemDto.getItemId()).orElseThrow(EntityNotFoundException::new);
            OrderItem orderItem = OrderItem.createOrderItem(item, orderItemDto.getCount());

            orderItems.add(orderItem);
        }

        Order order = Order.createOrder(member, orderItems);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
        }
        orderRepository.save(order);

        cartService.deleteCartItemsAfterOrder(member.getId(), orderItems);
        return order.getId();
    }

    public Page<OrderHistoryDto> getOrderHistory(String memberId, OrderSearchDto orderSearchDto, Pageable pageable) {
        Member member = memberRepository.findByEmail(memberId).orElseThrow(EntityNotFoundException::new);
        List<Order> orders = orderRepository.findOrders(member.getId(), orderSearchDto);
        Long total = orderRepository.countOrders(member.getId(), orderSearchDto);

        List<OrderHistoryDto> orderHistoryDtos = new ArrayList<>();
        for (Order order : orders) {
            OrderHistoryDto orderHistoryDto = new OrderHistoryDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                ItemImage itemImage = itemImageRepository.findByItemIdAndRepImageIsTrue(orderItem.getItem().getId(), true);
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImage.getItemImageUrl());
                orderHistoryDto.setOrderItemDtos(List.of(orderItemDto));
            }
            orderHistoryDtos.add(orderHistoryDto);
        }
        return new PageImpl<OrderHistoryDto>(orderHistoryDtos, pageable, total);
    }
}