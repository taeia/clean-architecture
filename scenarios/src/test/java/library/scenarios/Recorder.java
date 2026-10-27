package library.scenarios;

import library.application.borrowcopy.BorrowCopyOutput;
import library.application.borrowcopy.BorrowCopyResponse;
import library.application.listbooks.ListBooksOutput;
import library.application.listbooks.ListBooksResponse;
import library.application.listcopies.ListCopiesOutput;
import library.application.listcopies.ListCopiesResponse;
import library.application.listmembers.ListMembersOutput;
import library.application.listmembers.ListMembersResponse;
import library.application.listreservations.ListReservationsOutput;
import library.application.listreservations.ListReservationsResponse;
import library.application.payfine.PayFineOutput;
import library.application.payfine.PayFineResponse;
import library.application.renewloan.RenewLoanOutput;
import library.application.renewloan.RenewLoanResponse;
import library.application.reservebook.ReserveBookOutput;
import library.application.reservebook.ReserveBookResponse;
import library.application.returncopy.ReturnCopyOutput;
import library.application.returncopy.ReturnCopyResponse;

/**
 * A presenter that remembers. The scenarios are a driver, and a driver needs a
 * presenter on the output side of every use case it runs; this one implements all
 * nine output boundaries and keeps the last response of each kind, so that a test can
 * read a due date or a debt back as a value and compare it. It is the view model of a
 * test: not text, not rows, but the response itself. It lives with the scenarios
 * because no program shows the library this way.
 */
class Recorder implements BorrowCopyOutput, ReturnCopyOutput, RenewLoanOutput, ReserveBookOutput,
        PayFineOutput, ListCopiesOutput, ListMembersOutput, ListBooksOutput, ListReservationsOutput {

    BorrowCopyResponse borrowed;
    ReturnCopyResponse returned;
    RenewLoanResponse renewed;
    ReserveBookResponse reserved;
    PayFineResponse paid;
    ListCopiesResponse copies;
    ListMembersResponse members;
    ListBooksResponse books;
    ListReservationsResponse reservations;

    @Override
    public void present(BorrowCopyResponse response) {
        borrowed = response;
    }

    @Override
    public void present(ReturnCopyResponse response) {
        returned = response;
    }

    @Override
    public void present(RenewLoanResponse response) {
        renewed = response;
    }

    @Override
    public void present(ReserveBookResponse response) {
        reserved = response;
    }

    @Override
    public void present(PayFineResponse response) {
        paid = response;
    }

    @Override
    public void present(ListCopiesResponse response) {
        copies = response;
    }

    @Override
    public void present(ListMembersResponse response) {
        members = response;
    }

    @Override
    public void present(ListBooksResponse response) {
        books = response;
    }

    @Override
    public void present(ListReservationsResponse response) {
        reservations = response;
    }
}
