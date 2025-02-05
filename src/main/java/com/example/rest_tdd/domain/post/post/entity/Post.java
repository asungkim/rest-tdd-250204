package com.example.rest_tdd.domain.post.post.entity;

import com.example.rest_tdd.domain.member.member.entity.Member;
import com.example.rest_tdd.domain.post.comment.entity.Comment;
import com.example.rest_tdd.global.entity.BaseTime;
import com.example.rest_tdd.global.exception.ServiceException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Post extends BaseTime {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;
    private String title;
    private String content;
    private boolean published;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    public Comment addComment(Member author, String content) {
        Comment comment = Comment.builder()
                .post(this)
                .author(author)
                .content(content)
                .build();

        this.comments.add(comment);

        return comment;
    }

    public Comment getCommentById(long commentId) {
        return comments.stream()
                .filter(c -> c.getId() == commentId)
                .findFirst()
                .orElseThrow(
                        () -> new ServiceException("404-2", "해당 댓글은 존재하지 않습니다.")
                );
    }

    public void deleteComment(Comment comment) {
        this.comments.remove(comment);
    }

    public boolean canModify(Member writer) {
        if (writer == null) {
            throw new ServiceException("401-1", "인증 정보가 없습니다.");
        }

        if (writer.isAdmin() || writer.equals(this.getAuthor())) {
            return true;
        }

        throw new ServiceException("403-1", "자신이 작성한 글만 수정 가능합니다.");
    }

    public boolean canDelete(Member writer) {
        if (writer == null) {
            throw new ServiceException("401-1", "인증 정보가 없습니다.");
        }

        if (writer.isAdmin() || writer.equals(this.getAuthor())) {
            return true;
        }

        throw new ServiceException("403-1", "자신이 작성한 글만 삭제 가능합니다.");
    }

    public void canRead(Member writer) {
        if (writer.isAdmin() || writer.equals(this.getAuthor())) {
            return;
        }

        throw new ServiceException("403-1", "비공개 설정된 글입니다.");
    }
}
