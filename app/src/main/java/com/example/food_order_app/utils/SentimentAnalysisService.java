package com.example.food_order_app.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dịch vụ phân tích cảm xúc từ văn bản tiếng Việt
 * Sử dụng phương pháp dựa trên từ điển (Dictionary-based)
 */
public class SentimentAnalysisService {
    
    // Từ điển các từ tích cực
    private static final List<String> POSITIVE_WORDS = Arrays.asList(
        "ngon", "tuyệt", "đỉnh", "tốt", "thích", "yêu", "tuyệt vời", "hoàn hảo", "xuất sắc",
        "tươi", "béo ngậy", "thơm", "giòn", "mềm", "ngọt", "hấp dẫn", "ấn tượng",
        "chất lượng", "tươi ngon", "đậm đà", "hảo hạng", "đẹp", "nhanh", "nhiệt tình",
        "chu đáo", "tận tâm", "ok", "oke", "good", "great", "excellent", "nice", "delicious",
        "tasty", "amazing", "wonderful", "fantastic", "awesome", "perfect", "love", "like",
        "recommend", "best", "fresh", "crispy", "juicy", "tender", "flavorful", "yummy"
    );

    // Từ điển các từ tiêu cực
    private static final List<String> NEGATIVE_WORDS = Arrays.asList(
        "tệ", "dở", "kém", "hỏng", "thối", "cũ", "khó ăn", "mất vệ sinh", "không ngon",
        "chán", "nhạt", "dai", "khô", "cháy", "tanh", "ôi", "hôi", "nguội", "lạnh",
        "chậm", "lâu", "tệ hại", "thất vọng", "không tươi", "hết hạn", "kém chất lượng",
        "bad", "terrible", "horrible", "awful", "poor", "disappointing", "worst", "hate",
        "dislike", "disgusting", "gross", "stale", "bland", "overcooked", "undercooked",
        "cold", "slow", "rude", "expensive", "overpriced"
    );

    // Từ phủ định
    private static final List<String> NEGATION_WORDS = Arrays.asList(
        "không", "chẳng", "chả", "chưa", "never", "not", "no"
    );

    /**
     * Phân tích cảm xúc từ văn bản
     * @param text Văn bản cần phân tích
     * @return Map với sentiment (positive/negative/neutral) và score (0.0-1.0)
     */
    public static Map<String, Object> analyzeSentiment(String text) {
        Map<String, Object> result = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            result.put("sentiment", "neutral");
            result.put("score", 0.5);
            return result;
        }

        String lowerText = text.toLowerCase();
        String[] words = lowerText.split("\\s+");
        
        int positiveCount = 0;
        int negativeCount = 0;
        boolean hasNegation = false;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            
            // Kiểm tra từ phủ định
            if (NEGATION_WORDS.contains(word)) {
                hasNegation = true;
                continue;
            }

            // Đếm từ tích cực
            for (String positiveWord : POSITIVE_WORDS) {
                if (word.contains(positiveWord) || positiveWord.contains(word)) {
                    if (hasNegation) {
                        negativeCount++; // Phủ định của tích cực = tiêu cực
                        hasNegation = false;
                    } else {
                        positiveCount++;
                    }
                    break;
                }
            }

            // Đếm từ tiêu cực
            for (String negativeWord : NEGATIVE_WORDS) {
                if (word.contains(negativeWord) || negativeWord.contains(word)) {
                    if (hasNegation) {
                        positiveCount++; // Phủ định của tiêu cực = tích cực
                        hasNegation = false;
                    } else {
                        negativeCount++;
                    }
                    break;
                }
            }

            // Reset negation flag sau 2 từ
            if (hasNegation && i > 0 && (i - getLastNegationIndex(words, i)) > 2) {
                hasNegation = false;
            }
        }

        // Tính điểm cảm xúc
        int totalSentimentWords = positiveCount + negativeCount;
        String sentiment;
        double score;

        if (totalSentimentWords == 0) {
            sentiment = "neutral";
            score = 0.5;
        } else {
            double positiveRatio = (double) positiveCount / totalSentimentWords;
            
            if (positiveRatio > 0.6) {
                sentiment = "positive";
                score = 0.5 + (positiveRatio * 0.5); // 0.5 - 1.0
            } else if (positiveRatio < 0.4) {
                sentiment = "negative";
                score = (1 - positiveRatio) * 0.5; // 0.0 - 0.5
            } else {
                sentiment = "neutral";
                score = 0.5;
            }
        }

        result.put("sentiment", sentiment);
        result.put("score", Math.round(score * 100.0) / 100.0);
        result.put("positiveCount", positiveCount);
        result.put("negativeCount", negativeCount);
        
        return result;
    }

    /**
     * Tìm vị trí từ phủ định gần nhất
     */
    private static int getLastNegationIndex(String[] words, int currentIndex) {
        for (int i = currentIndex - 1; i >= 0; i--) {
            if (NEGATION_WORDS.contains(words[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Lấy sentiment dạng emoji
     */
    public static String getSentimentEmoji(String sentiment) {
        switch (sentiment) {
            case "positive": return "😊";
            case "negative": return "😞";
            case "neutral": return "😐";
            default: return "❓";
        }
    }

    /**
     * Lấy sentiment dạng tên tiếng Việt
     */
    public static String getSentimentName(String sentiment) {
        switch (sentiment) {
            case "positive": return "Tích cực";
            case "negative": return "Tiêu cực";
            case "neutral": return "Trung tính";
            default: return "Không xác định";
        }
    }

    /**
     * Lấy màu cho sentiment
     */
    public static int getSentimentColor(String sentiment) {
        switch (sentiment) {
            case "positive": return 0xFF4CAF50; // Green
            case "negative": return 0xFFF44336; // Red
            case "neutral": return 0xFFFF9800; // Orange
            default: return 0xFF9E9E9E; // Grey
        }
    }
}
