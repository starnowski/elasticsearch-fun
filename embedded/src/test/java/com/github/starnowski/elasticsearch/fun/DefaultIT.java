package com.github.starnowski.elasticsearch.fun;

import com.github.starnowski.elasticsearch.fun.model.Document;
import com.madadipouya.elasticsearch.springdata.DocumentElasticsearchContainer;
import com.madadipouya.elasticsearch.springdata.example.service.DocumentService;
import com.madadipouya.elasticsearch.springdata.example.service.exception.DocumentNotFoundException;
import com.madadipouya.elasticsearch.springdata.example.service.exception.DuplicateIsbnException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DefaultIT {

    @Autowired
    ElasticsearchTemplate template;

    @Container
    private static ElasticsearchContainer elasticsearchContainer = new DocumentElasticsearchContainer();

    @BeforeAll
    static void setUp() {
        elasticsearchContainer.start();
    }

    @BeforeEach
    void testIsContainerRunning() {
        assertTrue(elasticsearchContainer.isRunning());
        recreateIndex();
    }

    @Test
    void testGetAllDocuments() throws DuplicateIsbnException {
        bookService.create(createDocument("12 rules for life", "Jordan Peterson", 2018, "978-0345816023"));
        bookService.create(createDocument("The Cathedral and the Bazaar", "Eric Raymond", 1999, "9780596106386"));
        List<Document> books = bookService.getAll();

        assertNotNull(books);
        assertEquals(2, books.size());
    }

    @Test
    void testFindByTitleAndAuthor() throws DuplicateIsbnException {
        bookService.create(createDocument("12 rules for life", "Jordan Peterson", 2018, "978-0345816023"));
        bookService.create(createDocument("Rules or not rules?", "Jordan Miller", 2010, "978128000000"));
        bookService.create(createDocument("Poor economy", "Jordan Miller", 2006, "9781280789000"));
        bookService.create(createDocument("The Cathedral and the Bazaar", "Eric Raymond", 1999, "9780596106386"));

        List<Document> books = bookService.findByTitleAndAuthor("rules", "jordan");

        assertNotNull(books);
        assertEquals(2, books.size());
    }

    @Test
    void testCreateDocument() throws DuplicateIsbnException {
        Document createdDocument = bookService.create(createDocument("12 rules for life", "Jordan Peterson", 2018, "978-0345816023"));
        assertNotNull(createdDocument);
        assertNotNull(createdDocument.getId());
        assertEquals("12 rules for life", createdDocument.getTitle());
        assertEquals("Jordan Peterson", createdDocument.getAuthorName());
        assertEquals(2018, createdDocument.getPublicationYear());
        assertEquals("978-0345816023", createdDocument.getIsbn());
    }

    private Document createDocument(String title, String authorName, int publicationYear, String isbn) {
        Document book = new Document();
        book.setTitle(title);
        book.setAuthorName(authorName);
        book.setPublicationYear(publicationYear);
        book.setIsbn(isbn);
        return book;
    }

    private void recreateIndex() {
        if (template.indexExists(Document.class)) {
            template.deleteIndex(Document.class);
            template.createIndex(Document.class);
        }
    }

    @AfterAll
    static void destroy() {
        elasticsearchContainer.stop();
    }
}