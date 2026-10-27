package library.domain;

import java.time.LocalDate;

/**
 * A clock for the tests of this module. This ring declares the port, so a test
 * can hold its own clock and move it with one call. No test shares it with another.
 */
class FakeClock implements Clock {

    private LocalDate today;

    FakeClock(LocalDate today) {
        this.today = today;
    }

    void set(LocalDate today) {
        this.today = today;
    }

    @Override
    public LocalDate today() {
        return today;
    }
}
