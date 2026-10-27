package library.application.borrowcopy;

/**
 * The input boundary of the use case: what a driver holds, and the one method it can
 * call. A driving port of exactly one command, where Chapter 9's
 * {@code ForLendingCopies} had three. The request says which member and which copy;
 * the output is the presenter that will be told what happened, handed in with each
 * call, because the window and the endpoint share one use case and show its answer in
 * different places. A refused command ends in a {@code Refusal} and the output hears
 * nothing.
 */
public interface BorrowCopyInput {

    void execute(BorrowCopyRequest request, BorrowCopyOutput output);
}
