package library.domain;

import java.time.LocalDate;

/**
 * The library's idea of today. A driven port: the inside asks, and whoever wired the
 * library decides who answers, a date picker, a test, or the operating system. It is
 * declared in this ring because {@code Lending} is the one that asks.
 */
public interface Clock {

    LocalDate today();
}
