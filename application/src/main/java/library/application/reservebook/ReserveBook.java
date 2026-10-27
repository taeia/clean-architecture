package library.application.reservebook;

import library.application.ChangeSignal;
import library.application.Names;
import library.application.Refusal;
import library.domain.BookRepository;
import library.domain.MemberRepository;
import library.domain.QueueRepository;
import library.domain.Reserving;
import library.model.Book;
import library.model.LibraryException;
import library.model.Member;
import library.model.Reservation;

/**
 * Reserve a book. A use case, and the class behind the {@link ReserveBookInput}
 * boundary. It finds the member and the book by name, asks {@link Reserving} to do the
 * work and turns a refusal into a {@link Refusal}, rings the {@link ChangeSignal}, and
 * presents the position of the new reservation in the queue.
 */
public class ReserveBook implements ReserveBookInput {

    private final Reserving reserving;
    private final BookRepository books;
    private final MemberRepository members;
    private final QueueRepository queues;
    private final ChangeSignal signal;

    public ReserveBook(Reserving reserving, BookRepository books, MemberRepository members,
                       QueueRepository queues, ChangeSignal signal) {
        this.reserving = reserving;
        this.books = books;
        this.members = members;
        this.queues = queues;
        this.signal = signal;
    }

    @Override
    public void execute(ReserveBookRequest request, ReserveBookOutput output) {
        Member member = Names.member(members, request.member());
        Book book = Names.book(books, request.book());
        Reservation reservation;
        try {
            reservation = reserving.reserve(member, book);
        } catch (LibraryException e) {
            throw new Refusal(e.getMessage());
        }
        signal.changed();
        output.present(new ReserveBookResponse(member.name(), book.title(), queues.of(book).positionOf(reservation)));
    }
}
