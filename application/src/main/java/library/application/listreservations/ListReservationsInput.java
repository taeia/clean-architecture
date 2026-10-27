package library.application.listreservations;

/**
 * The input boundary of the use case. No request model: a listing asks for nothing.
 */
public interface ListReservationsInput {

    void execute(ListReservationsOutput output);
}
