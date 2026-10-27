package library.application.returncopy;

/**
 * The request model: the number of the copy that came back. One field, and still a
 * record of its own, because the boundary is explicit whether it has much to carry or
 * not.
 */
public record ReturnCopyRequest(String copy) {
}
