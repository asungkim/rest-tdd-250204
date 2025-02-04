package com.example.rest_tdd;

import com.example.rest_tdd.domain.member.member.controller.ApiV1MemberController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RestTddApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("회원 가입")
    void join() throws Exception {
        String requestBody = """
                {
                    "username": "userNew",
                    "password": "1234",
                    "nickname": "무명"
                }
                """;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.msg").value("회원가입이 완료되었습니다."));
    }

}
