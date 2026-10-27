package library.application.listreservations;

import java.util.ArrayList;
import java.util.List;

import library.domain.QueueRepository;
import library.model.Queue;
import library.model.Reservation;

/**
 * List the reservations. A use case of the asking kind: every reservation of every
 * queue, with its position, one {@link ReservationSummary} each.
 */
public class ListReservations implements ListReservationsInput {

    private final QueueRepository queues;

    public ListReservations(QueueRepository queues) {
        this.queues = queues;
    }

    @Override
    public void execute(ListReservationsOutput output) {
        List<ReservationSummary> result = new ArrayList<>();
        for (Queue queue : queues.all()) {
            for (Reservation reservation : queue.reservations()) {
                result.add(new ReservationSummary(queue.book().title(), reservation.member().name(),
                        queue.positionOf(reservation)));
            }
        }
        output.present(new ListReservationsResponse(result));
    }
}
