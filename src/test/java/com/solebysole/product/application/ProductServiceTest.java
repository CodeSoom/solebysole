package com.solebysole.product.application;

import com.solebysole.product.domain.Category;
import com.solebysole.product.domain.Image;
import com.solebysole.product.domain.Keyword;
import com.solebysole.product.domain.Product;
import com.solebysole.product.domain.ProductRepository;
import com.solebysole.product.dto.ProductCreateData;
import com.solebysole.product.dto.ProductDetailData;
import com.solebysole.common.errors.ProductNameDuplicationException;
import com.solebysole.common.errors.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("ProductService 클래스")
class ProductServiceTest {

    private ProductService productService;

    private ProductRepository productRepository = mock(ProductRepository.class);

    private List<Product> products;
    private Product product1;
    private Product product2;

    private ProductCreateData productCreateData;
    private ProductCreateData duplicatedProductCreateData;

    private final Long existingId = 1L;
    private final Long notExistingId = 9999L;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);

        product1 = createProduct("상품1");
        product2 = createProduct("상품2");

        productCreateData = createProductCreateData("만두 지갑");
        duplicatedProductCreateData = createProductCreateData("만두 지갑");
    }

    @Nested
    @DisplayName("getProducts")
    class Describe_getProducts {
        @Nested
        @DisplayName("저장된 상품이 여러개 있다면")
        class Context_with_products {
            @BeforeEach
            void setUp() {
                products = List.of(product1, product2);

                given(productRepository.findAll())
                        .willReturn(products);
            }

            @Test
            @DisplayName("모든 상품 목록을 리턴한다.")
            void it_returns_all_product_list() {
                assertThat(productService.getProducts()).hasSize(2);
            }
        }

        @Nested
        @DisplayName("저장된 상품이 없다면")
        class Context_without_products {
            @BeforeEach
            void setUp() {
                given(productRepository.findAll())
                        .willReturn(List.of());
            }

            @Test
            @DisplayName("비어있는 상품 목록을 리턴한다.")
            void it_returns_empty_product_list() {
                assertThat(productService.getProducts()).hasSize(0);
            }
        }
    }

    @Nested
    @DisplayName("getProduct")
    class Describe_getProduct {
        @Nested
        @DisplayName("존재하는 상품 id가 주어진다면")
        class Context_with_existing_product_id {
            @BeforeEach
            void setUp() {
                given(productRepository.findById(existingId))
                        .willReturn(Optional.of(product1));
            }

            @Test
            @DisplayName("찾은 상품을 리턴한다.")
            void it_returns_the_found_product() {
                ProductDetailData productDetailData = productService.getProduct(existingId);

                assertAll(
                        () -> assertThat(productDetailData.getName()).isEqualTo(
                                product1.getName()),
                        () -> assertThat(productDetailData.getOriginalPrice()).isEqualTo(
                                product1.getOriginalPrice()),
                        () -> assertThat(productDetailData.getDiscountedPrice()).isEqualTo(
                                product1.getDiscountedPrice()),
                        () -> assertThat(productDetailData.getDescription()).isEqualTo(
                                product1.getDescription()),
                        () -> assertThat(productDetailData.getCategory()).isEqualTo(
                                product1.getCategory()),
                        () -> assertThat(productDetailData.getKeywords()).isEqualTo(
                                product1.getKeywords()),
                        () -> assertThat(productDetailData.getImages()).isEqualTo(
                                product1.getImages()),
                        () -> assertThat(productDetailData.getOptions()).isEqualTo(
                                product1.getOptions())
                );
            }
        }

        @Nested
        @DisplayName("존재하지 않는 상품 id가 주어진다면")
        class Context_with_not_existing_product_id {
            @BeforeEach
            void setUp() {
                given(productRepository.findById(notExistingId))
                        .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("'상품을 찾을 수 없습니다.' 라는 예외가 발생한다.")
            void it_throws_exception() {
                assertThrows(ProductNotFoundException.class,
                        () -> productService.getProduct(notExistingId));
            }
        }
    }

    @Nested
    @DisplayName("createProduct")
    class Describe_createProduct {
        @Nested
        @DisplayName("올바른 상품 정보가 주어진다면")
        class Context_with_product {
            @BeforeEach
            void setUp() {
                given(productRepository.save(any(Product.class)))
                        .will(invocation -> invocation.<Product>getArgument(0));
            }

            @Test
            @DisplayName("상품을 생성한다.")
            void it_create_product() {
                productService.createProduct(productCreateData);

                verify(productRepository).save(any(Product.class));
            }
        }

        @Nested
        @DisplayName("중복된 상품 이름이 주어진다면")
        class Context_with_duplicated_product_name {
            @BeforeEach
            void setUp() {
                given(productRepository.existsByName(duplicatedProductCreateData.getName()))
                        .willReturn(true);
            }

            @Test
            @DisplayName("'중복된 상품 이름입니다.' 라는 예외가 발생한다.")
            void it_throws_exception() {
                assertThrows(ProductNameDuplicationException.class,
                        () -> productService.createProduct(productCreateData));
            }
        }
    }

    private ProductCreateData createProductCreateData(String name) {
        Set<Keyword> keywords = new HashSet<>(
                Arrays.asList(new Keyword("가죽"), new Keyword("지갑")));

        List<Image> images = Arrays.asList(
                new Image("url1"),
                new Image("url2"));

        ProductCreateData productCreateData = ProductCreateData.builder()
                .name(name)
                .originalPrice(50000)
                .discountedPrice(4000)
                .description("가죽 지갑입니다.")
                .category(Category.WALLET)
                .keywords(keywords)
                .images(images)
                .options(new ArrayList<>())
                .build();

        return productCreateData;
    }

    private Product createProduct(String name) {
        Product product = Product.builder()
                .name(name)
                .originalPrice(50000)
                .discountedPrice(40000)
                .description("가죽 지갑입니다.")
                .category(Category.WALLET)
                .build();

        product.addImage(new Image("url1"));

        return product;
    }

}
