package com.solebysole.cart.infra;

import com.solebysole.cart.domain.CartProduct;
import com.solebysole.cart.domain.CartProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA 장바구니 상품 저장소.
 */
public interface JpaCartProductRepository
        extends CartProductRepository, JpaRepository<CartProduct, Long> {

    List<CartProduct> findAllByUserId(Long userId);

    CartProduct save(CartProduct cartProduct);

}
