package library.scenarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import library.application.Refusal;
import library.application.borrowcopy.BorrowCopy;
import library.application.borrowcopy.BorrowCopyInput;
import library.application.borrowcopy.BorrowCopyRequest;
import library.application.borrowcopy.BorrowCopyResponse;
import library.application.listcopies.CopyStatus;
import library.application.listcopies.CopySummary;
import library.application.listcopies.ListCopies;
import library.application.listcopies.ListCopiesInput;
import library.application.listmembers.ListMembers;
import library.application.listmembers.ListMembersInput;
import library.application.listmembers.MemberSummary;
import library.application.listreservations.ListReservations;
import library.application.listreservations.ListReservationsInput;
import library.application.listreservations.ReservationSummary;
import library.application.payfine.PayFine;
import library.application.payfine.PayFineInput;
import library.application.payfine.PayFineRequest;
import library.application.renewloan.RenewLoan;
import library.application.renewloan.RenewLoanInput;
import library.application.renewloan.RenewLoanRequest;
import library.application.renewloan.RenewLoanResponse;
import library.application.reservebook.ReserveBook;
import library.application.reservebook.ReserveBookInput;
import library.application.reservebook.ReserveBookRequest;
import library.application.returncopy.ReturnCopy;
import library.application.returncopy.ReturnCopyInput;
import library.application.returncopy.ReturnCopyRequest;
import library.application.returncopy.ReturnCopyResponse;
import library.domain.Lending;
import library.domain.Reserving;
import library.inmemory.InMemoryBookRepository;
import library.inmemory.InMemoryCopyRepository;
import library.inmemory.InMemoryMemberRepository;
import library.inmemory.InMemoryNotifier;
import library.inmemory.InMemoryQueueRepository;
import library.inmemory.SettableClock;
import library.inmemory.StartingState;
import library.model.Money;

/**
 * One test per scenario of Chapter 0, with the same name. A driving adapter: the test
 * holds the use cases by their input boundaries, as the window and the command line
 * do, and brings its own presenter, a {@link Recorder} that keeps each response so
 * that an assertion can read a date or an amount out of it. Its setup is the
 * configurator for this driver. It names members, copies, and books the way Chapter 0
 * does, John, C1, The Hobbit, and reads the library back through the listing use
 * cases, so no test can reach an aggregate, which is the point: a scenario checks
 * what the library says, not what its objects contain. The helpers at the end are
 * what the test bodies call, so that a body reads as it did in Chapter 9.
 */
class ScenarioTest {

    private final InMemoryBookRepository bookRepository = new InMemoryBookRepository();
    private final InMemoryCopyRepository copyRepository = new InMemoryCopyRepository();
    private final InMemoryMemberRepository memberRepository = new InMemoryMemberRepository();
    private final InMemoryQueueRepository queueRepository = new InMemoryQueueRepository();
    private final InMemoryNotifier notifier = new InMemoryNotifier();
    private final SettableClock clock = new SettableClock();
    private final Recorder recorder = new Recorder();
    private BorrowCopyInput borrowCopy;
    private ReturnCopyInput returnCopy;
    private RenewLoanInput renewLoan;
    private ReserveBookInput reserveBook;
    private PayFineInput payFine;
    private ListCopiesInput listCopies;
    private ListMembersInput listMembers;
    private ListReservationsInput listReservations;

    @BeforeEach
    void startingState() {
        StartingState.load(bookRepository, copyRepository, memberRepository, queueRepository);
        notifier.clear();
        Lending lending = new Lending(memberRepository, copyRepository, queueRepository, clock);
        Reserving reserving = new Reserving(copyRepository, queueRepository);
        borrowCopy = new BorrowCopy(lending, memberRepository, copyRepository, () -> {});
        returnCopy = new ReturnCopy(lending, memberRepository, copyRepository, notifier, () -> {});
        renewLoan = new RenewLoan(lending, memberRepository, copyRepository, () -> {});
        reserveBook = new ReserveBook(reserving, bookRepository, memberRepository, queueRepository, () -> {});
        payFine = new PayFine(memberRepository, () -> {});
        listCopies = new ListCopies(copyRepository, memberRepository);
        listMembers = new ListMembers(memberRepository);
        listReservations = new ListReservations(queueRepository);
    }

