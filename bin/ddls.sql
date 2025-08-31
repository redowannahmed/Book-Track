CREATE SEQUENCE seq_user_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_book_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_author_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_genre_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_tag_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_custom_list_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_review_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_rating_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_interaction_id START WITH 1 INCREMENT BY 1;

-- =====================================================
-- CORE TABLES
-- =====================================================

-- Users table - stores user account information
CREATE TABLE users (
    user_id NUMBER PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    email VARCHAR2(100) NOT NULL UNIQUE,
    password_hash VARCHAR2(255) NOT NULL,
    first_name VARCHAR2(50),
    last_name VARCHAR2(50),
    profile_picture_url VARCHAR2(500),
    bio CLOB,
    date_joined DATE DEFAULT SYSDATE,
    last_login DATE,
    is_active NUMBER(1) DEFAULT 1,
    total_books_read NUMBER DEFAULT 0,
    total_reviews NUMBER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Authors table - stores author information
CREATE TABLE authors (
    author_id NUMBER PRIMARY KEY,
    author_name VARCHAR2(200) NOT NULL,
    author_bio CLOB,
    birth_date DATE,
    death_date DATE,
    nationality VARCHAR2(100),
    author_image_url VARCHAR2(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Genres table - stores book genres
CREATE TABLE genres (
    genre_id NUMBER PRIMARY KEY,
    genre_name VARCHAR2(100) NOT NULL UNIQUE,
    genre_description CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tags table - stores custom tags for books
CREATE TABLE tags (
    tag_id NUMBER PRIMARY KEY,
    tag_name VARCHAR2(50) NOT NULL UNIQUE,
    tag_color VARCHAR2(7) DEFAULT '#3498db', -- Hex color code
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Books table - stores book information (from Google Books API and manual entries)
CREATE TABLE books (
    book_id NUMBER PRIMARY KEY,
    google_books_id VARCHAR2(100) UNIQUE, -- Google Books API ID
    title VARCHAR2(500) NOT NULL,
    subtitle VARCHAR2(500),
    description CLOB,
    isbn_10 VARCHAR2(10),
    isbn_13 VARCHAR2(13),
    published_date DATE,
    publisher VARCHAR2(200),
    page_count NUMBER,
    language_code VARCHAR2(10) DEFAULT 'en',
    cover_image_url VARCHAR2(500),
    preview_link VARCHAR2(500),
    info_link VARCHAR2(500),
    average_rating NUMBER(3,2) DEFAULT 0,
    ratings_count NUMBER DEFAULT 0,
    total_reviews NUMBER DEFAULT 0,
    popularity_score NUMBER DEFAULT 0, -- For trending/popular books
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- JUNCTION TABLES (Many-to-Many Relationships)
-- =====================================================

-- Book-Author junction table (many-to-many)
CREATE TABLE book_authors (
    book_id NUMBER NOT NULL,
    author_id NUMBER NOT NULL,
    author_role VARCHAR2(50) DEFAULT 'Author', -- Author, Co-author, Editor, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(author_id) ON DELETE CASCADE
);

-- Book-Genre junction table (many-to-many)
CREATE TABLE book_genres (
    book_id NUMBER NOT NULL,
    genre_id NUMBER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (book_id, genre_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id) ON DELETE CASCADE
);

-- Book-Tag junction table (many-to-many)
CREATE TABLE book_tags (
    book_id NUMBER NOT NULL,
    tag_id NUMBER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (book_id, tag_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);

-- =====================================================
-- USER INTERACTION TABLES
-- =====================================================

-- Custom Lists table - user-created lists (wish to read, favorites, custom collections)
CREATE TABLE custom_lists (
    list_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    list_name VARCHAR2(100) NOT NULL,
    list_description CLOB,
    list_type VARCHAR2(20) NOT NULL CHECK (list_type IN ('WANT_TO_READ', 'CURRENTLY_READING', 'HAVE_READ', 'FAVORITES', 'CUSTOM')),
    is_public NUMBER(1) DEFAULT 1,
    is_default NUMBER(1) DEFAULT 0, -- For system default lists
    books_count NUMBER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE (user_id, list_name)
);

-- Custom List-Books junction table
CREATE TABLE custom_list_books (
    list_id NUMBER NOT NULL,
    book_id NUMBER NOT NULL,
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes CLOB, -- User's personal notes about this book in this list
    PRIMARY KEY (list_id, book_id),
    FOREIGN KEY (list_id) REFERENCES custom_lists(list_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);

-- Book Ratings table - user ratings for books
CREATE TABLE book_ratings (
    rating_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    book_id NUMBER NOT NULL,
    rating NUMBER(2,1) NOT NULL CHECK (rating >= 1 AND rating <= 5),
    rated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    UNIQUE (user_id, book_id)
);

-- Book Reviews table - user reviews for books
CREATE TABLE book_reviews (
    review_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    book_id NUMBER NOT NULL,
    review_title VARCHAR2(200),
    review_text CLOB NOT NULL,
    is_spoiler NUMBER(1) DEFAULT 0,
    is_public NUMBER(1) DEFAULT 1,
    likes_count NUMBER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    UNIQUE (user_id, book_id)
);

-- User Book Interactions table - tracks all user interactions with books (for timeline/history)
CREATE TABLE user_book_interactions (
    interaction_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    book_id NUMBER NOT NULL,
    interaction_type VARCHAR2(30) NOT NULL CHECK (interaction_type IN (
        'ADDED_TO_WANT', 'ADDED_TO_READING', 'ADDED_TO_READ', 'ADDED_TO_FAVORITES',
        'REMOVED_FROM_WANT', 'REMOVED_FROM_READING', 'REMOVED_FROM_READ', 'REMOVED_FROM_FAVORITES',
        'RATED', 'REVIEWED', 'UPDATED_REVIEW', 'DELETED_REVIEW',
        'ADDED_TO_CUSTOM_LIST', 'REMOVED_FROM_CUSTOM_LIST', 'SEARCHED', 'VIEWED'
    )),
    interaction_details VARCHAR2(500), -- JSON or additional info about the interaction
    list_id NUMBER, -- Reference to custom list if applicable
    old_value VARCHAR2(100), -- For update operations
    new_value VARCHAR2(100), -- For update operations
    interaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    FOREIGN KEY (list_id) REFERENCES custom_lists(list_id) ON DELETE SET NULL
);

