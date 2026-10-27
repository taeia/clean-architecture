package library.application.renewloan;

import java.time.LocalDate;

/**
 * The response model: whose loan it was, of which copy, and the new due date.
 * Chapter 8's {@code Renewed}, with the copy added.
 */
public record RenewLoanResponse(String member, String copy, LocalDate dueDate) {
}
