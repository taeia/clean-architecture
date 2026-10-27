package library.domain;

import java.util.List;

import library.model.Book;

/**
 * The books the library holds. A driven port.
 */
public interface BookRepository {

    List<Book> all();

    /**
     * The book with that title, or null if there is none. The model identifies a book
     * by its ISBN; the people at the desk, and so the drivers, name it by its title.
     */
    Book titled(String title);

    void save(Book book);
}
