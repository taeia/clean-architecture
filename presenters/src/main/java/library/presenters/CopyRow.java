package library.presenters;

/**
 * One copy as a table shows it: five strings, ready to be put in five cells. The view
 * model of a copy. The status is already words, and the due date is already "16 March"
 * or empty: nothing is left for the column to decide.
 */
public record CopyRow(String number, String book, String status, String borrower, String due) {
}
