package com.solebysole.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solebysole.authentication.service.AuthenticationService;
import com.solebysole.common.RestDocsConfiguration;
import com.solebysole.common.errors.ProductNameDuplicationException;
import com.solebysole.common.errors.ProductNotFoundException;
import com.solebysole.docs.ProductDocumentation;
import com.solebysole.product.application.ProductService;
import com.solebysole.product.domain.Category;
import com.solebysole.product.domain.Image;
import com.solebysole.product.domain.Keyword;
import com.solebysole.product.domain.Option;
import com.solebysole.product.dto.ProductCreateData;
import com.solebysole.product.dto.ProductData;
import com.solebysole.product.dto.ProductDetailData;
import com.solebysole.user.domain.Role;
import com.solebysole.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ProductController ?????????")
@Import(RestDocsConfiguration.class)
@AutoConfigureRestDocs
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private AuthenticationService authenticationService;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJ1c2VySWQiOjF9.ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaDk";

    private final Long savedId = 1L;
    private final Long existingId = 1L;
    private final Long notExistingId = 9999L;

    private final Long existingUserId = 1L;

    private List<ProductData> productDataList;
    private ProductData productData1;
    private ProductData productData2;

    private ProductCreateData productCreateData;
    private ProductCreateData invalidProductCreateData;
    private ProductCreateData duplicatedProductCreateData;

    private ProductDetailData productDetailData;

    private User user;
    private User adminUser;

    @BeforeEach
    void setUp() {
        productData1 = createProductData(1L, "??????1");
        productData2 = createProductData(2L, "??????2");

        productCreateData = createProductCreateData("?????? ??????");
        invalidProductCreateData = createProductCreateData("");
        duplicatedProductCreateData = createProductCreateData("?????? ??????");

        productDetailData = createProductDetailData(1L, "?????? ??????");

        user = User.builder()
                .id(existingUserId)
                .email("test@test.com")
                .password("1234abcd")
                .role(Role.ROLE_USER)
                .build();

        adminUser = User.builder()
                .id(existingUserId)
                .email("test@test.com")
                .password("1234abcd")
                .role(Role.ROLE_ADMIN)
                .build();

        given(authenticationService.parseToken(VALID_TOKEN))
                .willReturn(existingUserId);
        given(authenticationService.loadUserById(existingUserId))
                .willReturn(adminUser);
    }

    @Nested
    @DisplayName("GET ?????????")
    class Describe_GET {
        @Nested
        @DisplayName("????????? ????????? ????????? ?????????")
        class Context_with_products {
            @BeforeEach
            void setUp() {
                productDataList = List.of(productData1, productData2);

                given(productService.getProducts())
                        .willReturn(productDataList);
            }

            @Test
            @DisplayName("?????? ?????? ????????? ???????????? 200 OK ??? ????????????.")
            void it_responds_all_product_data_list() throws Exception {
                mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(status().isOk())
                        .andDo(ProductDocumentation.getProducts());
            }
        }

        @Nested
        @DisplayName("????????? ????????? ?????????")
        class Context_without_products {
            @BeforeEach
            void setUp() {
                productDataList = List.of();

                given(productService.getProducts())
                        .willReturn(productDataList);
            }

            @Test
            @DisplayName("???????????? ?????? ????????? ???????????? 200 OK ??? ????????????.")
            void it_responds_empty_product_data_list() throws Exception {
                mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$", hasSize(0)))
                        .andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("???????????? ?????? id??? ???????????????")
        class Context_with_existing_product_id {
            @BeforeEach
            void setUp() {
                given(productService.getProduct(existingId))
                        .willReturn(productDetailData);
            }

            @Test
            @DisplayName("?????? ????????? ???????????? 200 OK ??? ????????????.")
            void it_responds_the_found_product_and_status_code_200() throws Exception {
                mockMvc.perform(get("/api/products/{id}", existingId)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("name").exists())
                        .andExpect(jsonPath("originalPrice").exists())
                        .andExpect(jsonPath("discountedPrice").exists())
                        .andExpect(jsonPath("description").exists())
                        .andExpect(jsonPath("category").exists())
                        .andExpect(jsonPath("keywords").exists())
                        .andExpect(jsonPath("images").exists())
                        .andExpect(jsonPath("options").exists())
                        .andExpect(status().isOk())
                        .andDo(ProductDocumentation.getProduct());
            }
        }

        @Nested
        @DisplayName("???????????? ?????? ?????? id??? ???????????????")
        class Context_with_not_existing_product_id {
            @BeforeEach
            void setUp() {
                given(productService.getProduct(notExistingId))
                        .willThrow(new ProductNotFoundException(notExistingId));
            }

            @Test
            @DisplayName("???????????? 404 Not Found ??? ????????????.")
            void it_responds_status_code_404() throws Exception {
                mockMvc.perform(get("/api/products/{id}", notExistingId)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("POST ?????????")
    class Describe_POST {
        @Nested
        @DisplayName("????????? ?????? ????????? ???????????????")
        class Context_with_product_data {
            @BeforeEach
            void setUp() {
                given(productService.createProduct(any(ProductCreateData.class)))
                        .willReturn(savedId);
            }

            @Test
            @DisplayName("???????????? 201 Created ??? ????????????.")
            void it_responds_status_code_201() throws Exception {
                mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(productCreateData)))
                        .andExpect(header().string("location", "/api/products/" + savedId))
                        .andExpect(status().isCreated())
                        .andDo(ProductDocumentation.createProduct());
            }
        }

        @Nested
        @DisplayName("???????????? ?????? ?????? ????????? ???????????????")
        class Context_with_invalid_product_data {
            @Test
            @DisplayName("???????????? 400 Bad Request ??? ????????????.")
            void it_responds_status_code_400() throws Exception {
                mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(invalidProductCreateData)))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("????????? ?????? ????????? ???????????????")
        class Context_with_duplicated_product_data {
            @BeforeEach
            void setUp() {
                given(productService.createProduct(any(ProductCreateData.class)))
                        .willThrow(new ProductNameDuplicationException());
            }

            @Test
            @DisplayName("???????????? 400 Bad Request ??? ????????????.")
            void it_responds_status_code_400() throws Exception {
                mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(duplicatedProductCreateData)))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("???????????? ????????????")
        class Context_with_not_admin_user {
            @BeforeEach
            void setUp() {
                given(authenticationService.loadUserById(existingUserId))
                        .willReturn(user);
            }

            @Test
            @DisplayName("???????????? 403 Forbidden ??? ????????????.")
            void it_responds_status_code_401() throws Exception {
                mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(productCreateData)))
                        .andExpect(status().isForbidden());
            }
        }
    }

    private ProductCreateData createProductCreateData(String name) {
        Set<Keyword> keywords = createKeywords();
        List<Image> images = createImages();
        List<Option> options = createOptions();

        ProductCreateData productCreateData = ProductCreateData.builder()
                .name(name)
                .originalPrice(50000)
                .discountedPrice(4000)
                .description("?????? ???????????????.")
                .category(Category.WALLET)
                .keywords(keywords)
                .images(images)
                .options(options)
                .build();

        return productCreateData;
    }

    private ProductData createProductData(Long id, String name) {
        ProductData productData = ProductData.builder()
                .id(id)
                .name(name)
                .originalPrice(50000)
                .discountedPrice(40000)
                .imageUrl("url")
                .build();

        return productData;
    }

    private ProductDetailData createProductDetailData(Long id, String name) {
        Set<Keyword> keywords = createKeywords();
        List<Image> images = createImages();

        List<Option> options = createOptions();

        productDetailData = ProductDetailData.builder()
                .id(id)
                .name(name)
                .originalPrice(50000)
                .discountedPrice(40000)
                .description("?????? ???????????????.")
                .category(Category.WALLET)
                .keywords(keywords)
                .images(images)
                .options(options)
                .build();

        return productDetailData;
    }

    private HashSet<Keyword> createKeywords() {
        return new HashSet<>(
                Arrays.asList(new Keyword(1L, "??????"), new Keyword(2L, "??????")));
    }

    private List<Image> createImages() {
        return Arrays.asList(
                new Image(1L, "url1"),
                new Image(2L, "url2"));
    }

    private List<Option> createOptions() {
        return Arrays.asList(
                new Option(1L, "??????",
                        new Option(2L, "??????", 1000),
                        new Option(3L, "??????", 2000)
                ),
                new Option(4L, "?????? ??????",
                        new Option(5L, "???", 3000),
                        new Option(6L, "???", 4000)
                )
        );
    }

}
