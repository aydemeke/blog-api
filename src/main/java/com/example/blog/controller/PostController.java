package com.example.blog.controller;

import com.example.blog.dto.request.CreatePostDto;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PopularPostDto;
import com.example.blog.dto.response.PostDto;
import com.example.blog.model.Post;
import com.example.blog.repository.PostRepository;
import com.example.blog.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository; // violation: controller bypasses service layer

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody @Valid CreatePostDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(postService.getAllPosts(page, size, sortBy, sortDir));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PopularPostDto>> getPopularPosts() {
        return ResponseEntity.ok(postService.getPopularPosts());
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<PostDto>> searchPosts(
            @RequestParam @NotBlank(message = "keyword must not be blank") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(postService.searchPosts(keyword, page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id,
                                              @RequestBody @Valid CreatePostDto request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // BAD: DB logic in controller, no @Transactional, Optional.get() without check
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long total = postRepository.count();

        // Optional.get() with no isPresent() check — will throw NoSuchElementException if table is empty
        Post firstPost = postRepository.findAll().stream().findFirst().get();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", total);
        stats.put("firstPostTitle", firstPost.getTitle());
        return ResponseEntity.ok(stats);
    }
}
