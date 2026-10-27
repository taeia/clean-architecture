package library.presenters;

/**
 * One member as a table shows them: name, loans, debt, all three as text. The view
 * model of a member.
 */
public record MemberRow(String name, String loans, String debt) {
}
