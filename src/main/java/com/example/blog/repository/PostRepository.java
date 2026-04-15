package com.example.blog.repository;

import com.example.blog.dto.response.PopularPostDto;
import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);

    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("""
            SELECT new com.example.blog.dto.response.PopularPostDto(
                p.id, p.title, p.content, p.author, p.createdAt, p.updatedAt, COUNT(c))
            FROM Post p LEFT JOIN Comment c ON c.post = p
            GROUP BY p
            ORDER BY COUNT(c) DESC
            """)
    List<PopularPostDto> findTop5ByCommentCount(Pageable pageable);
}
