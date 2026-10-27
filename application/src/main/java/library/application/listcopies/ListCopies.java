package library.application.listcopies;

import java.util.ArrayList;
import java.util.List;

import library.domain.CopyRepository;
import library.domain.MemberRepository;
import library.model.Copy;
import library.model.Loan;
import library.model.Member;

/**
 * List the copies. A use case of the asking kind: it changes nothing, rings no signal,
 * and may be run as often as a driver likes, which is what the window does whenever
 * the change signal rings. It reads the aggregates through the same repositories the
 * commands use, because there is only one model, and copies what a listing needs out
 * of them into {@link CopySummary} records, one per copy. Chapter 8's
 * {@code BrowsingService.copies}, as a class.
 */
public class ListCopies implements ListCopiesInput {

    private final CopyRepository copies;
    private final MemberRepository members;

    public ListCopies(CopyRepository copies, MemberRepository members) {
        this.copies = copies;
        this.members = members;
    }

    @Override
    public void execute(ListCopiesOutput output) {
        List<CopySummary> result = new ArrayList<>();
        for (Copy copy : copies.all()) {
            result.add(summarise(copy));
        }
        output.present(new ListCopiesResponse(result));
    }

    /**
     * The copy decides whether it is on hold or not; whether it is on loan is a fact
     * about some member, so the members are asked.
     */
    private CopySummary summarise(Copy copy) {
        String title = copy.book().title();
        if (copy.isOnHold()) {
            return new CopySummary(copy.number(), title, CopyStatus.ON_HOLD, null, null, copy.heldFor().name());
        }
        Loan loan = loanOf(copy);
        if (loan != null) {
            return new CopySummary(copy.number(), title, CopyStatus.ON_LOAN,
                    loan.member().name(), loan.dueDate(), null);
        }
        return new CopySummary(copy.number(), title, CopyStatus.AVAILABLE, null, null, null);
    }

    private Loan loanOf(Copy copy) {
        for (Member member : members.all()) {
            Loan loan = member.loanOf(copy);
            if (loan != null) {
                return loan;
            }
        }
        return null;
    }
}
