package library.application.payfine;

/**
 * The input boundary of the use case: a driver names the member who pays, and hands in
 * the presenter.
 */
public interface PayFineInput {

    void execute(PayFineRequest request, PayFineOutput output);
}
