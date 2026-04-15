package com.example.blog.service;

import com.example.blog.dto.request.CreateCommentDto;
import com.example.blog.dto.response.CommentDto;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void addComment_existingPost_returnsCommentDto() {
        // Arrange
        Post post = Post.builder().id(1L).title("Post").content("Content").author("Alice").build();
        CreateCommentDto request = CreateCommentDto.builder()
                .content("Great post!")
                .author("Bob")
                .build();
        Comment savedComment = Comment.builder()
                .id(1L)
                .content("Great post!")
                .author("Bob")
                .post(post)
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // Act
        CommentDto result = commentService.addComment(1L, request);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Great post!", result.getContent());
        assertEquals("Bob", result.getAuthor());
        assertEquals(1L, result.getPostId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_nonExistentPost_throwsResourceNotFoundException() {
        // Arrange
        CreateCommentDto request = CreateCommentDto.builder()
                .content("Great post!")
                .author("Bob")
                .build();
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.addComment(99L, request)
        );
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void getCommentsByPostId_existingPost_returnsListOfCommentDtos() {
        // Arrange
        Post post = Post.builder().id(1L).title("Post").content("Content").author("Alice").build();
        Comment comment1 = Comment.builder().id(1L).content("First").author("Bob").post(post).build();
        Comment comment2 = Comment.builder().id(2L).content("Second").author("Carol").post(post).build();
        when(postRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByPostId(1L)).thenReturn(List.of(comment1, comment2));

        // Act
        List<CommentDto> result = commentService.getCommentsByPostId(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("First", result.get(0).getContent());
        assertEquals("Second", result.get(1).getContent());
        verify(commentRepository).findByPostId(1L);
    }

    @Test
    void getCommentsByPostId_nonExistentPost_throwsResourceNotFoundException() {
        // Arrange
        when(postRepository.existsById(99L)).thenReturn(false);

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.getCommentsByPostId(99L)
        );
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void deleteComment_existingPostAndComment_deletesComment() {
        // Arrange
        Post post = Post.builder().id(1L).title("Post").content("Content").author("Alice").build();
        Comment comment = Comment.builder().id(1L).content("Great post!").author("Bob").post(post).build();
        when(postRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByIdAndPostId(1L, 1L)).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(comment);

        // Act
        commentService.deleteComment(1L, 1L);

        // Assert
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_nonExistentPost_throwsResourceNotFoundException() {
        // Arrange
        when(postRepository.existsById(99L)).thenReturn(false);

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.deleteComment(99L, 1L)
        );
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void deleteComment_nonExistentComment_throwsResourceNotFoundException() {
        // Arrange
        when(postRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByIdAndPostId(99L, 1L)).thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.deleteComment(1L, 99L)
        );
        assertTrue(ex.getMessage().contains("99"));
    }
}
