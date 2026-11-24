package com.resolvex.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public class KnowledgeDtos {

    public static class KnowledgeArticleDto {
        private Long id;
        private String title;
        private String content;
        private String summary;
        private Long categoryId;
        private String categoryName;
        private Long authorId;
        private String authorName;
        private Boolean isPublished;
        private Integer viewsCount;
        private String tags;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public Long getAuthorId() { return authorId; }
        public void setAuthorId(Long authorId) { this.authorId = authorId; }

        public String getAuthorName() { return authorName; }
        public void setAuthorName(String authorName) { this.authorName = authorName; }

        public Boolean getIsPublished() { return isPublished; }
        public void setIsPublished(Boolean published) { isPublished = published; }

        public Integer getViewsCount() { return viewsCount; }
        public void setViewsCount(Integer viewsCount) { this.viewsCount = viewsCount; }

        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }

        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class CreateArticleRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Content is required")
        private String content;

        private String summary;
        private Long categoryId;
        private Boolean isPublished = true;
        private String tags;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public Boolean getIsPublished() { return isPublished; }
        public void setIsPublished(Boolean published) { isPublished = published; }

        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }
}
