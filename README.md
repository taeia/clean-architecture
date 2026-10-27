# Clean Architecture

Companion repository for Chapter 10 of [*Domain-Centric Architecture*](https://github.com/taeia/domain-centric-architecture): the library lending application with its use cases as classes, in the circles of Robert C. Martin's Clean Architecture, one Maven module per circle and one per adapter, every dependency pointing inward.

The rules, the aggregates, the domain services, the storage, and every sentence the program prints are those of Chapter 9, and so are the seventeen scenarios. What changed is the circle around the domain. Chapter 9's four application services, four driving ports, and seven records are now nine use cases, one class per thing the library does: `BorrowCopy`, `ReturnCopy`, `RenewLoan`, `ReserveBook`, `PayFine`, `ListCopies`, `ListMembers`, `ListBooks`, `ListReservations`, each in a package of its own with its input boundary, its output boundary, and its request and response models. A driver builds a request and calls the input boundary; the use case does the work and calls the output boundary with a response; a presenter on the outside implements the output boundary and turns the response into what a screen shows. The sentence "John borrowed C1 of The Hobbit. It is due on 16 March." was built in the window, on the command line, and in the endpoint in Chapters 8 and 9; here it is built once, in `TextPresenter`, and the three drivers show it.

The poms are the rule, as in Chapter 9. `model` lists nothing. `domain` lists `model`. `application`, the use-case circle, lists `domain` and `model`. On the outside, `inmemory` lists all three and did not change a line; `presenters` lists `application` alone; `http` lists `application` and `presenters`; `desktop` lists those two, `domain`, `inmemory`, and `http`; `cli` lists `application`, `presenters`, `domain`, `model`, and `inmemory`; `scenarios` lists `application`, `domain`, `model`, and `inmemory`, and brings a presenter of its own. A use case cannot name a presenter, a sentence, or a screen, because its pom does not list the modules they are in.

The library it implements, with its rules and scenarios, is described in [Chapter 0](https://github.com/taeia/domain-centric-architecture/blob/main/chapters/00-the-library.md). The chapter about this code is [Chapter 10](https://github.com/taeia/domain-centric-architecture/blob/main/chapters/10-clean-architecture.md).

## Running

You need Java 25 and Maven. JavaFX is a Maven dependency, so nothing else has to be installed.

```
mvn install
mvn -pl desktop javafx:run
```

The first command builds all nine modules and runs the tests. The second starts the window from the desktop module. Repeat the first command after changing anything outside `desktop`.

A window opens with the library in its starting state, and the status bar says that it also listens on `http://localhost:8080`. The date picker in the toolbar sets the library's idea of today, so you can walk through the scenarios of Chapter 0 and check the due dates and fines by hand. The *Reset library* button returns to the starting state.

Now open a terminal and borrow a copy from there:

```
curl -X POST "http://localhost:8080/borrow?member=John&copy=C1"
```

The answer is the sentence the window would have shown, and the Copies table in the window shows C1 on loan to John without a click. In PowerShell, write `curl.exe`, because `curl` alone is an alias for something else. The five commands are `POST /borrow?member=John&copy=C1`, `POST /return?copy=C1`, `POST /renew?copy=C1`, `POST /reserve?member=Mary&book=The%20Hobbit`, and `POST /pay?member=Jane`; the three listings are `GET /copies`, `GET /members`, and `GET /reservations`. A refusal answers with status 409 and the reason. There is no `day` over HTTP: the window owns the clock.

To start the window from an IDE, run `library.desktop.Launcher`. Running `MainWindow` directly fails with *JavaFX runtime components are missing*, because JavaFX is on the classpath and the Java launcher checks for the modules whenever the main class extends `Application`. `Launcher` does not extend it, so the check is skipped.

The same library on the command line:

```
mvn -q -pl cli exec:java
```

Type `help` for the commands. They are written the way Chapter 0 writes its scenarios: `day 2 March`, `borrow John C1`, `return C1`, `renew C1`, `reserve Mary The Hobbit`, `pay Jane`, and `copies`, `members`, `reservations` to look at the library. `quit` ends the session. A script runs the long scenario of Chapter 0 from start to finish:

```
mvn -q -pl cli exec:java -Dexec.args=cli/scripts/a-month-at-the-library.txt
```

## Tests

```
mvn test
```

`ScenarioTest` lives in the `scenarios` module and has one test per scenario of Chapter 0, with the same name. It holds the nine use cases by their input boundaries, names members, copies, and books the way Chapter 0 does, moves a `SettableClock` from day to day, and reads the library back through the listing use cases. It brings its own presenter, `Recorder`, which implements every output boundary and keeps the last response of each kind, so that an assertion can read a due date or a debt back as a value. It never sees an aggregate.

The unit tests of single rules are Chapter 9's, in the modules they test. The `model` module has the tests of the aggregates and the values, thirty-eight of them, which need no fake because the model asks nothing of anyone. The `domain` module has the eleven tests of `Lending` and `Reserving`, on fakes that implement four of the five interfaces the ring declares. The `cli` module tests the command line: what it prints for what it is told, including the whole month script. The `http` module tests the endpoint: what it answers for what it is sent, and that the change signal rings once per completed command and not for a refusal. Seventy-eight tests in all, the same seventy-eight as Chapters 8 and 9.

## Layout

```
pom.xml                        the parent: lists the nine modules, holds the versions
model/                         entities: the domain model, as in Chapter 9
  pom.xml                      depends on nothing (JUnit in test scope)
  src/main/java/library/model/
    Member.java                aggregate root: name, debt, loans; borrow, returnCopy, renew, pay
    Loan.java                  entity inside Member: due date, isOverdue, fine, each for a given day
    Queue.java                 aggregate root: the reservations of one book; join, leave, first
    Reservation.java           entity inside Queue
    Copy.java                  aggregate root: available, on loan, or on hold; lendTo, holdFor, makeAvailable
    Book.java                  aggregate root: ISBN and title
    Money.java                 value object: an amount in euros, never negative
    Fine.java                  value object: days of delay and what they cost
    LibraryException.java      thrown when the model refuses an operation
  src/test/java/library/model/
    MemberTest.java            unit tests of single rules, one class per domain class
    LoanTest.java
    QueueTest.java
    CopyTest.java
    MoneyTest.java
    FineTest.java
domain/                        entities: the domain services and the interfaces they call, as in Chapter 9
  pom.xml                      depends on model
  src/main/java/library/domain/
    Lending.java               domain service: borrow, return, renew, across aggregates; asks the clock
    Reserving.java             domain service: reserve
    MemberRepository.java      all, named, save: declared here, implemented on the outside
    QueueRepository.java
    CopyRepository.java        all, ofBook, numbered, save
    BookRepository.java        all, titled, save
    Clock.java                 what day it is
  src/test/java/library/domain/
    LendingTest.java           the operations that cross aggregates, on the fakes below
    ReservingTest.java
    FakeMemberRepository.java  four of this ring's five interfaces, implemented for these tests
    FakeCopyRepository.java
    FakeQueueRepository.java
    FakeClock.java
application/                   use cases: one package per thing the library does
  pom.xml                      depends on domain and model
  src/main/java/library/application/
    Refusal.java               thrown through an input boundary when the library refuses
    Notifier.java              driven port, called only by ReturnCopy: tell this member this
    ChangeSignal.java          driven port, called by the five command use cases: the library changed
    Names.java                 from a name in a request to the aggregate behind it, or a refusal
    borrowcopy/
      BorrowCopy.java          the use case: names to aggregates, Lending, signal, response out
      BorrowCopyInput.java     input boundary: execute(request, output); what a driver holds
      BorrowCopyOutput.java    output boundary: present(response); what a presenter implements
      BorrowCopyRequest.java   request model: member, copy
      BorrowCopyResponse.java  response model: member, copy, book, dueDate
    returncopy/                the same five files: ReturnCopy, ReturnCopyInput, ReturnCopyOutput,
                               ReturnCopyRequest (copy), ReturnCopyResponse (member, copy, daysLate, debt, heldFor)
    renewloan/                 RenewLoan; request (copy); response (member, copy, dueDate)
    reservebook/               ReserveBook; request (member, book); response (member, book, position)
    payfine/                   PayFine; request (member); response (member, paid)
    listcopies/
      ListCopies.java          the use case: every copy as a CopySummary
      ListCopiesInput.java     input boundary: execute(output); a listing has no request model
      ListCopiesOutput.java    output boundary
      ListCopiesResponse.java  response model: the list of summaries
      CopySummary.java         one copy: number, book, status, borrower, dueDate, heldFor
      CopyStatus.java          AVAILABLE, ON_LOAN, ON_HOLD
    listmembers/               ListMembers, its boundaries and response, MemberSummary
    listbooks/                 ListBooks, its boundaries and response, BookSummary
    listreservations/          ListReservations, its boundaries and response, ReservationSummary
inmemory/                      interface adapters: the gateways, unchanged since Chapter 9
  pom.xml                      depends on model, domain, and application
  src/main/java/library/inmemory/
    InMemoryMemberRepository.java   the repository interfaces of the domain ring, implemented with lists
    InMemoryQueueRepository.java
    InMemoryCopyRepository.java
    InMemoryBookRepository.java
    SettableClock.java         the clock: set by the date picker, the day command, and the tests
    InMemoryNotifier.java      the notifier: adds each message to a list the window shows
    StartingState.java         the starting state of Chapter 0
presenters/                    interface adapters: the presenters
  pom.xml                      depends on application
  src/main/java/library/presenters/
    TextPresenter.java         eight output boundaries; the sentence or the listing, as text, read with text()
    TablePresenter.java        four output boundaries; rows of strings for the window, read with copies() and the rest
    CopyRow.java               view models: a row each, every field already text
    MemberRow.java
    BookRow.java
    ReservationRow.java
http/                          the HTTP endpoint: a controller and a view
  pom.xml                      depends on application and presenters (domain and inmemory in test scope)
  src/main/java/library/http/
    HttpApi.java               one request becomes one request model and one call; a presenter per request
  src/test/java/library/http/
    HttpApiTest.java           what the endpoint answers for what it is sent
desktop/                       the JavaFX window: a view, with a controller in each dialog
  pom.xml                      depends on application, presenters, domain, inmemory, and http
  src/main/java/library/desktop/
    MainWindow.java            start() is the configurator for the window and the endpoint; refresh() when the signal rings
    Launcher.java              starts MainWindow from an IDE
    Chooser.java               a combo box that shows a row by the text the dialog chooses
    Loans.java                 how a loan is described in the Return and Renew lists
    BorrowDialog.java          the five dialogs, each holding the input boundaries it calls and the two presenters
    ReturnDialog.java
    RenewDialog.java
    ReserveDialog.java
    PayDialog.java
cli/                           the command line: a controller and a view
  pom.xml                      depends on application, presenters, domain, model, and inmemory
  scripts/
    a-month-at-the-library.txt the long scenario of Chapter 0 as a script
  src/main/java/library/cli/
    Cli.java                   main() is the configurator; execute() turns a line into a request and a call
    ConsoleNotifier.java       a driven adapter owned by this driver: prints each message
  src/test/java/library/cli/
    CliTest.java               what the command line prints for what it is told
scenarios/                     the tests
  pom.xml                      depends on application, domain, model, and inmemory
  src/test/java/library/scenarios/
    ScenarioTest.java          one test per scenario, through the input boundaries
    Recorder.java              a presenter that keeps each response, for the assertions to read
```