    @Test
    @DisplayName("Borrowing and returning on time")
    void borrowingAndReturningOnTime() {
        day(3, 2);
        assertEquals(date(3, 16), borrow("John", "C2").dueDate());

        day(3, 16);
        assertEquals(0, returnCopy("C2").daysLate());
        assertEquals(Money.ZERO, debt("John"));
        assertEquals(CopyStatus.AVAILABLE, copy("C2").status());
    }

    @Test
    @DisplayName("A copy cannot be lent twice")
    void aCopyCannotBeLentTwice() {
        day(3, 2);
        borrow("John", "C1");

        day(3, 3);
        refused(() -> borrow("Jane", "C1"), "C1 is on loan.");
    }

    @Test
    @DisplayName("The loan limit")
    void theLoanLimit() {
        day(3, 2);
        borrow("John", "C1");
        borrow("John", "C2");
        borrow("John", "C4");
        assertEquals(3, loanCount("John"));
        refused(() -> borrow("John", "C5"), "John has reached the loan limit of 3.");

        day(3, 5);
        assertEquals(0, returnCopy("C4").daysLate());
        borrow("John", "C5");
        assertEquals(3, loanCount("John"));
    }

    @Test
    @DisplayName("A late return creates a fine")
    void aLateReturnCreatesAFine() {
        day(3, 2);
        assertEquals(date(3, 16), borrow("Jane", "C3").dueDate());

        day(3, 19);
        assertEquals(3, returnCopy("C3").daysLate());
        assertEquals(Money.of("1.50"), debt("Jane"));
    }

    @Test
    @DisplayName("A debt blocks borrowing")
    void aDebtBlocksBorrowing() {
        day(3, 1);
        assertEquals(date(3, 15), borrow("John", "C1").dueDate());

        day(3, 18);
        assertEquals(3, returnCopy("C1").daysLate());
        assertEquals(Money.of("1.50"), debt("John"));
        refused(() -> borrow("John", "C2"), "John owes 1.50 EUR.");
    }

    @Test
    @DisplayName("Fines add up")
    void finesAddUp() {
        day(3, 2);
        borrow("Jane", "C2");
        borrow("Jane", "C4");

        day(3, 18);
        assertEquals(2, returnCopy("C2").daysLate());
        assertEquals(Money.of("1.00"), debt("Jane"));

        day(3, 21);
        assertEquals(5, returnCopy("C4").daysLate());
        assertEquals(Money.of("3.50"), debt("Jane"));
    }

    @Test
    @DisplayName("Paying fines")
    void payingFines() {
        day(3, 2);
        borrow("Jane", "C3");

        day(3, 19);
        returnCopy("C3");
        assertEquals(Money.of("1.50"), debt("Jane"));

        day(3, 20);
        assertEquals(Money.of("1.50"), pay("Jane"));
        assertEquals(Money.ZERO, debt("Jane"));
        assertEquals(date(4, 3), borrow("Jane", "C2").dueDate());
        refused(() -> pay("Mary"), "Mary owes nothing.");
    }

    @Test
    @DisplayName("Renewing a loan")
    void renewingALoan() {
        day(3, 2);
        assertEquals(date(3, 16), borrow("Mary", "C4").dueDate());

        day(3, 10);
        assertEquals(date(3, 30), renew("C4").dueDate());

        day(3, 28);
        assertEquals(date(4, 13), renew("C4").dueDate());

        day(4, 13);
        assertEquals(0, returnCopy("C4").daysLate());
        assertEquals(Money.ZERO, debt("Mary"));
    }

    @Test
    @DisplayName("A reservation blocks renewal")
    void aReservationBlocksRenewal() {
        day(3, 2);
        borrow("John", "C1");

        day(3, 5);
        assertEquals(1, reserve("Mary", "The Hobbit"));

        day(3, 10);
        refused(() -> renew("C1"), "The Hobbit is reserved.");
    }

