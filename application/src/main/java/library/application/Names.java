package library.application;

import library.domain.BookRepository;
import library.domain.CopyRepository;
import library.domain.MemberRepository;
import library.model.Book;
import library.model.Copy;
import library.model.Loan;
import library.model.Member;

/**
 * From the names the drivers use to the aggregates behind them. A request model carries
 * a member's name, a copy's number, or a book's title, which is what a line of text, a
 * dialog, or a URL can carry, and the use case has to find the aggregate behind each
 * one; a name nobody has is a refusal. Three use cases look up a member, three a copy,
 * one a book, and two the loan of a copy, so the four lookups are written here once.
 * This is a helper and not a use case: it has no state, and it belongs to no package
 * below this one because several of them need it.
 */
public final class Names {

    private Names() {
    }

    public static Member member(MemberRepository members, String name) {
        Member member = members.named(name);
        if (member == null) {
            throw new Refusal("There is no member called " + name + ".");
        }
        return member;
    }

    public static Copy copy(CopyRepository copies, String number) {
        Copy copy = copies.numbered(number);
        if (copy == null) {
            throw new Refusal("There is no copy " + number + ".");
        }
        return copy;
    }

    public static Book book(BookRepository books, String title) {
        Book book = books.titled(title);
        if (book == null) {
            throw new Refusal("There is no book called " + title + ".");
        }
        return book;
    }

    /**
     * Loans live inside members, so the question is put to each member in turn.
     */
    public static Loan loanOf(MemberRepository members, Copy copy) {
        for (Member member : members.all()) {
            Loan loan = member.loanOf(copy);
            if (loan != null) {
                return loan;
            }
        }
        throw new Refusal(copy.number() + " is not on loan.");
    }
}
