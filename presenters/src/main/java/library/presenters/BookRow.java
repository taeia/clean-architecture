package library.presenters;

/**
 * One book as a list shows it. The view model of a book, and the same two strings as
 * the summary it was made from: the presenter had nothing to decide, and the record
 * exists so that the view sees view models and nothing else.
 */
public record BookRow(String isbn, String title) {
}
