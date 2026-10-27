package library.application.returncopy;

import library.model.Money;

/**
 * The response model: who had the copy and which copy it was, how many days late it
 * came back, with 0 for on time, what that member owes now, and the member the copy is
 * held for, or null if nobody was waiting. Chapter 8's {@code Returned}, with the copy
 * added for the presenter's sake.
 */
public record ReturnCopyResponse(String member, String copy, long daysLate, Money debt, String heldFor) {
}
