package library.application.returncopy;

/**
 * The input boundary of the use case: a driver names the copy, and hands in the
 * presenter that will be told what happened.
 */
public interface ReturnCopyInput {

    void execute(ReturnCopyRequest request, ReturnCopyOutput output);
}
