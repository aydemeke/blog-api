package com.example.blog.service;

import com.example.blog.dto.request.CreatePostDto;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostDto;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.Post;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void createPost_validRequest_returnsPostDto() {
        // Arrange
        CreatePostDto request = CreatePostDto.builder()
                .title("Test Title")
                .content("Test Content")
                .author("Alice")
                .build();
        Post savedPost = Post.builder()
                .id(1L)
                .title("Test Title")
                .content("Test Content")
                .author("Alice")
                .build();
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // Act
        PostDto result = postService.createPost(request);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals("Alice", result.getAuthor());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void getAllPosts_postsExist_returnsPageResponse() {
        // Arrange
        Post post1 = Post.builder().id(1L).title("First").content("Content 1").author("Alice").build();
        Post post2 = Post.builder().id(2L).title("Second").content("Content 2").author("Bob").build();
        Page<Post> postPage = new PageImpl<>(List.of(post1, post2));
        when(postRepository.findAll(any(Pageable.class))).thenReturn(postPage);

        // Act
        PageResponse<PostDto> result = postService.getAllPosts(0, 10, "createdAt", "desc");

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(2L, result.getTotalElements());
        assertEquals("First", result.getContent().get(0).getTitle());
        assertEquals("Second", result.getContent().get(1).getTitle());
        verify(postRepository).findAll(any(Pageable.class));
    }

    @Test
    void searchPosts_matchingKeyword_returnsPageResponse() {
        // Arrange
        Post post = Post.builder().id(1L).title("Spring Boot Guide").content("Learn Spring Boot").author("Alice").build();
        Page<Post> postPage = new PageImpl<>(List.of(post));
        when(postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                eq("Spring"), eq("Spring"), any(Pageable.class))).thenReturn(postPage);

        // Act
        PageResponse<PostDto> result = postService.searchPosts("Spring", 0, 10, "createdAt", "desc");

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals("Spring Boot Guide", result.getContent().get(0).getTitle());
    }

    @Test
    void searchPosts_noMatches_returnsEmptyPageResponse() {
        // Arrange
        Page<Post> emptyPage = new PageImpl<>(List.of());
        when(postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                eq("xyz"), eq("xyz"), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        PageResponse<PostDto> result = postService.searchPosts("xyz", 0, 10, "createdAt", "desc");

        // Assert
        assertTrue(result.getContent().isEmpty());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void getPostById_existingId_returnsPostDto() {
        // Arrange
        Post post = Post.builder().id(1L).title("Test Title").content("Test Content").author("Alice").build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // Act
        PostDto result = postService.getPostById(1L);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals("Alice", result.getAuthor());
    }

    @Test
    void getPostById_nonExistentId_throwsResourceNotFoundException() {
        // Arrange
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> postService.getPostById(99L)
        );
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void updatePost_existingId_returnsUpdatedPostDto() {
        // Arrange
        Post existingPost = Post.builder().id(1L).title("Old Title").content("Old Content").author("Alice").build();
        CreatePostDto updateRequest = CreatePostDto.builder()
                .title("New Title")
                .content("New Content")
                .author("Alice")
                .build();
        Post updatedPost = Post.builder().id(1L).title("New Title").content("New Content").author("Alice").build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

        // Act
        PostDto result = postService.updatePost(1L, updateRequest);

        // Assert
        assertEquals("New Title", result.getTitle());
        assertEquals("New Content", result.getContent());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_nonExistentId_throwsResourceNotFoundException() {
        // Arrange
        CreatePostDto updateRequest = CreatePostDto.builder()
                .title("New Title")
                .content("New Content")
                .author("Alice")
                .build();
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.updatePost(99L, updateRequest)
        );
    }

    @Test
    void deletePost_existingId_deletesPost() {
        // Arrange
        Post post = Post.builder().id(1L).title("Test Title").content("Test Content").author("Alice").build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).delete(post);

        // Act
        postService.deletePost(1L);

        // Assert
        verify(postRepository).delete(post);
    }

    @Test
    void deletePost_nonExistentId_throwsResourceNotFoundException() {
        // Arrange
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.deletePost(99L)
        );
    }
}
