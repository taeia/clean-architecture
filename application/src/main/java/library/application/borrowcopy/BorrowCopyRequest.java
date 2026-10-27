package library.application.borrowcopy;

/**
 * The request model: what a driver has to say to borrow a copy. A member's name and a
 * copy's number, which is what a line of text, a dialog, or a URL can carry, and
 * nothing else. In Chapter 9 these were the two parameters of a port method; here
 * they are a record of their own, so that the thing that crosses the boundary has a
 * name and a file.
 */
public record BorrowCopyRequest(String member, String copy) {
}
