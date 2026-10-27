package library.application.renewloan;

/**
 * The output boundary of the use case, implemented by a presenter on the outside.
 */
public interface RenewLoanOutput {

    void present(RenewLoanResponse response);
}
