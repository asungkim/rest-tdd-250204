package com.example.rest_tdd.domain.post.post.repository;

import com.example.rest_tdd.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findTopByOrderByIdDesc();

    List<Post> findByListed(boolean listed);
}
