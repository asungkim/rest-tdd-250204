package com.example.rest_tdd.domain.post.comment.dto;

import com.example.rest_tdd.domain.post.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentDto {
    private long commentId;
    private String content;
    private long postId;
    private long authorId;
    private String authorName;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;

    public CommentDto(Comment comment) {
        this.commentId=comment.getId();
        this.content=comment.getContent();
        this.postId=comment.getPost().getId();
        this.authorId=comment.getAuthor().getId();
        this.authorName=comment.getAuthor().getUsername();
        this.createdTime=comment.getCreatedDate();
        this.modifiedTime=comment.getModifiedDate();
    }
}
