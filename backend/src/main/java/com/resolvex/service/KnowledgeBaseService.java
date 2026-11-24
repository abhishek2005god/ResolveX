package com.resolvex.service;

import com.resolvex.dto.KnowledgeDtos;
import com.resolvex.dto.PagedResponse;
import com.resolvex.entity.Category;
import com.resolvex.entity.KnowledgeArticle;
import com.resolvex.entity.User;
import com.resolvex.exception.ResourceNotFoundException;
import com.resolvex.repository.CategoryRepository;
import com.resolvex.repository.KnowledgeArticleRepository;
import com.resolvex.repository.UserRepository;
import com.resolvex.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {

    private final KnowledgeArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public KnowledgeBaseService(KnowledgeArticleRepository articleRepository,
                                CategoryRepository categoryRepository,
                                UserRepository userRepository,
                                AuditService auditService) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<KnowledgeDtos.KnowledgeArticleDto> getPublishedArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<KnowledgeArticle> paged = articleRepository.findByIsPublishedTrue(pageable);
        List<KnowledgeDtos.KnowledgeArticleDto> dtos = paged.getContent().stream().map(this::toDto).collect(Collectors.toList());

        return new PagedResponse<>(dtos, paged.getNumber(), paged.getSize(), paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    @Transactional(readOnly = true)
    public List<KnowledgeDtos.KnowledgeArticleDto> searchArticles(String query) {
        if (!StringUtils.hasText(query)) {
            return articleRepository.findAll().stream().limit(10).map(this::toDto).collect(Collectors.toList());
        }
        return articleRepository.searchArticles(query.trim()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public KnowledgeDtos.KnowledgeArticleDto getArticleById(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeArticle", "id", id));

        // Increment view count
        article.setViewsCount(article.getViewsCount() + 1);
        KnowledgeArticle saved = articleRepository.save(article);

        return toDto(saved);
    }

    @Transactional
    public KnowledgeDtos.KnowledgeArticleDto createArticle(KnowledgeDtos.CreateArticleRequest request, UserPrincipal currentUser) {
        User author = userRepository.findById(currentUser.getId()).orElse(null);

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        KnowledgeArticle article = new KnowledgeArticle(
                request.getTitle().trim(),
                request.getContent().trim(),
                request.getSummary(),
                category,
                author,
                request.getTags()
        );
        if (request.getIsPublished() != null) {
            article.setIsPublished(request.getIsPublished());
        }

        KnowledgeArticle saved = articleRepository.save(article);

        auditService.logAction(author, currentUser.getUsername(), "CREATE_ARTICLE", "KNOWLEDGE_ARTICLE", saved.getId(),
                "Published article: " + saved.getTitle(), "API");

        return toDto(saved);
    }

    @Transactional
    public KnowledgeDtos.KnowledgeArticleDto updateArticle(Long id, KnowledgeDtos.CreateArticleRequest request, UserPrincipal currentUser) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeArticle", "id", id));

        User user = userRepository.findById(currentUser.getId()).orElse(null);

        article.setTitle(request.getTitle().trim());
        article.setContent(request.getContent().trim());
        article.setSummary(request.getSummary());
        if (request.getCategoryId() != null) {
            article.setCategory(categoryRepository.findById(request.getCategoryId()).orElse(null));
        }
        if (request.getIsPublished() != null) {
            article.setIsPublished(request.getIsPublished());
        }
        article.setTags(request.getTags());

        KnowledgeArticle saved = articleRepository.save(article);

        auditService.logAction(user, currentUser.getUsername(), "UPDATE_ARTICLE", "KNOWLEDGE_ARTICLE", saved.getId(),
                "Updated article: " + saved.getTitle(), "API");

        return toDto(saved);
    }

    public KnowledgeDtos.KnowledgeArticleDto toDto(KnowledgeArticle a) {
        KnowledgeDtos.KnowledgeArticleDto dto = new KnowledgeDtos.KnowledgeArticleDto();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setContent(a.getContent());
        dto.setSummary(a.getSummary());
        if (a.getCategory() != null) {
            dto.setCategoryId(a.getCategory().getId());
            dto.setCategoryName(a.getCategory().getName());
        }
        if (a.getAuthor() != null) {
            dto.setAuthorId(a.getAuthor().getId());
            dto.setAuthorName(a.getAuthor().getFullName());
        }
        dto.setIsPublished(a.getIsPublished());
        dto.setViewsCount(a.getViewsCount());
        dto.setTags(a.getTags());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }
}
