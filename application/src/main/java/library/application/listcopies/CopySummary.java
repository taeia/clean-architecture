package library.application.listcopies;

import java.time.LocalDate;

/**
 * One copy as a listing shows it. The status is decided by the use case, so that no
 * presenter has to know what "available" means; the borrower and the due date are set
 * only when the copy is on loan, and the member it is held for only when it is on
 * hold. A record with no behaviour: holding one changes nothing. Chapter 8's record,
 * unchanged: it was a response model before the book had the name.
 */
public record CopySummary(String number, String book, CopyStatus status,
                          String borrower, LocalDate dueDate, String heldFor) {
}
