package library.presenters;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import library.application.borrowcopy.BorrowCopyOutput;
import library.application.borrowcopy.BorrowCopyResponse;
import library.application.listcopies.CopySummary;
import library.application.listcopies.ListCopiesOutput;
import library.application.listcopies.ListCopiesResponse;
import library.application.listmembers.ListMembersOutput;
import library.application.listmembers.ListMembersResponse;
import library.application.listmembers.MemberSummary;
import library.application.listreservations.ListReservationsOutput;
import library.application.listreservations.ListReservationsResponse;
import library.application.listreservations.ReservationSummary;
import library.application.payfine.PayFineOutput;
import library.application.payfine.PayFineResponse;
import library.application.renewloan.RenewLoanOutput;
import library.application.renewloan.RenewLoanResponse;
import library.application.reservebook.ReserveBookOutput;
import library.application.reservebook.ReserveBookResponse;
import library.application.returncopy.ReturnCopyOutput;
import library.application.returncopy.ReturnCopyResponse;

/**
 * The library in words. A presenter: it implements the output boundaries of the five
 * commands and of three listings, and turns each response into the sentence of
 * Chapter 0, "John borrowed C1 of The Hobbit. It is due on 16 March.", or into the
 * lines of a listing, one per row, joined with newlines and with no newline at the
 * end. The sentence that Chapters 8 and 9 built in a dialog, on the command line, and
 * in the endpoint is built here, once; the three of them now hold one of these and
 * read {@link #text()} after the use case has run. The view model is a string, kept
 * from one call to the next: a presenter fills it, a view shows it, and neither needs
 * the other to know how.
 *
 * Dates are written as "16 March" here and nowhere else. The books listing has no
 * text form, because no driver prints it.
 */
public class TextPresenter implements BorrowCopyOutput, ReturnCopyOutput, RenewLoanOutput,
        ReserveBookOutput, PayFineOutput, ListCopiesOutput, ListMembersOutput, ListReservationsOutput {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);

    private String text = "";

    /**
     * What the last use case had to say, as text.
     */
    public String text() {
        return text;
    }

    // ---- the five sentences ----

    @Override
    public void present(BorrowCopyResponse response) {
        text = response.member() + " borrowed " + response.copy() + " of " + response.book()
                + ". It is due on " + response.dueDate().format(DAY) + ".";
    }

    @Override
    public void present(ReturnCopyResponse response) {
        String result;
        if (response.daysLate() > 0) {
            result = response.member() + " returned " + response.copy() + " " + response.daysLate()
                    + " days late and now owes " + response.debt() + ".";
        } else {
            result = response.member() + " returned " + response.copy() + " on time.";
        }
        if (response.heldFor() != null) {
            result += " " + response.copy() + " is on hold for " + response.heldFor() + ", who has been notified.";
        }
        text = result;
    }

    @Override
    public void present(RenewLoanResponse response) {
        text = response.member() + " renewed the loan of " + response.copy()
                + ". It is now due on " + response.dueDate().format(DAY) + ".";
    }

    @Override
    public void present(ReserveBookResponse response) {
        text = response.member() + " reserved " + response.book()
                + ". Position in the queue: " + response.position() + ".";
    }

    @Override
    public void present(PayFineResponse response) {
        text = response.member() + " paid " + response.paid() + " and owes nothing now.";
    }

    // ---- the three listings ----

    @Override
    public void present(ListCopiesResponse response) {
        StringBuilder result = new StringBuilder();
        for (CopySummary copy : response.copies()) {
            result.append(copy.number()).append("  ").append(copy.book()).append(": ");
            switch (copy.status()) {
                case ON_HOLD -> result.append("on hold for ").append(copy.heldFor());
                case ON_LOAN -> result.append("on loan to ").append(copy.borrower())
                        .append(", due ").append(copy.dueDate().format(DAY));
                case AVAILABLE -> result.append("available");
            }
            result.append('\n');
        }
        text = result.toString().stripTrailing();
    }

    @Override
    public void present(ListMembersResponse response) {
        StringBuilder result = new StringBuilder();
        for (MemberSummary member : response.members()) {
            result.append(member.name()).append(": ").append(member.loans())
                    .append(member.loans() == 1 ? " loan" : " loans")
                    .append(", owes ").append(member.debt()).append('\n');
        }
        text = result.toString().stripTrailing();
    }

    @Override
    public void present(ListReservationsResponse response) {
        if (response.reservations().isEmpty()) {
            text = "No reservations.";
            return;
        }
        StringBuilder result = new StringBuilder();
        for (ReservationSummary reservation : response.reservations()) {
            result.append(reservation.book()).append(": ").append(reservation.member())
                    .append(", position ").append(reservation.position()).append('\n');
        }
        text = result.toString().stripTrailing();
    }
}
