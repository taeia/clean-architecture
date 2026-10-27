package library.application;

import library.model.Member;

/**
 * Tells a member something. A driven port: the inside says what it wants said and to
 * whom, and an adapter decides what sending means, a list in a window, a line on a
 * console, or a real channel. Chapter 6 put this interface in the domain and had the
 * application layer call it, and asked what to make of that. The hexagon's answer was
 * that both are inside, and a port belongs to the inside as a whole. The onion's answer
 * is this ring: an interface is declared in the innermost ring that calls it, and the
 * only caller is the {@code ReturnCopy} use case, so the notifier lives with the use
 * cases, and {@code Lending}, one ring in, cannot hold one.
 */
public interface Notifier {

    void send(Member member, String message);
}
