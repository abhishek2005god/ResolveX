package com.resolvex.repository;

import com.resolvex.entity.KnowledgeArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
    Page<KnowledgeArticle> findByIsPublishedTrue(Pageable pageable);
    List<KnowledgeArticle> findByCategoryIdAndIsPublishedTrue(Long categoryId);

    @Query("SELECT k FROM KnowledgeArticle k WHERE k.isPublished = true AND (" +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(k.summary) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(k.tags) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<KnowledgeArticle> searchArticles(@Param("query") String query);
}
