package library.application.listcopies;

/**
 * The input boundary of the use case. A listing has no request model: there is nothing
 * to say but "the copies, please", and the method name says it.
 */
public interface ListCopiesInput {

    void execute(ListCopiesOutput output);
}
