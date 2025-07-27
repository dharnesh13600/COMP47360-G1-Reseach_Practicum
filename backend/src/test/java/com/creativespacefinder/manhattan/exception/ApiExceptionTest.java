package com.creativespacefinder.manhattan.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionTest {
    @Test
    void constructor_withMessageOnly_setsMessageAndNullCause() {
        ApiException ex = new ApiException("msg");
        assertEquals("msg", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructor_withMessageAndCause_setsBoth() {
        Throwable cause = new IllegalStateException("cause");
        ApiException ex = new ApiException("msg", cause);
        assertEquals("msg", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
