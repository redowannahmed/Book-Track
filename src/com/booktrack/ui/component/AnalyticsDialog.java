package com.booktrack.ui.component;

import com.booktrack.service.AnalyticsService;
import com.booktrack.service.AnalyticsService.*;
import com.booktrack.session.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 * Analytics dialog showing user reading insights
 */
public class AnalyticsDialog extends JDialog {
    private AnalyticsService analyticsService;
    private UserAnalytics analytics;
    
    public AnalyticsDialog(JFrame parent) {
        super(parent, "Your Reading Analytics", true);
        this.analyticsService = new AnalyticsService();
        
        initializeComponents();
        loadUserAnalytics();
    }
    
    private void initializeComponents() {
        setSize(900, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Main panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.decode("#f5f7fa"));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content in scrollable area
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.decode("#f5f7fa"));
        
        JPanel contentPanel = createContentPanel();
        scrollPane.setViewportView(contentPanel);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Footer with close button
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#34495e"));
        headerPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        // Title and subtitle
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.decode("#34495e"));
        
        JLabel titleLabel = new JLabel("📊 Your Reading Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Discover your reading patterns and insights");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.decode("#bdc3c7"));
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.decode("#f5f7fa"));
        contentPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Loading message initially
        JLabel loadingLabel = new JLabel("Loading your analytics...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        loadingLabel.setForeground(Color.decode("#7f8c8d"));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(loadingLabel);
        contentPanel.add(Box.createVerticalGlue());
        
        return contentPanel;
    }
    
    private void loadUserAnalytics() {
        SwingWorker<UserAnalytics, Void> worker = new SwingWorker<UserAnalytics, Void>() {
            @Override
            protected UserAnalytics doInBackground() throws Exception {
                int userId = SessionManager.getInstance().getCurrentUserId();
                return analyticsService.getUserAnalytics(userId);
            }
            
            @Override
            protected void done() {
                try {
                    analytics = get();
                    displayAnalytics();
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorMessage();
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayAnalytics() {
        // Remove loading content
        JScrollPane scrollPane = (JScrollPane) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
        JPanel contentPanel = createAnalyticsContent();
        scrollPane.setViewportView(contentPanel);
        revalidate();
        repaint();
    }
    
    private JPanel createAnalyticsContent() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.decode("#f5f7fa"));
        contentPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        // Overview stats cards
        contentPanel.add(createOverviewSection());
        contentPanel.add(Box.createVerticalStrut(25));
        
        // Genre breakdown
        contentPanel.add(createGenreSection());
        contentPanel.add(Box.createVerticalStrut(25));
        
        // Rating distribution
        contentPanel.add(createRatingSection());
        contentPanel.add(Box.createVerticalStrut(25));
        
        // Reading trends
        contentPanel.add(createTrendsSection());
        
        return contentPanel;
    }
    
    private JPanel createOverviewSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.decode("#f5f7fa"));
        
        JLabel sectionTitle = new JLabel("📈 Reading Overview");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(Color.decode("#2c3e50"));
        sectionTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        section.add(sectionTitle, BorderLayout.NORTH);
        
        // Stats cards grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 15, 15));
        statsGrid.setBackground(Color.decode("#f5f7fa"));
        
        Map<String, Object> stats = analytics.getBasicStats();
        
        statsGrid.add(createStatCard("Books Rated", String.valueOf(stats.get("booksRated")), "📚", "#3498db"));
        statsGrid.add(createStatCard("Books Reviewed", String.valueOf(stats.get("booksReviewed")), "✍️", "#2ecc71"));
        statsGrid.add(createStatCard("Custom Lists", String.valueOf(stats.get("customLists")), "📋", "#e74c3c"));
        statsGrid.add(createStatCard("Books Explored", String.valueOf(stats.get("booksInteracted")), "🔍", "#9b59b6"));
        
        double avgRating = (Double) stats.get("avgRatingGiven");
        String avgRatingStr = avgRating > 0 ? String.format("%.1f ⭐", avgRating) : "N/A";
        statsGrid.add(createStatCard("Avg Rating Given", avgRatingStr, "⭐", "#f39c12"));
        
        section.add(statsGrid, BorderLayout.CENTER);
        return section;
    }
    
    private JPanel createStatCard(String title, String value, String icon, String color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#ecf0f1"), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Icon and value
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(Color.decode(color));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(valueLabel, BorderLayout.EAST);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(Color.decode("#7f8c8d"));
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(titleLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createGenreSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.decode("#f5f7fa"));
        
        JLabel sectionTitle = new JLabel("📚 Genre Breakdown");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(Color.decode("#2c3e50"));
        sectionTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        section.add(sectionTitle, BorderLayout.NORTH);
        
        List<GenreStats> genres = analytics.getGenreBreakdown();
        
        if (genres.isEmpty()) {
            JLabel noGenres = new JLabel("No genre data available yet.");
            noGenres.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noGenres.setForeground(Color.decode("#7f8c8d"));
            section.add(noGenres, BorderLayout.CENTER);
        } else {
            JPanel genresPanel = new JPanel();
            genresPanel.setLayout(new BoxLayout(genresPanel, BoxLayout.Y_AXIS));
            genresPanel.setBackground(Color.decode("#f5f7fa"));
            
            for (int i = 0; i < Math.min(5, genres.size()); i++) {
                genresPanel.add(createGenreBar(genres.get(i)));
                // Add spacing between items, but not after the last one
                if (i < Math.min(5, genres.size()) - 1) {
                    genresPanel.add(Box.createVerticalStrut(15));
                }
            }
            
            section.add(genresPanel, BorderLayout.CENTER);
        }
        
        return section;
    }
    
    private JPanel createGenreBar(GenreStats genre) {
        JPanel barPanel = new JPanel(new BorderLayout());
        barPanel.setBackground(Color.WHITE);
        barPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#ecf0f1"), 1),
            new EmptyBorder(18, 20, 18, 20)
        ));
        barPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        barPanel.setPreferredSize(new Dimension(0, 80));
        
        // Left side: Genre info with better layout
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        
        // Genre name
        JLabel genreLabel = new JLabel(genre.getGenre());
        genreLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        genreLabel.setForeground(Color.decode("#2c3e50"));
        genreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Stats with more spacing
        String statsText = String.format("%d books • %.1f⭐ avg", 
            genre.getBookCount(), genre.getAvgRating());
        JLabel statsLabel = new JLabel(statsText);
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(Color.decode("#7f8c8d"));
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add some vertical spacing and ensure proper layout
        leftPanel.add(genreLabel);
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(statsLabel);
        leftPanel.add(Box.createVerticalGlue()); // Push content to top
        
        barPanel.add(leftPanel, BorderLayout.WEST);
        
        // Right side: Count badge with better styling
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(Color.WHITE);
        
        JLabel countLabel = new JLabel(String.valueOf(genre.getBookCount()));
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        countLabel.setForeground(Color.WHITE);
        countLabel.setOpaque(true);
        countLabel.setBackground(Color.decode("#3498db"));
        countLabel.setBorder(new EmptyBorder(6, 12, 6, 12));
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        rightPanel.add(countLabel);
        barPanel.add(rightPanel, BorderLayout.EAST);
        
        return barPanel;
    }
    
    private JPanel createRatingSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.decode("#f5f7fa"));
        
        JLabel sectionTitle = new JLabel("⭐ Rating Distribution");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(Color.decode("#2c3e50"));
        sectionTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        section.add(sectionTitle, BorderLayout.NORTH);
        
        Map<Integer, Integer> distribution = analytics.getRatingDistribution();
        int total = distribution.values().stream().mapToInt(Integer::intValue).sum();
        
        if (total == 0) {
            JLabel noRatings = new JLabel("No ratings data available yet.");
            noRatings.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noRatings.setForeground(Color.decode("#7f8c8d"));
            section.add(noRatings, BorderLayout.CENTER);
        } else {
            JPanel ratingsPanel = new JPanel();
            ratingsPanel.setLayout(new BoxLayout(ratingsPanel, BoxLayout.Y_AXIS));
            ratingsPanel.setBackground(Color.decode("#f5f7fa"));
            
            String[] colors = {"#e74c3c", "#e67e22", "#f39c12", "#27ae60", "#2ecc71"};
            
            for (int rating = 5; rating >= 1; rating--) {
                int count = distribution.get(rating);
                double percentage = (count * 100.0) / total;
                ratingsPanel.add(createRatingBar(rating, count, percentage, colors[rating - 1]));
                ratingsPanel.add(Box.createVerticalStrut(8));
            }
            
            section.add(ratingsPanel, BorderLayout.CENTER);
        }
        
        return section;
    }
    
    private JPanel createRatingBar(int rating, int count, double percentage, String color) {
        JPanel barPanel = new JPanel(new BorderLayout());
        barPanel.setBackground(Color.WHITE);
        barPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#ecf0f1"), 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        barPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        // Rating label
        String stars = "⭐".repeat(rating);
        JLabel ratingLabel = new JLabel(stars + " " + rating);
        ratingLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ratingLabel.setForeground(Color.decode("#2c3e50"));
        ratingLabel.setPreferredSize(new Dimension(80, 20));
        
        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) percentage);
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%d books (%.1f%%)", count, percentage));
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        progressBar.setForeground(Color.decode(color));
        progressBar.setBackground(Color.decode("#ecf0f1"));
        
        barPanel.add(ratingLabel, BorderLayout.WEST);
        barPanel.add(progressBar, BorderLayout.CENTER);
        
        return barPanel;
    }
    
    private JPanel createTrendsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.decode("#f5f7fa"));
        
        JLabel sectionTitle = new JLabel("📊 Reading Trends (Last 12 Months)");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(Color.decode("#2c3e50"));
        sectionTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        section.add(sectionTitle, BorderLayout.NORTH);
        
        List<MonthlyStats> trends = analytics.getReadingTrends();
        
        if (trends.isEmpty()) {
            JLabel noTrends = new JLabel("No reading trends data available yet.");
            noTrends.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noTrends.setForeground(Color.decode("#7f8c8d"));
            section.add(noTrends, BorderLayout.CENTER);
        } else {
            JPanel trendsPanel = new JPanel();
            trendsPanel.setLayout(new BoxLayout(trendsPanel, BoxLayout.Y_AXIS));
            trendsPanel.setBackground(Color.decode("#f5f7fa"));
            
            for (MonthlyStats trend : trends) {
                trendsPanel.add(createTrendBar(trend));
                trendsPanel.add(Box.createVerticalStrut(8));
            }
            
            section.add(trendsPanel, BorderLayout.CENTER);
        }
        
        return section;
    }
    
    private JPanel createTrendBar(MonthlyStats trend) {
        JPanel barPanel = new JPanel(new BorderLayout());
        barPanel.setBackground(Color.WHITE);
        barPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#ecf0f1"), 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        barPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        // Month label
        JLabel monthLabel = new JLabel(trend.getMonth());
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        monthLabel.setForeground(Color.decode("#2c3e50"));
        monthLabel.setPreferredSize(new Dimension(80, 20));
        
        // Stats
        String statsText = String.format("%d ratings • %.1f⭐ avg", 
            trend.getRatingsCount(), trend.getAvgRating());
        JLabel statsLabel = new JLabel(statsText);
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statsLabel.setForeground(Color.decode("#7f8c8d"));
        
        // Count badge
        JLabel countLabel = new JLabel(String.valueOf(trend.getRatingsCount()));
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        countLabel.setForeground(Color.WHITE);
        countLabel.setOpaque(true);
        countLabel.setBackground(Color.decode("#9b59b6"));
        countLabel.setBorder(new EmptyBorder(3, 6, 3, 6));
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        barPanel.add(monthLabel, BorderLayout.WEST);
        barPanel.add(statsLabel, BorderLayout.CENTER);
        barPanel.add(countLabel, BorderLayout.EAST);
        
        return barPanel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.decode("#f5f7fa"));
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.setBackground(Color.decode("#34495e"));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setFocusPainted(false);
        
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        footerPanel.add(closeButton);
        
        return footerPanel;
    }
    
    private void showErrorMessage() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.decode("#f5f7fa"));
        contentPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        
        JLabel errorLabel = new JLabel("❌ Unable to load analytics", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        errorLabel.setForeground(Color.decode("#e74c3c"));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel messageLabel = new JLabel("Please try again later", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(Color.decode("#7f8c8d"));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(errorLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalGlue());
        
        JScrollPane scrollPane = (JScrollPane) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
        scrollPane.setViewportView(contentPanel);
        revalidate();
        repaint();
    }
}
