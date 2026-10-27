package library.application.returncopy;

import library.application.ChangeSignal;
import library.application.Names;
import library.application.Notifier;
import library.domain.CopyRepository;
import library.domain.Lending;
import library.domain.MemberRepository;
import library.model.Copy;
import library.model.Loan;
import library.model.Member;

/**
 * Return a copy. A use case, and the class behind the {@link ReturnCopyInput} boundary.
 * It finds the copy and the loan behind the number, asks {@link Lending} to take the
 * copy back, tells the member the copy is now held for, if there is one, through the
 * {@link Notifier} port, rings the {@link ChangeSignal}, and presents who returned
 * what, how late, what they owe now, and whom the copy waits for. The model refuses
 * nothing here: a copy that is not on loan is caught while looking for its loan.
 */
public class ReturnCopy implements ReturnCopyInput {

    private final Lending lending;
    private final MemberRepository members;
    private final CopyRepository copies;
    private final Notifier notifier;
    private final ChangeSignal signal;

    public ReturnCopy(Lending lending, MemberRepository members, CopyRepository copies,
                      Notifier notifier, ChangeSignal signal) {
        this.lending = lending;
        this.members = members;
        this.copies = copies;
        this.notifier = notifier;
        this.signal = signal;
    }

    @Override
    public void execute(ReturnCopyRequest request, ReturnCopyOutput output) {
        Copy copy = Names.copy(copies, request.copy());
        Loan loan = Names.loanOf(members, copy);
        Member member = loan.member();
        long daysLate = lending.returnCopy(loan);
        if (copy.isOnHold()) {
            notifier.send(copy.heldFor(), copy.book().title() + " is waiting for you.");
        }
        signal.changed();
        output.present(new ReturnCopyResponse(member.name(), copy.number(), daysLate, member.debt(),
                copy.isOnHold() ? copy.heldFor().name() : null));
    }
}
