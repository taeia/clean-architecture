package library.application.renewloan;

/**
 * The input boundary of the use case: a driver names the copy whose loan is renewed,
 * and hands in the presenter.
 */
public interface RenewLoanInput {

    void execute(RenewLoanRequest request, RenewLoanOutput output);
}
