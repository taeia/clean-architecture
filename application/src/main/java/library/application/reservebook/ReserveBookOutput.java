package library.application.reservebook;

/**
 * The output boundary of the use case, implemented by a presenter on the outside.
 */
public interface ReserveBookOutput {

    void present(ReserveBookResponse response);
}
