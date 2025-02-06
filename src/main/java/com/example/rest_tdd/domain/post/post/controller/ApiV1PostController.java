package com.example.rest_tdd.domain.post.post.controller;

import com.example.rest_tdd.domain.member.member.entity.Member;
import com.example.rest_tdd.domain.post.post.dto.PageDto;
import com.example.rest_tdd.domain.post.post.dto.PostWithContentDto;
import com.example.rest_tdd.domain.post.post.entity.Post;
import com.example.rest_tdd.domain.post.post.service.PostService;
import com.example.rest_tdd.global.Rq;
import com.example.rest_tdd.global.dto.RsData;
import com.example.rest_tdd.global.exception.ServiceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {

    private final PostService postService;
    private final Rq rq;

    @GetMapping
    public RsData<PageDto> getItems(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "3") int pageSize,
                                    @RequestParam(defaultValue = "title") String keywordType,
                                    @RequestParam(defaultValue = "") String keyword) {
        Page<Post> postPage = postService.getListedItems(page, pageSize, keywordType, keyword);

        return new RsData<>(
                "200-1",
                "글 목록 조회가 완료되었습니다.",
                new PageDto(postPage)
        );

    }

    @GetMapping("/{id}")
    public RsData<PostWithContentDto> getItem(@PathVariable long id) {
        Post post = postService.getItem(id).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 글입니다.")
        );

        // 비공개 글인 경우에만 인증을 하고 읽어본다. 공개글인 경우 로그인 하지 않은 사람도 확인 가능
        if (!post.isPublished()) {
            Member writer = rq.getAuthenticatedWriter();
            post.canRead(writer);
        }

        return new RsData<>(
                "200-1",
                "%d번 글을 조회하였습니다.".formatted(id),
                new PostWithContentDto(post)
        );
    }

    record WriteReqBody(@NotBlank String title,
                        @NotBlank String content,
                        boolean published,
                        boolean listed) {
    }

    @PostMapping
    public RsData<PostWithContentDto> write(@Valid @RequestBody WriteReqBody body) {
        Member writer = rq.getAuthenticatedWriter();

        Post post = postService.write(writer, body.title(), body.content(), body.published(), body.listed());

        return new RsData<>(
                "201-1",
                "%d번 글 작성 완료되었습니다.".formatted(post.getId()),
                new PostWithContentDto(post)
        );
    }

    record ModifyReqBody(@NotBlank String title, @NotBlank String content) {
    }

    @PutMapping("/{id}")
    public RsData<PostWithContentDto> modify(@Valid @RequestBody ModifyReqBody body, @PathVariable long id) {
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
                new PostWithContentDto(post)
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
