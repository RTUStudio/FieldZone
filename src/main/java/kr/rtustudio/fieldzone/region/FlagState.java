package kr.rtustudio.fieldzone.region;

/**
 * Tri-state result for flag queries.
 * <ul>
 *   <li>{@link #TRUE} — the flag is explicitly set to {@code true}</li>
 *   <li>{@link #FALSE} — the flag is explicitly set to {@code false}</li>
 *   <li>{@link #NONE} — the flag has not been set on the region</li>
 * </ul>
 */
public enum FlagState {
    TRUE,
    FALSE,
    NONE;

    /**
     * Converts to a boolean.
     * Only {@link #TRUE} returns {@code true}; both {@link #FALSE} and {@link #NONE} return {@code false}.
     */
    public boolean toBoolean() {
        return this == TRUE;
    }

    /**
     * Creates a FlagState from a boolean value.
     */
    public static FlagState of(boolean value) {
        return value ? TRUE : FALSE;
    }
}
