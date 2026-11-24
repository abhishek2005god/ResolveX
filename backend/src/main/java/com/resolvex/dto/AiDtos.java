package com.resolvex.dto;

import com.resolvex.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AiDtos {

    public static class AiTicketAnalysisRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Description is required")
        private String description;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class AiTicketAnalysisResponse {
        private Long suggestedCategoryId;
        private String suggestedCategoryName;
        private TicketPriority suggestedPriority;
        private String summary;
        private double confidence;
        private boolean isAiGenerated;
        private List<KnowledgeDtos.KnowledgeArticleDto> recommendedArticles;

        public Long getSuggestedCategoryId() { return suggestedCategoryId; }
        public void setSuggestedCategoryId(Long suggestedCategoryId) { this.suggestedCategoryId = suggestedCategoryId; }

        public String getSuggestedCategoryName() { return suggestedCategoryName; }
        public void setSuggestedCategoryName(String suggestedCategoryName) { this.suggestedCategoryName = suggestedCategoryName; }

        public TicketPriority getSuggestedPriority() { return suggestedPriority; }
        public void setSuggestedPriority(TicketPriority suggestedPriority) { this.suggestedPriority = suggestedPriority; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public boolean isAiGenerated() { return isAiGenerated; }
        public void setAiGenerated(boolean aiGenerated) { isAiGenerated = aiGenerated; }

        public List<KnowledgeDtos.KnowledgeArticleDto> getRecommendedArticles() { return recommendedArticles; }
        public void setRecommendedArticles(List<KnowledgeDtos.KnowledgeArticleDto> recommendedArticles) { this.recommendedArticles = recommendedArticles; }
    }
}
