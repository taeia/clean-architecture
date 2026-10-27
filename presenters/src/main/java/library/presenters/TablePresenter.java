package library.presenters;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import library.application.listbooks.BookSummary;
import library.application.listbooks.ListBooksOutput;
import library.application.listbooks.ListBooksResponse;
import library.application.listcopies.CopyStatus;
import library.application.listcopies.CopySummary;
import library.application.listcopies.ListCopiesOutput;
import library.application.listcopies.ListCopiesResponse;
import library.application.listmembers.ListMembersOutput;
import library.application.listmembers.ListMembersResponse;
import library.application.listmembers.MemberSummary;
import library.application.listreservations.ListReservationsOutput;
import library.application.listreservations.ListReservationsResponse;
import library.application.listreservations.ReservationSummary;

/**
 * The library in rows. A presenter for a screen with tables and lists: it implements
 * the output boundaries of the four listings and turns each response into rows of
 * strings, {@link CopyRow}, {@link MemberRow}, {@link BookRow}, {@link ReservationRow},
 * with every decision already made. The status of a copy is "available", "on loan",
 * or "on hold for Mary"; the due date is "16 March" or nothing; a count is digits. A
 * table column shows a field and decides nothing, which is what Martin asks of a
 * view. The rows are kept from one call to the next, and the window reads them after
 * each listing has run. {@link #loans()} is the copies on loan, picked out here for
 * the Return and Renew dialogs, because which copies are on loan is a decision too.
 */
public class TablePresenter implements ListCopiesOutput, ListMembersOutput, ListBooksOutput, ListReservationsOutput {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);

    private List<CopyRow> copies = List.of();
    private List<CopyRow> loans = List.of();
    private List<MemberRow> members = List.of();
    private List<BookRow> books = List.of();
    private List<ReservationRow> reservations = List.of();

    public List<CopyRow> copies() {
        return copies;
    }

    public List<CopyRow> loans() {
        return loans;
    }

    public List<MemberRow> members() {
        return members;
    }

    public List<BookRow> books() {
        return books;
    }

    public List<ReservationRow> reservations() {
        return reservations;
    }

    @Override
    public void present(ListCopiesResponse response) {
        List<CopyRow> rows = new ArrayList<>();
        List<CopyRow> onLoan = new ArrayList<>();
        for (CopySummary copy : response.copies()) {
            CopyRow row = new CopyRow(copy.number(), copy.book(), status(copy),
                    copy.borrower() == null ? "" : copy.borrower(),
                    copy.dueDate() == null ? "" : copy.dueDate().format(DAY));
            rows.add(row);
            if (copy.status() == CopyStatus.ON_LOAN) {
                onLoan.add(row);
            }
        }
        copies = rows;
        loans = onLoan;
    }

    private static String status(CopySummary copy) {
        return switch (copy.status()) {
            case AVAILABLE -> "available";
            case ON_LOAN -> "on loan";
            case ON_HOLD -> "on hold for " + copy.heldFor();
        };
    }

    @Override
    public void present(ListMembersResponse response) {
        List<MemberRow> rows = new ArrayList<>();
        for (MemberSummary member : response.members()) {
            rows.add(new MemberRow(member.name(), String.valueOf(member.loans()), member.debt().toString()));
        }
        members = rows;
    }

    @Override
    public void present(ListBooksResponse response) {
        List<BookRow> rows = new ArrayList<>();
        for (BookSummary book : response.books()) {
            rows.add(new BookRow(book.isbn(), book.title()));
        }
        books = rows;
    }

    @Override
    public void present(ListReservationsResponse response) {
        List<ReservationRow> rows = new ArrayList<>();
        for (ReservationSummary reservation : response.reservations()) {
            rows.add(new ReservationRow(reservation.book(), reservation.member(), String.valueOf(reservation.position())));
        }
        reservations = rows;
    }
}
