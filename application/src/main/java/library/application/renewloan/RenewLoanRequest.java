package library.application.renewloan;

/**
 * The request model: the number of the copy whose loan is renewed. The library has no
 * loan numbers; a loan at the desk is a copy that is on loan.
 */
public record RenewLoanRequest(String copy) {
}
