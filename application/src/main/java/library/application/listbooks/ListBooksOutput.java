package library.application.listbooks;

/**
 * The output boundary of the use case, implemented by a presenter on the outside.
 */
public interface ListBooksOutput {

    void present(ListBooksResponse response);
}
