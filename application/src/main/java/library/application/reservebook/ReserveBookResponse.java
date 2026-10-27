package library.application.reservebook;

/**
 * The response model: who reserved what, and the position of the new reservation in
 * the queue, counting from 1. Chapter 8 answered with the position alone.
 */
public record ReserveBookResponse(String member, String book, int position) {
}
