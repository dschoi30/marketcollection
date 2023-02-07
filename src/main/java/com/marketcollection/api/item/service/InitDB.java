package com.marketcollection.api.item.service;

import com.marketcollection.domain.item.Item;
import com.marketcollection.domain.item.ItemSaleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@RequiredArgsConstructor
@Component
public class InitDB {

    private final InitService initService;

    @PostConstruct
    public void init() {
//        for(int i = 0; i < 100; i++) {
            initService.dbInit1();
//        }
    }

    @RequiredArgsConstructor
    @Transactional
    @Component
    public static class InitService {
        private final EntityManager em;

        public void dbInit1() {

            for(int i = 0; i < 100; i++) {
                Item item = Item.builder()
                        .itemName("강아지 장난감_" + i)
                        .originalPrice(10000)
                        .salePrice(10000 - i * 100)
                        .stockQuantity(1000)
                        .description("너무 좋아요")
                        .thumbnailImageFile("/images/items/dogtoy3.png")
                        .categoryId(1L)
                        .itemSaleStatus(ItemSaleStatus.ON_SALE)
                        .build();
                em.persist(item);
            }
        }
    }
}