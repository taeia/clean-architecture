package library.application.listcopies;

/**
 * The output boundary of the use case, implemented by a presenter on the outside: one
 * that writes lines of text, one that fills rows of a table, or a test that remembers.
 */
public interface ListCopiesOutput {

    void present(ListCopiesResponse response);
}
