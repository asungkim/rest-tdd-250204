package com.example.rest_tdd.domain.post.post.repository;

import com.example.rest_tdd.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
