package library.application.payfine;

/**
 * The request model: the name of the member who pays. There is no amount, because a
 * member pays the whole debt; Chapter 0 has no partial payments.
 */
public record PayFineRequest(String member) {
}
