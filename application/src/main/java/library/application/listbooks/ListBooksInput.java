package library.application.listbooks;

/**
 * The input boundary of the use case. No request model: a listing asks for nothing.
 */
public interface ListBooksInput {

    void execute(ListBooksOutput output);
}
