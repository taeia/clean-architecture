package library.application.renewloan;

import library.application.ChangeSignal;
import library.application.Names;
import library.application.Refusal;
import library.domain.CopyRepository;
import library.domain.Lending;
import library.domain.MemberRepository;
import library.model.Copy;
import library.model.LibraryException;
import library.model.Loan;

/**
 * Renew a loan. A use case, and the class behind the {@link RenewLoanInput} boundary.
 * It finds the loan behind the copy's number, asks {@link Lending} to renew it and
 * turns the model's refusal, a reservation, an overdue loan, a debt, into a
 * {@link Refusal}, rings the {@link ChangeSignal}, and presents whose loan it was and
 * the new due date.
 */
public class RenewLoan implements RenewLoanInput {

    private final Lending lending;
    private final MemberRepository members;
    private final CopyRepository copies;
    private final ChangeSignal signal;

    public RenewLoan(Lending lending, MemberRepository members, CopyRepository copies, ChangeSignal signal) {
        this.lending = lending;
        this.members = members;
        this.copies = copies;
        this.signal = signal;
    }

    @Override
    public void execute(RenewLoanRequest request, RenewLoanOutput output) {
        Copy copy = Names.copy(copies, request.copy());
        Loan loan = Names.loanOf(members, copy);
        try {
            lending.renew(loan);
        } catch (LibraryException e) {
            throw new Refusal(e.getMessage());
        }
        signal.changed();
        output.present(new RenewLoanResponse(loan.member().name(), copy.number(), loan.dueDate()));
    }
}
