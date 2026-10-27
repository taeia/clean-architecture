package library.application.listcopies;

/**
 * The three states of a copy, as the use case reports them. The enum inside {@code Copy}
 * is the copy's own business; this one is what crosses the boundary.
 */
public enum CopyStatus {
    AVAILABLE, ON_LOAN, ON_HOLD
}
