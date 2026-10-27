package library.domain;

import java.util.List;

import library.model.Book;
import library.model.Queue;

/**
 * The queues of the library, one per book, stored whole with their reservations. A
 * driven port.
 */
public interface QueueRepository {

    /**
     * The queue for the book. Every book has one; a book nobody has reserved has an
     * empty queue.
     */
    Queue of(Book book);

    List<Queue> all();

    void save(Queue queue);
}
