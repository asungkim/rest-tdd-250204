package com.example.rest_tdd.domain.post.post.controller;

import com.example.rest_tdd.domain.member.member.entity.Member;
import com.example.rest_tdd.domain.post.post.dto.PostDto;
import com.example.rest_tdd.domain.post.post.entity.Post;
import com.example.rest_tdd.domain.post.post.service.PostService;
import com.example.rest_tdd.global.Rq;
import com.example.rest_tdd.global.dto.RsData;
import com.example.rest_tdd.global.exception.ServiceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {

    private final PostService postService;
    private final Rq rq;

    @GetMapping("/{id}")
    public RsData<PostDto> getItem(@PathVariable long id) {
        Post post = postService.getItem(id).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 글입니다.")
        );

        // 비공개 글인 경우
        if (!post.isPublished()) {
            Member writer = rq.getAuthenticatedWriter();
            post.canRead(writer);
        }

        return new RsData<>(
                "200-1",
                "%d번 글을 조회하였습니다.".formatted(id),
                new PostDto(post)
        );
    }

    record WriteReqBody(@NotBlank String title,
                        @NotBlank String content,
                        boolean published) {
    }

    @PostMapping
    public RsData<PostDto> write(@Valid @RequestBody WriteReqBody body) {
        Member writer = rq.getAuthenticatedWriter();

        Post post = postService.write(writer, body.title(), body.content(), body.published());

        return new RsData<>(
                "201-1",
                "%d번 글 작성 완료되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }

    record ModifyReqBody(@NotBlank String title, @NotBlank String content) {
    }

    @PutMapping("/{id}")
    public RsData<PostDto> modify(@Valid @RequestBody ModifyReqBody body, @PathVariable long id) {
        Member writer = rq.getAuthenticatedWriter();

        Post post = postService.getItem(id).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 글입니다.")
        );

        if (post.canModify(writer)) {
            postService.modify(post, body.title(), body.content());

        }
        return new RsData<>(
                "200-1",
                "%d번 글 수정이 완료되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }

    @DeleteMapping("/{id}")
    public RsData<Void> delete(@PathVariable long id) {
        Member writer = rq.getAuthenticatedWriter();

        Post post = postService.getItem(id).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 글입니다.")
        );

        if (post.canDelete(writer)) {
            postService.delete(post);
        }

        return new RsData<>(
                "200-1",
                "%d번 글 삭제 완료되었습니다.".formatted(id)
        );
    }
}
