package com.booktrack.service;

import com.booktrack.config.DatabaseConfig;
import java.sql.*;
import java.util.*;

/**
 * Service for user analytics and insights
 */
public class AnalyticsService {
    private DatabaseConfig dbConfig;
    
    public AnalyticsService() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Get comprehensive user reading analytics
     */
    public UserAnalytics getUserAnalytics(int userId) {
        UserAnalytics analytics = new UserAnalytics();
        
        analytics.setBasicStats(getBasicReadingStats(userId));
        analytics.setGenreBreakdown(getGenreBreakdown(userId));
        analytics.setRatingDistribution(getRatingDistribution(userId));
        analytics.setReadingTrends(getReadingTrends(userId));
        
        return analytics;
    }
    
    /**
     * Get basic reading statistics
     */
    private Map<String, Object> getBasicReadingStats(int userId) {
        Map<String, Object> stats = new HashMap<>();
        
        String sql = """
            SELECT 
                COUNT(DISTINCT br.book_id) as books_rated,
                COUNT(DISTINCT rev.book_id) as books_reviewed,
                COUNT(DISTINCT cl.list_id) as custom_lists,
                COUNT(DISTINCT ubi.book_id) as books_interacted,
                ROUND(AVG(br.rating), 2) as avg_rating_given,
                MAX(br.rated_at) as last_rating_date,
                MAX(rev.created_at) as last_review_date
            FROM users u
            LEFT JOIN book_ratings br ON u.user_id = br.user_id
            LEFT JOIN book_reviews rev ON u.user_id = rev.user_id
            LEFT JOIN custom_lists cl ON u.user_id = cl.user_id
            LEFT JOIN user_book_interactions ubi ON u.user_id = ubi.user_id
            WHERE u.user_id = ?
            """;
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                stats.put("booksRated", rs.getInt("books_rated"));
                stats.put("booksReviewed", rs.getInt("books_reviewed"));
                stats.put("customLists", rs.getInt("custom_lists"));
                stats.put("booksInteracted", rs.getInt("books_interacted"));
                stats.put("avgRatingGiven", rs.getDouble("avg_rating_given"));
                stats.put("lastRatingDate", rs.getTimestamp("last_rating_date"));
                stats.put("lastReviewDate", rs.getTimestamp("last_review_date"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting basic stats: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Get genre reading breakdown
     */
    private List<GenreStats> getGenreBreakdown(int userId) {
        List<GenreStats> genres = new ArrayList<>();
        
        String sql = """
            SELECT 
                g.genre_name as genre,
                COUNT(*) as book_count,
                ROUND(AVG(br.rating), 2) as avg_rating,
                COUNT(rev.review_id) as reviews_written
            FROM book_ratings br
            JOIN books b ON br.book_id = b.book_id
            JOIN book_genres bg ON b.book_id = bg.book_id
            JOIN genres g ON bg.genre_id = g.genre_id
            LEFT JOIN book_reviews rev ON br.user_id = rev.user_id AND br.book_id = rev.book_id
            WHERE br.user_id = ?
            GROUP BY g.genre_name
            ORDER BY book_count DESC
            """;
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                GenreStats genre = new GenreStats();
                genre.setGenre(rs.getString("genre"));
                genre.setBookCount(rs.getInt("book_count"));
                genre.setAvgRating(rs.getDouble("avg_rating"));
                genre.setReviewsWritten(rs.getInt("reviews_written"));
                genres.add(genre);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting genre breakdown: " + e.getMessage());
        }
        
        return genres;
    }
    
    /**
     * Get user's rating distribution
     */
    private Map<Integer, Integer> getRatingDistribution(int userId) {
        Map<Integer, Integer> distribution = new HashMap<>();
        
        // Initialize all ratings
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }
        
        String sql = """
            SELECT rating, COUNT(*) as count
            FROM book_ratings
            WHERE user_id = ?
            GROUP BY rating
            ORDER BY rating
            """;
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                distribution.put(rs.getInt("rating"), rs.getInt("count"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting rating distribution: " + e.getMessage());
        }
        
        return distribution;
    }
    
    /**
     * Get reading trends over time (last 12 months)
     */
    private List<MonthlyStats> getReadingTrends(int userId) {
        List<MonthlyStats> trends = new ArrayList<>();
        
        String sql = """
            SELECT 
                TO_CHAR(br.rated_at, 'YYYY-MM') as month,
                COUNT(*) as ratings_count,
                ROUND(AVG(br.rating), 2) as avg_rating
            FROM book_ratings br
            WHERE br.user_id = ? 
            AND br.rated_at >= ADD_MONTHS(CURRENT_DATE, -12)
            GROUP BY TO_CHAR(br.rated_at, 'YYYY-MM')
            ORDER BY month
            """;
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                MonthlyStats stats = new MonthlyStats();
                stats.setMonth(rs.getString("month"));
                stats.setRatingsCount(rs.getInt("ratings_count"));
                stats.setAvgRating(rs.getDouble("avg_rating"));
                trends.add(stats);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting reading trends: " + e.getMessage());
        }
        
        return trends;
    }
    
    /**
     * Get user achievements based on activity
     */
    private List<Achievement> getUserAchievements(int userId) {
        List<Achievement> achievements = new ArrayList<>();
        Map<String, Object> stats = getBasicReadingStats(userId);
        
        // Safely get values with null checks
        Integer booksRatedObj = (Integer) stats.get("booksRated");
        Integer booksReviewedObj = (Integer) stats.get("booksReviewed");
        Integer customListsObj = (Integer) stats.get("customLists");
        Double avgRatingObj = (Double) stats.get("avgRatingGiven");
        
        int booksRated = booksRatedObj != null ? booksRatedObj : 0;
        int booksReviewed = booksReviewedObj != null ? booksReviewedObj : 0;
        int customLists = customListsObj != null ? customListsObj : 0;
        double avgRating = avgRatingObj != null ? avgRatingObj : 0.0;
        
        // Rating-based achievements
        if (booksRated >= 100) {
            achievements.add(new Achievement("Century Reader", "Rated 100+ books", "🏆"));
        } else if (booksRated >= 50) {
            achievements.add(new Achievement("Avid Reader", "Rated 50+ books", "📚"));
        } else if (booksRated >= 10) {
            achievements.add(new Achievement("Book Explorer", "Rated 10+ books", "🔍"));
        }
        
        // Review-based achievements
        if (booksReviewed >= 50) {
            achievements.add(new Achievement("Master Reviewer", "Wrote 50+ reviews", "✍️"));
        } else if (booksReviewed >= 10) {
            achievements.add(new Achievement("Review Writer", "Wrote 10+ reviews", "📝"));
        }
        
        // Organization achievements
        if (customLists >= 10) {
            achievements.add(new Achievement("List Master", "Created 10+ custom lists", "📋"));
        } else if (customLists >= 3) {
            achievements.add(new Achievement("Organizer", "Created multiple lists", "📄"));
        }
        
        // Rating pattern achievements
        if (avgRating >= 4.5 && booksRated >= 5) {
            achievements.add(new Achievement("Optimist", "Average rating 4.5+", "😊"));
        } else if (avgRating <= 2.5 && booksRated >= 10) {
            achievements.add(new Achievement("Critic", "Tough reviewer", "🎭"));
        }
        
        // Genre diversity achievement
        List<GenreStats> genres = getGenreBreakdown(userId);
        if (genres.size() >= 5) {
            achievements.add(new Achievement("Genre Explorer", "Read across 5+ genres", "🌟"));
        }
        
        // First timer achievements
        if (booksRated >= 1) {
            achievements.add(new Achievement("First Rating", "Rated your first book", "⭐"));
        }
        if (booksReviewed >= 1) {
            achievements.add(new Achievement("First Review", "Wrote your first review", "✏️"));
        }
        if (customLists >= 1) {
            achievements.add(new Achievement("List Creator", "Created your first list", "📝"));
        }
        
        return achievements;
    }
    
    // Data classes for analytics
    public static class UserAnalytics {
        private Map<String, Object> basicStats;
        private List<GenreStats> genreBreakdown;
        private Map<Integer, Integer> ratingDistribution;
        private List<MonthlyStats> readingTrends;
        
        // Getters and setters
        public Map<String, Object> getBasicStats() { return basicStats; }
        public void setBasicStats(Map<String, Object> basicStats) { this.basicStats = basicStats; }
        
        public List<GenreStats> getGenreBreakdown() { return genreBreakdown; }
        public void setGenreBreakdown(List<GenreStats> genreBreakdown) { this.genreBreakdown = genreBreakdown; }
        
        public Map<Integer, Integer> getRatingDistribution() { return ratingDistribution; }
        public void setRatingDistribution(Map<Integer, Integer> ratingDistribution) { this.ratingDistribution = ratingDistribution; }
        
        public List<MonthlyStats> getReadingTrends() { return readingTrends; }
        public void setReadingTrends(List<MonthlyStats> readingTrends) { this.readingTrends = readingTrends; }
    }
    
    public static class GenreStats {
        private String genre;
        private int bookCount;
        private double avgRating;
        private int reviewsWritten;
        
        // Getters and setters
        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
        
        public int getBookCount() { return bookCount; }
        public void setBookCount(int bookCount) { this.bookCount = bookCount; }
        
        public double getAvgRating() { return avgRating; }
        public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
        
        public int getReviewsWritten() { return reviewsWritten; }
        public void setReviewsWritten(int reviewsWritten) { this.reviewsWritten = reviewsWritten; }
    }
    
    public static class MonthlyStats {
        private String month;
        private int ratingsCount;
        private double avgRating;
        
        // Getters and setters
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        
        public int getRatingsCount() { return ratingsCount; }
        public void setRatingsCount(int ratingsCount) { this.ratingsCount = ratingsCount; }
        
        public double getAvgRating() { return avgRating; }
        public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
    }
    
    public static class Achievement {
        private String title;
        private String description;
        private String icon;
        
        public Achievement(String title, String description, String icon) {
            this.title = title;
            this.description = description;
            this.icon = icon;
        }
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }
}
