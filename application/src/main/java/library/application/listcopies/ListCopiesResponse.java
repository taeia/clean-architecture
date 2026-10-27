package library.application.listcopies;

import java.util.List;

/**
 * The response model: the copies, one summary each. A record with one field, so that
 * each listing answers with a type of its own, which a presenter that implements
 * several output boundaries needs: Java cannot tell one {@code present(List)} from
 * another once the lists are erased.
 */
public record ListCopiesResponse(List<CopySummary> copies) {
}
