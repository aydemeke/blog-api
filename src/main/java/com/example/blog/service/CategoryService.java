package com.example.blog.service;

import com.example.blog.dto.request.CreateCategoryDto;
import com.example.blog.dto.response.CategoryDto;
import com.example.blog.dto.response.PageResponse;
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

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    /**
     * Creates a new category.
     *
     * @param request the DTO containing the category name and optional description
     * @return the created category as a {@link CategoryDto}
     */
    @Transactional
    public CategoryDto createCategory(CreateCategoryDto request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return toDto(categoryRepository.save(category));
    }

    /**
     * Retrieves all categories.
     *
     * @return list of all categories as {@link CategoryDto}
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Retrieves a paginated list of posts belonging to the given category.
     *
     * @param categoryId the ID of the category
     * @param page       zero-based page index
     * @param size       number of posts per page
     * @param sortBy     field to sort by
     * @param sortDir    sort direction: "asc" or "desc"
     * @return a {@link PageResponse} containing matching posts and pagination metadata
     * @throws ResourceNotFoundException if no category exists with the given ID
     */
    @Transactional(readOnly = true)
    public PageResponse<PostDto> getPostsByCategoryId(Long categoryId, int page, int size,
                                                      String sortBy, String sortDir) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> postPage = postRepository.findByCategoryId(categoryId, pageable);
        List<PostDto> content = postPage.getContent().stream().map(this::toPostDto).toList();
        return PageResponse.<PostDto>builder()
                .content(content)
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .last(postPage.isLast())
                .build();
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    private PostDto toPostDto(Post post) {
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
}
