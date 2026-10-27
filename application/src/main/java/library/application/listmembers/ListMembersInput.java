package library.application.listmembers;

/**
 * The input boundary of the use case. No request model: a listing asks for nothing.
 */
public interface ListMembersInput {

    void execute(ListMembersOutput output);
}
