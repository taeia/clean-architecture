package library.application;

/**
 * Thrown through an input boundary when the library refuses a command. The message says
 * why, in the words of Chapter 0: "C1 is on loan.", "John owes 1.50 EUR.", "There is no
 * member called Bob." At the centre the model throws {@code LibraryException}; the use
 * cases catch it and throw this instead, so that a driver can catch a refusal without
 * importing anything from the model. A refusal does not reach the output boundary: the
 * presenter sees responses, and a refused command has none.
 */
public class Refusal extends RuntimeException {

    public Refusal(String message) {
        super(message);
    }
}
