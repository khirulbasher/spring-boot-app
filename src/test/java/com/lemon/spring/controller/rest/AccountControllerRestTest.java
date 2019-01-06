package com.lemon.spring.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.framework.web.data.UserInfo;
import com.lemon.spring.Application;
import com.lemon.spring.domain.AuthorityModel;
import com.lemon.spring.domain.UserModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;

import static com.lemon.spring.controller.rest.AccountControllerRest.BASE_PATH;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = MOCK,
        classes = Application.class)
@AutoConfigureMockMvc
public class AccountControllerRestTest {
    @Inject
    private MockMvc mockMvc;

    @Inject
    private ObjectMapper objectMapper;

    /*
        TODO: If Enable it, It just show you that, the bean is or not called by test, and inside things not execute, and if disable, it work with existing code
        Like here it will save the user, if it enable, it doesn't save the user
    @MockBean
    private AccountService accountService;*/

    /*@MockBean
    private AccountService accountService;*/

    @Before
    public void setUp() throws Exception {
        System.out.print("");
    }

    @Test
    public void save() throws Exception {
        UserModel user= adminCreation();
        /*objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(user );*/

        mockMvc.perform(post("/api"+ BASE_PATH)
        .contentType(MediaType.APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(user)).accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    private UserModel simpleUserCreation() {
        UserModel user=new UserModel();
        user.setUsername("lemon");
        user.setAuthorities(new HashSet<>(Arrays.asList(new AuthorityModel("ROLE_USER"),new AuthorityModel("ROLE_REST_TEST"))));
        user.setPassword("rest-test-123");
        user.setEmail("resttest@mail.com");
        user.setFullName("Rest Test Full Name");
        return user;
    }

    private UserModel adminCreation() {
        UserModel user=new UserModel();
        user.setUsername("admin");
        user.setAuthorities(new HashSet<>(Arrays.asList(new AuthorityModel("ROLE_USER"),new AuthorityModel("ROLE_REST_TEST"),new AuthorityModel("ROLE_ADMIN"))));
        user.setPassword("admin");
        user.setEmail("admin@mail.com");
        user.setFullName("Admin Test Full Name");
        return user;
    }

    @Test
    public void login() throws Exception {
        loginSimple("lemon","rest-test-123");
    }

    private void loginSimple(String username, String password) throws Exception {
        UserInfo userInfo =new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPassword(password);

        mockMvc.perform(post("/api"+ BASE_PATH+"/login-rest")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userInfo))
        ).andExpect(status().isOk());
    }

    private void loginJwt(String username, String password) throws Exception {
        UserInfo userInfo =new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPassword(password);

        mockMvc.perform(post("/api"+ BASE_PATH+"/login-jwt")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userInfo))
        ).andExpect(status().isOk());
    }


    @Test
    public void keyVal() throws Exception {
        loginAdmin();
        mockMvc.perform(get("/api"+BASE_PATH+"/key/username")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isOk());

    }

    private void loginAdmin() throws Exception {
        loginJwt("admin","admin");
    }
}