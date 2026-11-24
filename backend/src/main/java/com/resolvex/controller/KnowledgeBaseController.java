package com.resolvex.controller;

import com.resolvex.dto.ApiResponse;
import com.resolvex.dto.KnowledgeDtos;
import com.resolvex.dto.PagedResponse;
import com.resolvex.security.UserPrincipal;
import com.resolvex.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-base")
@Tag(name = "Knowledge Base", description = "Enterprise articles, FAQs, and self-service documentation")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping
    @Operation(summary = "Get paginated published knowledge-base articles")
    public ResponseEntity<ApiResponse<PagedResponse<KnowledgeDtos.KnowledgeArticleDto>>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<KnowledgeDtos.KnowledgeArticleDto> result = knowledgeBaseService.getPublishedArticles(page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/search")
    @Operation(summary = "Search knowledge-base articles by keyword or tags")
    public ResponseEntity<ApiResponse<List<KnowledgeDtos.KnowledgeArticleDto>>> searchArticles(
            @RequestParam(required = false) String q) {
        List<KnowledgeDtos.KnowledgeArticleDto> articles = knowledgeBaseService.searchArticles(q);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get knowledge article by ID and increment view count")
    public ResponseEntity<ApiResponse<KnowledgeDtos.KnowledgeArticleDto>> getArticleById(@PathVariable Long id) {
        KnowledgeDtos.KnowledgeArticleDto article = knowledgeBaseService.getArticleById(id);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new knowledge-base article")
    public ResponseEntity<ApiResponse<KnowledgeDtos.KnowledgeArticleDto>> createArticle(
            @Valid @RequestBody KnowledgeDtos.CreateArticleRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        KnowledgeDtos.KnowledgeArticleDto created = knowledgeBaseService.createArticle(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Article published successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update an existing knowledge-base article")
    public ResponseEntity<ApiResponse<KnowledgeDtos.KnowledgeArticleDto>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeDtos.CreateArticleRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        KnowledgeDtos.KnowledgeArticleDto updated = knowledgeBaseService.updateArticle(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Article updated successfully", updated));
    }
}
