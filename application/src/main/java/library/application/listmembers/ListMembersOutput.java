package library.application.listmembers;

/**
 * The output boundary of the use case, implemented by a presenter on the outside.
 */
public interface ListMembersOutput {

    void present(ListMembersResponse response);
}
