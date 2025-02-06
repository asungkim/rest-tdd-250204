package com.example.rest_tdd.domain.post.comment.controller;

import com.example.rest_tdd.domain.post.comment.entity.Comment;
import com.example.rest_tdd.domain.post.post.entity.Post;
import com.example.rest_tdd.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    PostService postService;

    @Test
    @DisplayName("댓글 작성")
    void write() throws Exception {
        long postId = 1;
        String apiKey = "user1";
        String content = "댓글 내용";


        String requestBody = """
                {
                    "content": "%s"
                }
                """.formatted(content).stripIndent();

        ResultActions resultActions = mvc.perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .header("Authorization", "Bearer " + apiKey)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andDo(print());

        Post post = postService.getItem(postId).get();
        Comment comment = post.getLatestComment();

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d 번 댓글 작성이 완료되었습니다.".formatted(comment.getId())));
    }

}