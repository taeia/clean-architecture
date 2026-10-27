package library.domain;

import java.util.List;

import library.model.Book;
import library.model.Copy;

/**
 * The copies the library owns. A driven port.
 */
public interface CopyRepository {

    List<Copy> all();

    List<Copy> ofBook(Book book);

    /**
     * The copy with that number, or null if there is none.
     */
    Copy numbered(String number);

    void save(Copy copy);
}
