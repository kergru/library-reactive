-- schema_and_seed.sql
-- Struktur und Seed-Daten für users, books, loans

CREATE DATABASE IF NOT EXISTS library;

-- Berechtigungen für admin-User
CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'pwd';
GRANT ALL PRIVILEGES ON library.* TO 'admin'@'%';
FLUSH PRIVILEGES;

USE library;

-- users
CREATE TABLE users
(
    id        BIGINT       NOT NULL,
    username  VARCHAR(100) NOT NULL,
    firstname VARCHAR(100) NOT NULL,
    lastname  VARCHAR(100) NOT NULL,
    email     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- books
CREATE TABLE books
(
    id           BIGINT       NOT NULL,
    isbn         VARCHAR(32)  NOT NULL,
    title        VARCHAR(255) NOT NULL,
    author       VARCHAR(255) NOT NULL,
    published_at INT          NOT NULL,
    publisher    VARCHAR(255) NOT NULL,
    language     VARCHAR(32)  NOT NULL,
    description  TEXT         NOT NULL,
    pages        INT          NOT NULL,
    PRIMARY KEY (id),
    KEY          idx_books_isbn(isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- loans
CREATE TABLE loans
(
    id          BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    book_id     BIGINT       NOT NULL,
    borrowed_at TIMESTAMP(6) NOT NULL,
    returned_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    KEY         idx_loans_user(user_id),
    KEY         idx_loans_book(book_id),
    CONSTRAINT fk_loans_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed-Daten
-- users
INSERT INTO users (id, username, firstname, lastname, email)
VALUES (1, 'demo_user_1', 'Demo', 'Eins', 'demo1@example.test'),
       (2, 'demo_user_2', 'Demo', 'Zwei', 'demo2@example.test'),
       (3, 'demo_user_3', 'Demo', 'Drei', 'demo3@example.test'),
       (4, 'librarian', 'The', 'Librarian', 'librarian@example.test');

-- books
INSERT INTO books (id, isbn, title, author, published_at, publisher, language, description, pages)
VALUES (1001, '9780132350884', 'Clean Code', 'Robert C. Martin', 2008, 'Prentice Hall', 'EN', 'Beschreibung Clean Code', 464),
       (1002, '9780134685991', 'Effective Java', 'Joshua Bloch', 2018, 'Addison-Wesley', 'EN', 'Beschreibung Effective Java', 416),
       (1003, '9780201633610', 'Design Patterns', 'Gamma; Helm; Johnson; Vlissides', 1994, 'Addison-Wesley', 'EN', 'Beschreibung Design Patterns', 395),
       (1004, '9780134757599', 'Refactoring', 'Martin Fowler', 2018, 'Addison-Wesley', 'EN', 'Beschreibung Refactoring', 448),
       (1005, '9780321125217', 'Domain-Driven Design', 'Eric Evans', 2003, 'Addison-Wesley', 'EN', 'Beschreibung DDD', 560),
       (1006, '9780134494166', 'Clean Architecture', 'Robert C. Martin', 2017, 'Prentice Hall', 'EN', 'Beschreibung Clean Architecture', 432),
       (1007, '9780135957059', 'The Pragmatic Programmer', 'Andrew Hunt; David Thomas', 2019, 'Addison-Wesley', 'EN', 'Beschreibung Pragmatic Programmer', 352),
       (1008, '9780596007126', 'Head First Design Patterns', 'Eric Freeman; Elisabeth Robson', 2004, 'O''Reilly Media', 'EN', 'Beschreibung HFDP', 694),
       (1009, '9780321349606', 'Java Concurrency in Practice', 'Brian Goetz; et al.', 2006, 'Addison-Wesley', 'EN', 'Beschreibung JCIP', 384),
       (1010, '9781617294945', 'Spring in Action', 'Craig Walls', 2018, 'Manning', 'EN', 'Beschreibung Spring in Action', 520);

-- loans
-- Einige aktive Ausleihen (returnedAt IS NULL) + eine zurückgegebene
INSERT INTO loans (id, user_id, book_id, borrowed_at, returned_at)
VALUES (1, 1, 1001, DATE_SUB(NOW(6), INTERVAL 5 DAY), NULL),                               -- aktiv: demo_user_1 -> Clean Code
       (2, 2, 1003, DATE_SUB(NOW(6), INTERVAL 2 DAY), NULL),                               -- aktiv: demo_user_2 -> Design Patterns
       (3, 1, 1005, DATE_SUB(NOW(6), INTERVAL 20 DAY), DATE_SUB(NOW(6), INTERVAL 10 DAY)), -- zurückgegeben
       (4, 3, 1008, DATE_SUB(NOW(6), INTERVAL 1 DAY), NULL); -- aktiv: demo_user_3 -> HFDP