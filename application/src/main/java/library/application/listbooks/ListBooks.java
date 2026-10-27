package library.application.listbooks;

import java.util.ArrayList;
import java.util.List;

import library.domain.BookRepository;
import library.model.Book;

/**
 * List the books. A use case of the asking kind, run by the window to fill the
 * Reserve dialog's list of titles; no other driver asks.
 */
public class ListBooks implements ListBooksInput {

    private final BookRepository books;

    public ListBooks(BookRepository books) {
        this.books = books;
    }

    @Override
    public void execute(ListBooksOutput output) {
        List<BookSummary> result = new ArrayList<>();
        for (Book book : books.all()) {
            result.add(new BookSummary(book.isbn(), book.title()));
        }
        output.present(new ListBooksResponse(result));
    }
}
