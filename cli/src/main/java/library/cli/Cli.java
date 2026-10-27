package library.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

import library.application.Refusal;
import library.application.borrowcopy.BorrowCopy;
import library.application.borrowcopy.BorrowCopyInput;
import library.application.borrowcopy.BorrowCopyRequest;
import library.application.listcopies.ListCopies;
import library.application.listcopies.ListCopiesInput;
import library.application.listmembers.ListMembers;
import library.application.listmembers.ListMembersInput;
import library.application.listreservations.ListReservations;
import library.application.listreservations.ListReservationsInput;
import library.application.payfine.PayFine;
import library.application.payfine.PayFineInput;
import library.application.payfine.PayFineRequest;
import library.application.renewloan.RenewLoan;
import library.application.renewloan.RenewLoanInput;
import library.application.renewloan.RenewLoanRequest;
import library.application.reservebook.ReserveBook;
import library.application.reservebook.ReserveBookInput;
import library.application.reservebook.ReserveBookRequest;
import library.application.returncopy.ReturnCopy;
import library.application.returncopy.ReturnCopyInput;
import library.application.returncopy.ReturnCopyRequest;
import library.domain.Lending;
import library.domain.Reserving;
import library.inmemory.InMemoryBookRepository;
import library.inmemory.InMemoryCopyRepository;
import library.inmemory.InMemoryMemberRepository;
import library.inmemory.InMemoryQueueRepository;
import library.inmemory.SettableClock;
import library.inmemory.StartingState;
import library.presenters.TextPresenter;

/**
 * The library on the command line. A driving adapter, and in Martin's words a
 * controller and a view in one class: it reads commands written in the words of
 * Chapter 0, turns each line into a request model and calls the use case behind the
 * input boundary, and prints what the presenter made of the response. The names on
 * the line, John, C1, The Hobbit, are the names the requests carry, so a command is
 * one record built from the words and one call. The sentences and the listings are
 * not built here any more: a {@link TextPresenter} builds them, the same one the
 * window and the endpoint use, and this class prints what it finds in it. It names a
 * use case class only in {@link #main}, which is the configurator for this program.
 *
 * Commands are one per line. {@code day 2 March} moves the clock. {@code borrow John C1},
 * {@code return C1}, {@code renew C1}, {@code reserve Mary The Hobbit}, and
 * {@code pay Jane} are the five operations. {@code copies}, {@code members}, and
 * {@code reservations} show the library, {@code reset} puts it back in its starting
 * state, and {@code quit} ends the session. Dates are in 2026, the year of the scenarios.
 */
public class Cli {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_WITH_YEAR = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("d MMMM uuuu")
            .toFormatter(Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);

    private final BorrowCopyInput borrowCopy;
    private final ReturnCopyInput returnCopy;
    private final RenewLoanInput renewLoan;
    private final ReserveBookInput reserveBook;
    private final PayFineInput payFine;
    private final ListCopiesInput listCopies;
    private final ListMembersInput listMembers;
    private final ListReservationsInput listReservations;
    private final TextPresenter presenter = new TextPresenter();
    private final SettableClock clock;
    private final Runnable reset;
    private final PrintStream out;

    /**
     * Eight use cases, one by one, where Chapter 9 took four ports. That is what a
     * driver holds when every use case has a boundary of its own; the count is the
     * point, and the chapter says what it costs.
     */
    public Cli(BorrowCopyInput borrowCopy, ReturnCopyInput returnCopy, RenewLoanInput renewLoan,
               ReserveBookInput reserveBook, PayFineInput payFine, ListCopiesInput listCopies,
               ListMembersInput listMembers, ListReservationsInput listReservations,
               SettableClock clock, Runnable reset, PrintStream out) {
        this.borrowCopy = borrowCopy;
        this.returnCopy = returnCopy;
        this.renewLoan = renewLoan;
        this.reserveBook = reserveBook;
        this.payFine = payFine;
        this.listCopies = listCopies;
        this.listMembers = listMembers;
        this.listReservations = listReservations;
        this.clock = clock;
        this.reset = reset;
        this.out = out;
    }

