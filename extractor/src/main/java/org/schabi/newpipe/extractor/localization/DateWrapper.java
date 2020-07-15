package org.schabi.newpipe.extractor.localization;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Calendar;

/**
 * A wrapper class that provides a field to describe if the date is precise or just an approximation.
 */
public class DateWrapper implements Serializable {
    @Nonnull private final Calendar date;
    private final boolean isApproximation;

    public DateWrapper(@Nonnull Calendar date) {
        this(date, false);
    }

    public DateWrapper(@Nonnull Calendar date, boolean isApproximation) {
        this.date = date;
        this.isApproximation = isApproximation;
    }

    /**
     * @return the wrapped date.
     */
    @Nonnull
    public Calendar date() {
        return date;
    }

    /**
     * @return if the date is considered is precise or just an approximation (e.g. service only returns an approximation
     * like 2 weeks ago instead of a precise date).
     */
    public boolean isApproximation() {
        return isApproximation;
    }
}
