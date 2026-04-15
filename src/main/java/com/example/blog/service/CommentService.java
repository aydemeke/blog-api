package com.example.blog.service;

import com.example.blog.dto.request.CreateCommentDto;
import com.example.blog.dto.response.CommentDto;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    /**
     * Adds a new comment to an existing post.
     *
     * @param postId  the ID of the post to comment on
     * @param request the DTO containing content and author
     * @return the created comment as a {@link CommentDto}
     * @throws ResourceNotFoundException if no post exists with the given postId
     */
    @Transactional
    public CommentDto addComment(Long postId, CreateCommentDto request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(request.getAuthor())
                .post(post)
                .build();
        return toDto(commentRepository.save(comment));
    }

    /**
     * Retrieves all comments for a given post.
     *
     * @param postId the ID of the post whose comments to retrieve
     * @return a list of comments as {@link CommentDto}
     * @throws ResourceNotFoundException if no post exists with the given postId
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }
        return commentRepository.findByPostId(postId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Deletes a comment from a post.
     *
     * @param postId    the ID of the post the comment belongs to
     * @param commentId the ID of the comment to delete
     * @throws ResourceNotFoundException if no post exists with the given postId,
     *                                   or if no comment with the given commentId belongs to that post
     */
    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        commentRepository.delete(comment);
    }

    private CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAuthor())
                .createdAt(comment.getCreatedAt())
                .postId(comment.getPost().getId())
                .build();
    }
}
