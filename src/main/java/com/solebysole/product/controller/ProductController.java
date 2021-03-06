package com.solebysole.product.controller;

import com.solebysole.authentication.CurrentUser;
import com.solebysole.product.application.ProductService;
import com.solebysole.product.dto.ProductCreateData;
import com.solebysole.product.dto.ProductData;
import com.solebysole.product.dto.ProductDetailData;
import com.solebysole.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

/**
 * 상품과 관련된 HTTP 요청 처리를 담당합니다.
 */
@CrossOrigin
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 모든 상품을 응답합니다.
     */
    @GetMapping
    public ResponseEntity<List<ProductData>> list() {
        return ResponseEntity.ok(productService.getProducts());
    }

    /**
     * 주어진 id에 해당하는 상품 상세 정보를 응답합니다.
     *
     * @param id 상품의 식별자
     * @return 주어진 id에 해당하는 상품 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailData> findOne(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    /**
     * 주어진 상품 정보로 상품을 생성합니다.
     *
     * @param user 현재 회원
     * @param productCreateData 상품 정보
     * @return 응답 정보
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Void> create(
            @CurrentUser User user,
            @RequestBody @Valid ProductCreateData productCreateData) {
        Long savedId = productService.createProduct(productCreateData);
        return ResponseEntity.created(URI.create("/api/products/" + savedId)).build();
    }

}
