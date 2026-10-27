package library.application.payfine;

/**
 * The output boundary of the use case, implemented by a presenter on the outside.
 */
public interface PayFineOutput {

    void present(PayFineResponse response);
}
