package com.solebysole.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solebysole.authentication.service.AuthenticationService;
import com.solebysole.common.RestDocsConfiguration;
import com.solebysole.common.errors.InvalidTokenException;
import com.solebysole.common.errors.UserEmailDuplicationException;
import com.solebysole.docs.UserDocumentation;
import com.solebysole.user.application.UserService;
import com.solebysole.user.domain.Role;
import com.solebysole.user.domain.User;
import com.solebysole.user.dto.UserRegisterData;
import com.solebysole.user.dto.UserUpdateData;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UserController 클래스")
@Import(RestDocsConfiguration.class)
@AutoConfigureRestDocs
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJ1c2VySWQiOjF9.ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaDk";
    private static final String INVALID_TOKEN = VALID_TOKEN + "INVALID";

    private final Long savedId = 1L;
    private final Long existingId = 1L;

    private UserRegisterData userRegisterData;
    private UserRegisterData invalidUserRegisterData;
    private UserRegisterData duplicateUserRegisterData;

    private UserUpdateData userUpdateData;
    private UserUpdateData invalidUserUpdateData;

    private User user;

    @BeforeEach
    void setUp() {
        userRegisterData = createUserRegisterData("test@test.com");
        invalidUserRegisterData = createUserRegisterData("");
        duplicateUserRegisterData = createUserRegisterData("duplicate@test.com");

        userUpdateData = UserUpdateData.builder()
                .name("userName")
                .build();

        invalidUserUpdateData = UserUpdateData.builder()
                .name("")
                .build();

        user = User.builder()
                .id(existingId)
                .email("test@test.com")
                .name("test")
                .password("89ajf78@ajf8v8hg4712h6v982897vn@$6()*")
                .role(Role.ROLE_USER)
                .build();

        given(authenticationService.parseToken(VALID_TOKEN))
                .willReturn(1L);
        given(authenticationService.parseToken(INVALID_TOKEN))
                .willThrow(new InvalidTokenException(INVALID_TOKEN));
        given(authenticationService.loadUserById(existingId))
                .willReturn(user);
    }

    @Nested
    @DisplayName("POST 요청은")
    class Describe_POST {
        @Nested
        @DisplayName("올바른 회원 정보가 주어진다면")
        class Context_with_valid_user_register_data {
            @BeforeEach
            void setUp() {
                given(userService.registerUser(any(UserRegisterData.class)))
                        .willReturn(savedId);
            }

            @Test
            @DisplayName("상태코드 201 Created 를 응답한다.")
            void it_responds_status_code_201() throws Exception {
                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterData)))
                        .andExpect(header().string("location", "/api/users/" + savedId))
                        .andExpect(status().isCreated())
                        .andDo(UserDocumentation.createUser());
            }
        }

        @Nested
        @DisplayName("올바르지 않은 회원 정보가 주어진다면")
        class Context_with_invalid_user_register_data {
            @Test
            @DisplayName("상태코드 400 Bad Request 를 응답한다.")
            void it_responds_status_code_400() throws Exception {
                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRegisterData)))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("중복된 회원 이메일 주어진다면")
        class Context_with_duplicate_user_email {
            @BeforeEach
            void setUp() {
                given(userService.registerUser(any(UserRegisterData.class)))
                        .willThrow(new UserEmailDuplicationException());
            }

            @Test
            @DisplayName("상태코드 400 Bad Request 를 응답한다.")
            void it_responds_status_code_400() throws Exception {
                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUserRegisterData)))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("GET /me 요청은")
    class Describe_GET {
        @Nested
        @DisplayName("올바른 사용자가 요청한다면")
        class Context_with_valid_user {
            @Test
            @DisplayName("사용자 정보와 상태코드 200 OK 를 응답한다.")
            void it_responds_status_code_200() throws Exception {
                mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                        .andExpect(jsonPath("id").exists())
                        .andExpect(jsonPath("email").exists())
                        .andExpect(jsonPath("name").exists())
                        .andExpect(jsonPath("password").exists())
                        .andExpect(status().isOk())
                        .andDo(UserDocumentation.getUser());
            }
        }

        @Nested
        @DisplayName("알수 없는 사용자가 주어진다면")
        class Context_with_anonymous_user {
            @Test
            @DisplayName("상태코드 401 Unauthorized 를 응답한다.")
            void it_responds_status_code_401() throws Exception {
                mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isUnauthorized());
            }
        }
    }

    @Nested
    @DisplayName("PATCH /me 요청은")
    class Describe_PATCH {
        @Nested
        @DisplayName("올바른 수정 정보가 주어진다면")
        class Context_with_valid_user_update_data {
            @Test
            @DisplayName("사용자 정보와 상태코드 200 OK 를 응답한다.")
            void it_responds_status_code_200() throws Exception {
                mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(userUpdateData)))
                        .andExpect(status().isOk())
                        .andDo(UserDocumentation.updateUser());
            }
        }

        @Nested
        @DisplayName("올바르지 않은 수정 정보가 주어진다면")
        class Context_with_invalid_user_update_data {
            @Test
            @DisplayName("상태코드 400 Bad Request 를 응답한다.")
            void it_responds_status_code_400() throws Exception {
                mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .content(objectMapper.writeValueAsString(invalidUserUpdateData)))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("알수 없는 사용자가 주어진다면")
        class Context_with_anonymous_user {
            @Test
            @DisplayName("상태코드 401 Unauthorized 를 응답한다.")
            void it_responds_status_code_401() throws Exception {
                mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateData)))
                        .andExpect(status().isUnauthorized());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /me 요청은")
    class Describe_DELETE {
        @Nested
        @DisplayName("올바른 회원이 주어진다면")
        class Context_with_valid_user {
            @Test
            @DisplayName("상태코드 204 No Content 를 응답한다.")
            void it_responds_status_code_204() throws Exception {
                mockMvc.perform(delete("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                        .andExpect(status().isNoContent())
                        .andDo(UserDocumentation.deleteUser());
            }
        }

        @Nested
        @DisplayName("알수 없는 사용자가 주어진다면")
        class Context_with_anonymous_user {
            @Test
            @DisplayName("상태코드 401 Unauthorized 를 응답한다.")
            void it_responds_status_code_401() throws Exception {
                mockMvc.perform(delete("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isUnauthorized());
            }
        }
    }

    public UserRegisterData createUserRegisterData(String email) {
        return UserRegisterData.builder()
                .email(email)
                .name("test")
                .password("12345678")
                .build();
    }

}
