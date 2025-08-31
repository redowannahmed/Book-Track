package com.booktrack.model;

/**
 * Book model class representing book data from Google Books API
 */
public class Book {
    private Integer bookId;  // Database book ID
    private String googleBooksId;
    private String title;
    private String subtitle;
    private String[] authors;
    private String description;
    private String publisher;
    private String publishedDate;
    private int pageCount;
    private String[] categories;
    private double averageRating;
    private int ratingsCount;
    private String language;
    private String thumbnailUrl;
    private String smallThumbnailUrl;
    private String previewLink;
    private String infoLink;
    private String isbn10;
    private String isbn13;
    
    // Default constructor
    public Book() {}
    
    // Constructor with essential fields
    public Book(String googleBooksId, String title, String[] authors, String thumbnailUrl) {
        this.googleBooksId = googleBooksId;
        this.title = title;
        this.authors = authors;
        this.thumbnailUrl = thumbnailUrl;
    }
    
    // Business logic methods
    public String getAuthorsAsString() {
        if (authors == null || authors.length == 0) {
            return "Unknown Author";
        }
        return String.join(", ", authors);
    }
    
    public String getCategoriesAsString() {
        if (categories == null || categories.length == 0) {
            return "General";
        }
        return String.join(", ", categories);
    }
    
    public String getShortDescription() {
        if (description == null || description.isEmpty()) {
            return "No description available.";
        }
        if (description.length() <= 200) {
            return description;
        }
        return description.substring(0, 200) + "...";
    }
    
    public String getDisplayTitle() {
        if (subtitle != null && !subtitle.trim().isEmpty()) {
            return title + ": " + subtitle;
        }
        return title;
    }
    
    public String getRatingDisplay() {
        if (averageRating > 0) {
            return String.format("%.1f (%d reviews)", averageRating, ratingsCount);
        }
        return "No ratings yet";
    }
    
    public boolean hasValidThumbnail() {
        return thumbnailUrl != null && !thumbnailUrl.trim().isEmpty() && 
               !thumbnailUrl.contains("no-image");
    }
    
    // Getters and Setters
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    
    public String getGoogleBooksId() { return googleBooksId; }
    public void setGoogleBooksId(String googleBooksId) { this.googleBooksId = googleBooksId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    
    public String[] getAuthors() { return authors; }
    public void setAuthors(String[] authors) { this.authors = authors; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public String getPublishedDate() { return publishedDate; }
    public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
    
    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
    
    public String[] getCategories() { return categories; }
    public void setCategories(String[] categories) { this.categories = categories; }
    
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    
    public int getRatingsCount() { return ratingsCount; }
    public void setRatingsCount(int ratingsCount) { this.ratingsCount = ratingsCount; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    public String getSmallThumbnailUrl() { return smallThumbnailUrl; }
    public void setSmallThumbnailUrl(String smallThumbnailUrl) { this.smallThumbnailUrl = smallThumbnailUrl; }
    
    public String getPreviewLink() { return previewLink; }
    public void setPreviewLink(String previewLink) { this.previewLink = previewLink; }
    
    public String getInfoLink() { return infoLink; }
    public void setInfoLink(String infoLink) { this.infoLink = infoLink; }
    
    public String getIsbn10() { return isbn10; }
    public void setIsbn10(String isbn10) { this.isbn10 = isbn10; }
    
    public String getIsbn13() { return isbn13; }
    public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }
    
    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", authors=" + getAuthorsAsString() +
                ", publisher='" + publisher + '\'' +
                ", rating=" + averageRating +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Book book = (Book) obj;
        return googleBooksId != null && googleBooksId.equals(book.googleBooksId);
    }
    
    @Override
    public int hashCode() {
        return googleBooksId != null ? googleBooksId.hashCode() : 0;
    }
}
