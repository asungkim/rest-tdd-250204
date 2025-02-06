package com.example.rest_tdd.domain.post.post.dto;

import com.example.rest_tdd.domain.post.post.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageDto {
    List<PostDto> items;
    private int totalPages;
    private long totalItems;
    private int currentPageNo;
    private int pageSize;

    public PageDto(Page<Post> postPage) {
        this.items = postPage.getContent().stream()
                .map(PostDto::new)
                .toList();
        this.totalPages = postPage.getTotalPages();
        this.totalItems = (int) postPage.getTotalElements();
        this.currentPageNo = postPage.getNumber();
        this.pageSize = postPage.getSize();

    }
}
