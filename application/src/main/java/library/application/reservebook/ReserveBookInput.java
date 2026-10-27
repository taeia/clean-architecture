package library.application.reservebook;

/**
 * The input boundary of the use case: a driver names the member and the book, and hands
 * in the presenter.
 */
public interface ReserveBookInput {

    void execute(ReserveBookRequest request, ReserveBookOutput output);
}
