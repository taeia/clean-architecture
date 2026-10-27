package library.application.listbooks;

/**
 * One book as a listing shows it. The title is what the drivers name a book by.
 */
public record BookSummary(String isbn, String title) {
}
