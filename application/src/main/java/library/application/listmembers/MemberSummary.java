package library.application.listmembers;

import library.model.Money;

/**
 * One member as a listing shows them: the name, how many copies they have on loan,
 * and what they owe. The debt is a {@code Money}, a value, and a value may cross the
 * boundary: it has no identity and nothing in the library changes when it is held.
 */
public record MemberSummary(String name, int loans, Money debt) {
}
