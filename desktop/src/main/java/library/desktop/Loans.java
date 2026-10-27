package library.desktop;

import library.presenters.CopyRow;

/**
 * How the Return and Renew dialogs show a loan in their list: the copy, its book, who
 * has it, and when it is due, from the row the table presenter made. Which copies are
 * on loan is no longer decided here; the presenter picks them out, and this class
 * only puts four strings in a row. In Chapter 8 it also did the picking.
 */
class Loans {

    static String describe(CopyRow copy) {
        return copy.number() + " (" + copy.book() + "), " + copy.borrower() + ", due " + copy.due();
    }
}