    @Test
    @DisplayName("An overdue loan cannot be renewed")
    void anOverdueLoanCannotBeRenewed() {
        day(3, 2);
        borrow("Jane", "C5");

        day(3, 18);
        refused(() -> renew("C5"), "The loan is overdue.");
        assertEquals(2, returnCopy("C5").daysLate());
        assertEquals(Money.of("1.00"), debt("Jane"));
    }

    @Test
    @DisplayName("A debt blocks renewal")
    void aDebtBlocksRenewal() {
        day(3, 2);
        borrow("Jane", "C2");

        day(3, 10);
        assertEquals(date(3, 24), borrow("Jane", "C4").dueDate());

        day(3, 18);
        returnCopy("C2");
        assertEquals(Money.of("1.00"), debt("Jane"));

        day(3, 20);
        refused(() -> renew("C4"), "Jane owes 1.00 EUR.");
    }

    @Test
    @DisplayName("When a book can be reserved")
    void whenABookCanBeReserved() {
        day(3, 2);
        refused(() -> reserve("Jane", "Pride and Prejudice"),
                "A copy of Pride and Prejudice is available.");
        borrow("John", "C2");
        borrow("Mary", "C3");

        day(3, 3);
        refused(() -> reserve("John", "Pride and Prejudice"),
                "John has a copy of Pride and Prejudice on loan.");
        assertEquals(1, reserve("Jane", "Pride and Prejudice"));

        day(3, 4);
        refused(() -> reserve("Jane", "Pride and Prejudice"),
                "Jane already has a reservation for Pride and Prejudice.");
    }

    @Test
    @DisplayName("Reserving ignores the loan limit and debt")
    void reservingIgnoresTheLoanLimitAndDebt() {
        day(3, 2);
        borrow("John", "C1");
        borrow("Mary", "C2");
        borrow("Mary", "C4");
        borrow("Mary", "C5");
        borrow("Jane", "C3");

        day(3, 3);
        assertEquals(1, reserve("Mary", "The Hobbit"));

        day(3, 18);
        returnCopy("C3");
        assertEquals(Money.of("1.00"), debt("Jane"));
        assertEquals(2, reserve("Jane", "The Hobbit"));
    }

    @Test
    @DisplayName("A returned copy is held and the member is notified")
    void aReturnedCopyIsHeldAndTheMemberIsNotified() {
        day(3, 2);
        borrow("John", "C1");

        day(3, 4);
        reserve("Mary", "The Hobbit");

        day(3, 5);
        reserve("Jane", "The Hobbit");

        day(3, 10);
        assertEquals(0, returnCopy("C1").daysLate());
        assertEquals("Mary", copy("C1").heldFor());
        assertTrue(notifier.sent().contains("Mary: The Hobbit is waiting for you."));
    }

    @Test
    @DisplayName("A held copy is only for the member it is held for")
    void aHeldCopyIsOnlyForTheMemberItIsHeldFor() {
        day(3, 2);
        borrow("John", "C1");

        day(3, 4);
        reserve("Mary", "The Hobbit");

        day(3, 5);
        reserve("Jane", "The Hobbit");

        day(3, 10);
        returnCopy("C1");
        assertEquals("Mary", copy("C1").heldFor());
        assertTrue(notifier.sent().contains("Mary: The Hobbit is waiting for you."));

        day(3, 11);
        refused(() -> borrow("Jane", "C1"), "C1 is on hold for Mary.");

        day(3, 12);
        assertEquals(date(3, 26), borrow("Mary", "C1").dueDate());
        assertNull(copy("C1").heldFor());
        assertEquals(1, reservations().size());
        assertEquals("Jane", reservations().get(0).member());
    }

    @Test
    @DisplayName("A hold waits while the member cannot borrow")
    void aHoldWaitsWhileTheMemberCannotBorrow() {
        day(3, 2);
        borrow("Mary", "C2");

        day(3, 5);
        assertEquals(date(3, 19), borrow("John", "C1").dueDate());

        day(3, 6);
        reserve("Mary", "The Hobbit");

        day(3, 18);
        assertEquals(2, returnCopy("C2").daysLate());
        assertEquals(Money.of("1.00"), debt("Mary"));

        day(3, 19);
        assertEquals(0, returnCopy("C1").daysLate());
        assertEquals("Mary", copy("C1").heldFor());
        assertTrue(notifier.sent().contains("Mary: The Hobbit is waiting for you."));
        refused(() -> borrow("Mary", "C1"), "Mary owes 1.00 EUR.");
        assertEquals("Mary", copy("C1").heldFor());
        refused(() -> borrow("Jane", "C1"), "C1 is on hold for Mary.");

        day(3, 20);
        assertEquals(Money.of("1.00"), pay("Mary"));
        assertEquals(date(4, 3), borrow("Mary", "C1").dueDate());
    }

