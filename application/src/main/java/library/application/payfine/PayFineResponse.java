package library.application.payfine;

import library.model.Money;

/**
 * The response model: who paid, and how much. Chapter 8 answered with the amount
 * alone. The amount is a {@code Money}, a value, and a value may cross the boundary.
 */
public record PayFineResponse(String member, Money paid) {
}
