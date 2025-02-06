package com.example.rest_tdd.domain.post.post.controller;

import com.example.rest_tdd.domain.post.post.entity.Post;
import com.example.rest_tdd.domain.post.post.service.PostService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    private void checkPost(ResultActions resultActions, Post post) throws Exception {
        resultActions
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value(post.getTitle()))
                .andExpect(jsonPath("$.data.content").value(post.getContent()))
                .andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getNickname()))
                .andExpect(jsonPath("$.data.published").value(post.isPublished()))
                .andExpect(jsonPath("$.data.listed").value(post.isListed()))
                .andExpect(jsonPath("$.data.createdDate").value(matchesPattern(post.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.modifiedDate").value(matchesPattern(post.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
    }

    private ResultActions itemRequest(long postId, String apiKey) throws Exception {
        return mvc
                .perform(
                        get("/api/v1/posts/%d".formatted(postId))
                                .header("Authorization", "Bearer " + apiKey)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("글 단건 조회 - 다른 유저의 공개글 조회")
    void item1() throws Exception {
        long postId = 1;
        String apiKey = "";

        ResultActions resultActions = itemRequest(postId, apiKey);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글을 조회하였습니다.".formatted(postId)));

        Post post = postService.getItem(postId).get();
        checkPost(resultActions, post);
    }

    @Test
    @DisplayName("글 단건 조회 실패 - 없는 글인 경우")
    void item2() throws Exception {
        long postId = 100;
        String apiKey = "user2";

        ResultActions resultActions = itemRequest(postId, apiKey);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value("404-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."));

    }

    @Test
    @DisplayName("글 단건 조회 - 다른 유저의 비공개글 조회")
    void item3() throws Exception {

        long postId = 3;
        String apiKey = "user1";

        ResultActions resultActions = itemRequest(postId, apiKey);

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value("403-1"))
                .andExpect(jsonPath("$.msg").value("비공개 설정된 글입니다."));

    }

    private ResultActions writeRequest(String apiKey, String title, String content) throws Exception {
        String requestBody = """
                {
                    "title": "%s",
                    "content": "%s",
                    "published": true,
                    "listed": true
                }
                """.formatted(title, content).stripIndent();

        return mvc
                .perform(
                        post("/api/v1/posts")
                                .header("Authorization", "Bearer " + apiKey)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("글 작성")
    void write1() throws Exception {
        String title = "new title";
        String content = "new content";
        String apiKey = "user1";

        ResultActions resultActions = writeRequest(apiKey, title, content);

        Post post = postService.getLatestItem().get();

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 작성 완료되었습니다."
                        .formatted(post.getId())));

        checkPost(resultActions, post);
    }

    @Test
    @DisplayName("글 작성 실패 - 로그인 하지 않았을 때")
    void write2() throws Exception {
        String title = "new title";
        String content = "new content";
        String apiKey = "";

        ResultActions resultActions = writeRequest(apiKey, title, content);

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));


    }

    @Test
    @DisplayName("글 작성 실패 - 제목, 내용이 없을 때")
    void write3() throws Exception {
        String title = "";
        String content = "";
        String apiKey = "user1";

        ResultActions resultActions = writeRequest(apiKey, title, content);

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content : NotBlank : must not be blank
                        title : NotBlank : must not be blank
                        """.trim().stripIndent()));


    }

    private ResultActions modifyReqeust(long postId, String apiKey, String title, String content) throws Exception {
        String requestBody = """
                {
                    "title": "%s",
                    "content": "%s",
                    "published": true,
                    "listed": true
                }
                """.formatted(title, content).stripIndent();
        return mvc
                .perform(
                        put("/api/v1/posts/%d".formatted(postId))
                                .header("Authorization", "Bearer " + apiKey)
                                .contentType("application/json")
                                .content(requestBody)

                )
                .andDo(print());
    }

    @Test
    @DisplayName("글 수정")
    void modify1() throws Exception {

        long postId = 1;
        String title = "modified title";
        String content = "modified content";
        String apiKey = "user1";

        ResultActions resultActions = modifyReqeust(postId, apiKey, title, content);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 수정이 완료되었습니다.".formatted(postId)));

        Post post = postService.getItem(postId).get();
        checkPost(resultActions, post);
    }

    @Test
    @DisplayName("글 수정 실패 - no apiKey ")
    void modify2() throws Exception {

        long postId = 1;
        String title = "modified title";
        String content = "modified content";
        String apiKey = "123123123";

        ResultActions resultActions = modifyReqeust(postId, apiKey, title, content);

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
    }

    @Test
    @DisplayName("글 수정 실패 - no post ")
    void modify3() throws Exception {

        long postId = 1000;
        String title = "modified title";
        String content = "modified content";
        String apiKey = "user1";

        ResultActions resultActions = modifyReqeust(postId, apiKey, title, content);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("404-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."));
    }

    @Test
    @DisplayName("글 수정 실패 - no input data ")
    void modify4() throws Exception {

        long postId = 1;
        String title = "";
        String content = "";
        String apiKey = "user1";

        ResultActions resultActions = modifyReqeust(postId, apiKey, title, content);

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content : NotBlank : must not be blank
                        title : NotBlank : must not be blank
                        """.trim().stripIndent()));
    }

    @Test
    @DisplayName("글 수정 실패 - no permission ")
    void modify5() throws Exception {

        long postId = 1;
        String title = "다른 유저 제목 수정";
        String content = "다른 유저 내용 수정";
        String apiKey = "user2";

        ResultActions resultActions = modifyReqeust(postId, apiKey, title, content);

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("403-1"))
                .andExpect(jsonPath("$.msg").value("자신이 작성한 글만 수정 가능합니다."));
    }

    private ResultActions deleteRequest(long postId, String apiKey) throws Exception {
        return mvc
                .perform(
                        delete("/api/v1/posts/%d".formatted(postId))
                                .header("Authorization", "Bearer " + apiKey)

                )
                .andDo(print());
    }

    @Test
    @DisplayName("글 삭제")
    void delete1() throws Exception {

        long postId = 1;
        String apiKey = "user1";

        ResultActions resultActions = deleteRequest(postId, apiKey);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 삭제 완료되었습니다.".formatted(postId)));


        Assertions.assertThat(postService.getItem(postId)).isEmpty();
    }

    @Test
    @DisplayName("글 삭제 실패 - no apiKey ")
    void delete2() throws Exception {

        long postId = 1;
        String apiKey = "123123123";

        ResultActions resultActions = deleteRequest(postId, apiKey);

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
    }

    @Test
    @DisplayName("글 삭제 실패 - no post ")
    void delete3() throws Exception {

        long postId = 1000;
        String apiKey = "user1";

        ResultActions resultActions = deleteRequest(postId, apiKey);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("404-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."));
    }

    @Test
    @DisplayName("글 삭제 실패 - no permission ")
    void delete4() throws Exception {

        long postId = 1;
        String apiKey = "user2";

        ResultActions resultActions = deleteRequest(postId, apiKey);

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("403-1"))
                .andExpect(jsonPath("$.msg").value("자신이 작성한 글만 삭제 가능합니다."));
    }
}