    @Test
    @DisplayName("A month at the library")
    void aMonthAtTheLibrary() {
        day(3, 2);
        assertEquals(date(3, 16), borrow("John", "C1").dueDate());
        assertEquals(date(3, 16), borrow("Jane", "C2").dueDate());

        day(3, 4);
        refused(() -> borrow("Mary", "C1"), "C1 is on loan.");
        assertEquals(1, reserve("Mary", "The Hobbit"));

        day(3, 10);
        refused(() -> renew("C1"), "The Hobbit is reserved.");

        day(3, 12);
        assertEquals(date(3, 30), renew("C2").dueDate());

        day(3, 16);
        assertEquals(0, returnCopy("C1").daysLate());
        assertEquals("Mary", copy("C1").heldFor());
        assertTrue(notifier.sent().contains("Mary: The Hobbit is waiting for you."));

        day(3, 17);
        refused(() -> borrow("Jane", "C1"), "C1 is on hold for Mary.");
        assertEquals(date(3, 31), borrow("Mary", "C1").dueDate());
        assertTrue(reservations().isEmpty());

        day(4, 2);
        assertEquals(3, returnCopy("C2").daysLate());
        assertEquals(Money.of("1.50"), debt("Jane"));
        refused(() -> borrow("Jane", "C4"), "Jane owes 1.50 EUR.");

        day(4, 3);
        assertEquals(Money.of("1.50"), pay("Jane"));
        assertEquals(date(4, 17), borrow("Jane", "C4").dueDate());
        assertEquals(3, returnCopy("C1").daysLate());
        assertEquals(Money.of("1.50"), debt("Mary"));
    }

    // ---- helpers ----

    private void day(int month, int day) {
        clock.set(date(month, day));
    }

    private LocalDate date(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    private void refused(Executable operation, String reason) {
        Refusal refusal = assertThrows(Refusal.class, operation);
        assertEquals(reason, refusal.getMessage());
    }

    // ---- the five commands: a request in, the recorded response out ----

    private BorrowCopyResponse borrow(String member, String copy) {
        borrowCopy.execute(new BorrowCopyRequest(member, copy), recorder);
        return recorder.borrowed;
    }

    private ReturnCopyResponse returnCopy(String copy) {
        returnCopy.execute(new ReturnCopyRequest(copy), recorder);
        return recorder.returned;
    }

    private RenewLoanResponse renew(String copy) {
        renewLoan.execute(new RenewLoanRequest(copy), recorder);
        return recorder.renewed;
    }

    private int reserve(String member, String book) {
        reserveBook.execute(new ReserveBookRequest(member, book), recorder);
        return recorder.reserved.position();
    }

    private Money pay(String member) {
        payFine.execute(new PayFineRequest(member), recorder);
        return recorder.paid.paid();
    }

    // ---- reading the library back through the listing use cases ----

    private CopySummary copy(String number) {
        listCopies.execute(recorder);
        for (CopySummary copy : recorder.copies.copies()) {
            if (copy.number().equals(number)) {
                return copy;
            }
        }
        throw new IllegalArgumentException(number);
    }

    private MemberSummary member(String name) {
        listMembers.execute(recorder);
        for (MemberSummary member : recorder.members.members()) {
            if (member.name().equals(name)) {
                return member;
            }
        }
        throw new IllegalArgumentException(name);
    }

    private List<ReservationSummary> reservations() {
        listReservations.execute(recorder);
        return recorder.reservations.reservations();
    }

    private Money debt(String name) {
        return member(name).debt();
    }

    private int loanCount(String name) {
        return member(name).loans();
    }
}
