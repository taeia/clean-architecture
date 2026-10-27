package library.application.returncopy;

/**
 * The output boundary of the use case, implemented by a presenter on the outside.
 */
public interface ReturnCopyOutput {

    void present(ReturnCopyResponse response);
}
