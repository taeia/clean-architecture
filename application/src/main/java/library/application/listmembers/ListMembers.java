package library.application.listmembers;

import java.util.ArrayList;
import java.util.List;

import library.domain.MemberRepository;
import library.model.Member;

/**
 * List the members. A use case of the asking kind: the name, how many copies each
 * member has on loan, and what they owe, one {@link MemberSummary} per member, and
 * nothing in the library changes.
 */
public class ListMembers implements ListMembersInput {

    private final MemberRepository members;

    public ListMembers(MemberRepository members) {
        this.members = members;
    }

    @Override
    public void execute(ListMembersOutput output) {
        List<MemberSummary> result = new ArrayList<>();
        for (Member member : members.all()) {
            result.add(new MemberSummary(member.name(), member.loans().size(), member.debt()));
        }
        output.present(new ListMembersResponse(result));
    }
}
