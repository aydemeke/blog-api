package com.example.blog.service;

import com.example.blog.dto.request.CreatePostDto;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PopularPostDto;
import com.example.blog.dto.response.PostDto;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.model.Category;
import com.example.blog.model.Post;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Creates a new blog post.
     *
     * @param request the DTO containing title, content, author, and optional categoryId
     * @return the created post as a {@link PostDto}
     * @throws ResourceNotFoundException if a categoryId is provided but does not exist
     */
    @Transactional
    public PostDto createPost(CreatePostDto request) {
        Category category = resolveCategory(request.getCategoryId());
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(request.getAuthor())
                .category(category)
                .build();
        return toDto(postRepository.save(post));
    }

    /**
     * Retrieves a paginated and sorted list of all blog posts.
     *
     * @param page    zero-based page index (default 0)
     * @param size    number of posts per page (default 10)
     * @param sortBy  field to sort by (default "createdAt")
     * @param sortDir sort direction: "asc" or "desc" (default "desc")
     * @return a {@link PageResponse} containing the post page and pagination metadata
     */
    @Transactional(readOnly = true)
    public PageResponse<PostDto> getAllPosts(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, sortDir));
        return toPageResponse(postRepository.findAll(pageable));
    }

    /**
     * Searches posts by keyword in title or content, with pagination and sorting.
     *
     * @param keyword the search term matched against title and content (case-insensitive)
     * @param page    zero-based page index (default 0)
     * @param size    number of posts per page (default 10)
     * @param sortBy  field to sort by (default "createdAt")
     * @param sortDir sort direction: "asc" or "desc" (default "desc")
     * @return a {@link PageResponse} containing matching posts and pagination metadata
     */
    @Transactional(readOnly = true)
    public PageResponse<PostDto> searchPosts(String keyword, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, sortDir));
        return toPageResponse(
                postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable)
        );
    }

    /**
     * Retrieves a single blog post by its ID.
     *
     * @param id the ID of the post to retrieve
     * @return the post as a {@link PostDto}
     * @throws ResourceNotFoundException if no post exists with the given ID
     */
    @Transactional(readOnly = true)
    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        return toDto(post);
    }

    /**
     * Updates an existing blog post.
     *
     * @param id      the ID of the post to update
     * @param request the DTO containing updated title, content, author, and optional categoryId
     * @return the updated post as a {@link PostDto}
     * @throws ResourceNotFoundException if no post exists with the given ID, or if a categoryId
     *                                   is provided but does not exist
     */
    @Transactional
    public PostDto updatePost(Long id, CreatePostDto request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(request.getAuthor());
        post.setCategory(resolveCategory(request.getCategoryId()));
        return toDto(postRepository.save(post));
    }

    /**
     * Deletes a blog post by its ID.
     *
     * @param id the ID of the post to delete
     * @throws ResourceNotFoundException if no post exists with the given ID
     */
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        postRepository.delete(post);
    }

    /**
     * Returns the 5 posts with the highest number of comments, in descending order.
     *
     * @return list of up to 5 posts with their comment counts as {@link PopularPostDto}
     */
    @Transactional(readOnly = true)
    public List<PopularPostDto> getPopularPosts() {
        return postRepository.findTop5ByCommentCount(PageRequest.of(0, 5));
    }

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "title", "author", "createdAt", "updatedAt");

    private Sort buildSort(String sortBy, String sortDir) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sortBy value: '" + sortBy + "'. Allowed values: " + ALLOWED_SORT_FIELDS);
        }
        return sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    }

    private PageResponse<PostDto> toPageResponse(Page<Post> postPage) {
        List<PostDto> content = postPage.getContent().stream().map(this::toDto).toList();
        return PageResponse.<PostDto>builder()
                .content(content)
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .last(postPage.isLast())
                .build();
    }

    private PostDto toDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
                .build();
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }
}
