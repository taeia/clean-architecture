package library.domain;

import java.util.List;

import library.model.Member;

/**
 * The members of the library, with their loans. A driven port: the inside says what it
 * needs from storage, in its own terms, and an adapter on the outer ring provides it.
 * Evans calls this a repository, and it is one; the hexagon calls it a port, because
 * from the hexagon's edge a repository, a clock, and a mailbox are the same kind of
 * thing. The onion says which ring declares it: this one, one ring out from the model,
 * beside the domain services that call it, which is where Palermo draws the repository
 * interfaces. The model cannot name it, because the model's pom lists nothing. A member
 * is stored whole, loans included.
 */
public interface MemberRepository {

    List<Member> all();

    /**
     * The member with that name, or null if there is none. The drivers name members
     * by name, so the inside has to find them by it.
     */
    Member named(String name);

    void save(Member member);
}
