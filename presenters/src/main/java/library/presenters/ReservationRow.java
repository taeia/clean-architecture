package library.presenters;

/**
 * One reservation as a table shows it: book, member, position, all three as text.
 * The view model of a reservation.
 */
public record ReservationRow(String book, String member, String position) {
}
