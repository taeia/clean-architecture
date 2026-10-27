package library.application.payfine;

import library.application.ChangeSignal;
import library.application.Names;
import library.application.Refusal;
import library.domain.MemberRepository;
import library.model.LibraryException;
import library.model.Member;
import library.model.Money;

/**
 * Pay a fine. A use case, and the class behind the {@link PayFineInput} boundary. It
 * finds the member by name, and the member knows how to pay; paying touches one
 * aggregate, so no domain service is needed, and this class saves the result, rings
 * the {@link ChangeSignal}, and presents what was paid.
 */
public class PayFine implements PayFineInput {

    private final MemberRepository members;
    private final ChangeSignal signal;

    public PayFine(MemberRepository members, ChangeSignal signal) {
        this.members = members;
        this.signal = signal;
    }

    @Override
    public void execute(PayFineRequest request, PayFineOutput output) {
        Member member = Names.member(members, request.member());
        Money paid;
        try {
            paid = member.pay();
        } catch (LibraryException e) {
            throw new Refusal(e.getMessage());
        }
        members.save(member);
        signal.changed();
        output.present(new PayFineResponse(member.name(), paid));
    }
}
