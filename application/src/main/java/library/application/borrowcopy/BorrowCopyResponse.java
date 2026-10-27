package library.application.borrowcopy;

import java.time.LocalDate;

/**
 * The response model: what the use case has to say once a copy was borrowed. This is
 * Chapter 8's {@code Borrowed}, with the member and the copy added. Chapter 8 left them
 * out because the caller had just said them; here the presenter is the one that builds
 * the sentence, and it has seen the request no more than it has seen the aggregates,
 * so the response carries everything the sentence needs. The due date is a date, not
 * text: how it is written is the presenter's decision.
 */
public record BorrowCopyResponse(String member, String copy, String book, LocalDate dueDate) {
}
