package library.application.reservebook;

/**
 * The request model: a member's name and a book's title. The model identifies a book by
 * its ISBN; the people at the desk, and so the drivers, name it by its title.
 */
public record ReserveBookRequest(String member, String book) {
}
