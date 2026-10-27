package library.application.listreservations;

/**
 * The output boundary of the use case, implemented by a presenter on the outside.
 */
public interface ListReservationsOutput {

    void present(ListReservationsResponse response);
}
