package library.application.borrowcopy;

/**
 * The output boundary of the use case: what the use case calls when it is done, and
 * what a presenter on the outside implements. Declared in this ring, beside the use
 * case that calls it, for an outer ring to implement: the use case does not know what
 * a sentence, a status bar, or a response body is, and this interface is why it does
 * not have to.
 */
public interface BorrowCopyOutput {

    void present(BorrowCopyResponse response);
}