    /**
     * Reads commands from a script file if one is given, otherwise from the terminal.
     */
    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.ENGLISH);

        // The configurator for the command line: the same entities and use cases as the
        // window, the same in-memory storage and clock, a different mailbox, and nobody
        // on the change signal. The window keeps the messages in a list it shows; the
        // command line prints them as they are sent. The window refreshes when the
        // signal rings; the command line prints what it was told and has no tables to
        // refresh. Eight use cases are built here, not nine: nobody lists books on a
        // command line.
        InMemoryBookRepository books = new InMemoryBookRepository();
        InMemoryCopyRepository copies = new InMemoryCopyRepository();
        InMemoryMemberRepository members = new InMemoryMemberRepository();
        InMemoryQueueRepository queues = new InMemoryQueueRepository();
        SettableClock clock = new SettableClock();
        ConsoleNotifier notifier = new ConsoleNotifier(System.out);
        Lending lending = new Lending(members, copies, queues, clock);
        Reserving reserving = new Reserving(copies, queues);
        Runnable reset = () -> StartingState.load(books, copies, members, queues);
        reset.run();
        Cli cli = new Cli(new BorrowCopy(lending, members, copies, () -> {}),
                new ReturnCopy(lending, members, copies, notifier, () -> {}),
                new RenewLoan(lending, members, copies, () -> {}),
                new ReserveBook(reserving, books, members, queues, () -> {}),
                new PayFine(members, () -> {}),
                new ListCopies(copies, members),
                new ListMembers(members),
                new ListReservations(queues),
                clock, reset, System.out);

        if (args.length > 0) {
            try (BufferedReader script = Files.newBufferedReader(Path.of(args[0]))) {
                cli.run(script, true);
            }
        } else {
            cli.run(new BufferedReader(new InputStreamReader(System.in)), false);
        }
    }

    /**
     * Runs commands until the input ends or a {@code quit}. When reading a script, each
     * command is echoed behind a prompt, so that the output reads like a session.
     */
    public void run(BufferedReader in, boolean echo) throws IOException {
        if (!echo) {
            out.print("> ");
        }
        String line;
        while ((line = in.readLine()) != null) {
            line = line.strip();
            if (!line.isEmpty() && !line.startsWith("#")) {
                if (echo) {
                    out.println("> " + line);
                }
                out.println(execute(line));
                if (line.equals("quit")) {
                    return;
                }
            }
            if (!echo) {
                out.print("> ");
            }
        }
    }

    /**
     * Executes one command and returns the sentence to print.
     */
    public String execute(String line) {
        String[] words = line.strip().split("\\s+", 2);
        String command = words[0];
        String rest = words.length > 1 ? words[1].strip().replaceAll("\\s+", " ") : "";
        try {
            return switch (command) {
                case "day" -> day(rest);
                case "borrow" -> borrow(rest);
                case "return" -> returnCopy(rest);
                case "renew" -> renew(rest);
                case "reserve" -> reserve(rest);
                case "pay" -> pay(rest);
                case "copies" -> copies();
                case "members" -> members();
                case "reservations" -> reservations();
                case "reset" -> reset();
                case "help" -> help();
                case "quit" -> "Goodbye.";
                default -> "Unknown command: " + command + ". Type help.";
            };
        } catch (Refusal e) {
            return "The library refuses. " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    // ---- the clock ----

    private String day(String text) {
        try {
            clock.set(LocalDate.parse(text + " 2026", DAY_WITH_YEAR));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Write the day as in \"day 2 March\".");
        }
        return "It is " + clock.today().format(DAY) + ".";
    }

    // ---- the five operations: a request in, the presenter's sentence out ----

    private String borrow(String rest) {
        String[] words = twoWords(rest, "borrow John C1");
        borrowCopy.execute(new BorrowCopyRequest(words[0], words[1]), presenter);
        return presenter.text();
    }

    private String returnCopy(String rest) {
        returnCopy.execute(new ReturnCopyRequest(oneWord(rest, "return C1")), presenter);
        return presenter.text();
    }

    private String renew(String rest) {
        renewLoan.execute(new RenewLoanRequest(oneWord(rest, "renew C1")), presenter);
        return presenter.text();
    }

    private String reserve(String rest) {
        String[] words = twoWords(rest, "reserve Mary The Hobbit");
        reserveBook.execute(new ReserveBookRequest(words[0], words[1]), presenter);
        return presenter.text();
    }

    private String pay(String rest) {
        payFine.execute(new PayFineRequest(oneWord(rest, "pay Jane")), presenter);
        return presenter.text();
    }

    // ---- the library as it stands: the presenter's lines ----

    private String copies() {
        listCopies.execute(presenter);
        return presenter.text();
    }

    private String members() {
        listMembers.execute(presenter);
        return presenter.text();
    }

    private String reservations() {
        listReservations.execute(presenter);
        return presenter.text();
    }

    private String reset() {
        reset.run();
        return "The library is back in its starting state.";
    }

    private String help() {
        return """
                day 2 March              set the date (the year is 2026)
                borrow John C1           a member borrows a copy
                return C1                a copy comes back
                renew C1                 the loan of a copy is renewed
                reserve Mary The Hobbit  a member reserves a book
                pay Jane                 a member pays what they owe
                copies, members, reservations
                reset, help, quit""";
    }

    // ---- the shape of a line ----

    private static String oneWord(String rest, String example) {
        if (rest.isEmpty() || rest.contains(" ")) {
            throw new IllegalArgumentException("Write it as in \"" + example + "\".");
        }
        return rest;
    }

    private static String[] twoWords(String rest, String example) {
        String[] words = rest.split("\\s+", 2);
        if (words.length < 2) {
            throw new IllegalArgumentException("Write it as in \"" + example + "\".");
        }
        return words;
    }
}
