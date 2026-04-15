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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_validRequest_returnsCategoryDto() {
        // Arrange
        CreateCategoryDto request = CreateCategoryDto.builder()
                .name("Technology")
                .description("Tech-related posts")
                .build();
        Category savedCategory = Category.builder()
                .id(1L)
                .name("Technology")
                .description("Tech-related posts")
                .build();
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // Act
        CategoryDto result = categoryService.createCategory(request);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Technology", result.getName());
        assertEquals("Tech-related posts", result.getDescription());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_noDescription_returnsCategoryDtoWithNullDescription() {
        // Arrange
        CreateCategoryDto request = CreateCategoryDto.builder()
                .name("Misc")
                .build();
        Category savedCategory = Category.builder()
                .id(2L)
                .name("Misc")
                .description(null)
                .build();
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // Act
        CategoryDto result = categoryService.createCategory(request);

        // Assert
        assertEquals(2L, result.getId());
        assertEquals("Misc", result.getName());
        assertNull(result.getDescription());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void getAllCategories_categoriesExist_returnsListOfCategoryDtos() {
        // Arrange
        Category cat1 = Category.builder().id(1L).name("Technology").description("Tech").build();
        Category cat2 = Category.builder().id(2L).name("Science").description("Sci").build();
        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        // Act
        List<CategoryDto> result = categoryService.getAllCategories();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Technology", result.get(0).getName());
        assertEquals("Science", result.get(1).getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void getAllCategories_noCategoriesExist_returnsEmptyList() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(List.of());

        // Act
        List<CategoryDto> result = categoryService.getAllCategories();

        // Assert
        assertTrue(result.isEmpty());
        verify(categoryRepository).findAll();
    }

    @Test
    void getPostsByCategoryId_existingCategory_returnsPageResponse() {
        // Arrange
        Category category = Category.builder().id(1L).name("Technology").build();
        Post post1 = Post.builder().id(1L).title("Post One").content("Content 1").author("Alice").category(category).build();
        Post post2 = Post.builder().id(2L).title("Post Two").content("Content 2").author("Bob").category(category).build();
        Page<Post> postPage = new PageImpl<>(List.of(post1, post2));
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(postRepository.findByCategoryId(eq(1L), any(Pageable.class))).thenReturn(postPage);

        // Act
        PageResponse<PostDto> result = categoryService.getPostsByCategoryId(1L, 0, 10, "createdAt", "desc");

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(2L, result.getTotalElements());
        assertEquals("Post One", result.getContent().get(0).getTitle());
        assertEquals("Post Two", result.getContent().get(1).getTitle());
        verify(categoryRepository).existsById(1L);
        verify(postRepository).findByCategoryId(eq(1L), any(Pageable.class));
    }

    @Test
    void getPostsByCategoryId_existingCategoryNoPosts_returnsEmptyPageResponse() {
        // Arrange
        Page<Post> emptyPage = new PageImpl<>(List.of());
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(postRepository.findByCategoryId(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        PageResponse<PostDto> result = categoryService.getPostsByCategoryId(1L, 0, 10, "createdAt", "desc");

        // Assert
        assertTrue(result.getContent().isEmpty());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void getPostsByCategoryId_nonExistentCategory_throwsResourceNotFoundException() {
        // Arrange
        when(categoryRepository.existsById(99L)).thenReturn(false);

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getPostsByCategoryId(99L, 0, 10, "createdAt", "desc")
        );
        assertTrue(ex.getMessage().contains("99"));
    }
}
