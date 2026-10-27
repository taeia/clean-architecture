package library.application.listreservations;

import java.util.List;

/**
 * The response model: the reservations, one summary each, in queue order.
 */
public record ListReservationsResponse(List<ReservationSummary> reservations) {
}
