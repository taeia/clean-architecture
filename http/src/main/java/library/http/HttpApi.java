package library.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import library.application.Refusal;
import library.application.borrowcopy.BorrowCopyInput;
import library.application.borrowcopy.BorrowCopyRequest;
import library.application.listcopies.ListCopiesInput;
import library.application.listmembers.ListMembersInput;
import library.application.listreservations.ListReservationsInput;
import library.application.payfine.PayFineInput;
import library.application.payfine.PayFineRequest;
import library.application.renewloan.RenewLoanInput;
import library.application.renewloan.RenewLoanRequest;
import library.application.reservebook.ReserveBookInput;
import library.application.reservebook.ReserveBookRequest;
import library.application.returncopy.ReturnCopyInput;
import library.application.returncopy.ReturnCopyRequest;
import library.presenters.TextPresenter;

/**
 * The library over HTTP. A driving adapter, a controller and a view in one class: it
 * turns a request into a request model and a call on one input boundary, and answers
 * with what the presenter made of the response, which is the sentence the window and
 * the command line show. {@code POST /borrow?member=John&copy=C1},
 * {@code POST /return?copy=C1}, {@code POST /renew?copy=C1},
 * {@code POST /reserve?member=Mary&book=The+Hobbit}, and {@code POST /pay?member=Jane}
 * are the five commands; {@code GET /copies}, {@code GET /members}, and
 * {@code GET /reservations} are the listings. A refusal answers 409 with the reason.
 * There is no {@code day}: this driver does not own the clock, the window it runs
 * beside does.
 *
 * It has no {@code main}. The desktop configurator starts it next to the window and
 * tells it which thread to answer on, so that the window and the terminal drive one
 * library and never at the same instant. Each request gets a presenter of its own,
 * because a presenter keeps what it was last told and two requests should not share
 * one.
 */
public class HttpApi {

    private final BorrowCopyInput borrowCopy;
    private final ReturnCopyInput returnCopy;
    private final RenewLoanInput renewLoan;
    private final ReserveBookInput reserveBook;
    private final PayFineInput payFine;
    private final ListCopiesInput listCopies;
    private final ListMembersInput listMembers;
    private final ListReservationsInput listReservations;
    private HttpServer server;

    public HttpApi(BorrowCopyInput borrowCopy, ReturnCopyInput returnCopy, RenewLoanInput renewLoan,
                   ReserveBookInput reserveBook, PayFineInput payFine, ListCopiesInput listCopies,
                   ListMembersInput listMembers, ListReservationsInput listReservations) {
        this.borrowCopy = borrowCopy;
        this.returnCopy = returnCopy;
        this.renewLoan = renewLoan;
        this.reserveBook = reserveBook;
        this.payFine = payFine;
        this.listCopies = listCopies;
        this.listMembers = listMembers;
        this.listReservations = listReservations;
    }

    /**
     * Starts listening on the port, 0 for any free one, and answers every request on
     * the executor, or on the server's own thread when the executor is null.
     */
    public void start(int port, Executor runOn) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(runOn);
        server.createContext("/", this::handle);
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    /**
     * Turns one request into one call on one use case, the way {@code Cli.execute}
     * turns one line into one. The method and the path together name the use case.
     * A listing ends with a newline, a sentence does not: that is this view's
     * decision, and the only one it makes about the text.
     */
    private void handle(HttpExchange exchange) throws IOException {
        String request = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();
        TextPresenter presenter = new TextPresenter();
        try {
            Map<String, String> query = query(exchange.getRequestURI().getRawQuery());
            switch (request) {
                case "POST /borrow" -> borrowCopy.execute(
                        new BorrowCopyRequest(required(query, "member"), required(query, "copy")), presenter);
                case "POST /return" -> returnCopy.execute(new ReturnCopyRequest(required(query, "copy")), presenter);
                case "POST /renew" -> renewLoan.execute(new RenewLoanRequest(required(query, "copy")), presenter);
                case "POST /reserve" -> reserveBook.execute(
                        new ReserveBookRequest(required(query, "member"), required(query, "book")), presenter);
                case "POST /pay" -> payFine.execute(new PayFineRequest(required(query, "member")), presenter);
                case "GET /copies" -> listCopies.execute(presenter);
                case "GET /members" -> listMembers.execute(presenter);
                case "GET /reservations" -> listReservations.execute(presenter);
                default -> throw new NoSuchElementException("There is nothing at " + request + ".");
            }
            reply(exchange, 200, request.startsWith("GET") ? presenter.text() + "\n" : presenter.text());
        } catch (Refusal e) {
            reply(exchange, 409, "The library refuses. " + e.getMessage());
        } catch (IllegalArgumentException e) {
            reply(exchange, 400, e.getMessage());
        } catch (NoSuchElementException e) {
            reply(exchange, 404, e.getMessage());
        }
    }

    // ---- the request and the response ----

    private static Map<String, String> query(String rawQuery) {
        Map<String, String> result = new HashMap<>();
        if (rawQuery != null) {
            for (String pair : rawQuery.split("&")) {
                int equals = pair.indexOf('=');
                if (equals > 0) {
                    result.put(URLDecoder.decode(pair.substring(0, equals), StandardCharsets.UTF_8),
                            URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8));
                }
            }
        }
        return result;
    }

    private static String required(Map<String, String> query, String name) {
        String value = query.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The request needs a " + name + ".");
        }
        return value;
    }

    private static void reply(HttpExchange exchange, int status, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
