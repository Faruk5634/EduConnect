package com.educonnect.exception;

/**
 * Thrown when a requested entity genuinely doesn't exist.
 *
 * The previous handler mapped generic IllegalArgumentException -> 404,
 * "because we only throw it for not-found cases" — but that's an implicit,
 * fragile contract: any accidental IllegalArgumentException anywhere in the
 * codebase (bad enum value, bad argument unrelated to a lookup) would have
 * been silently reported to the client as "404 Not Found", which is
 * misleading and makes real bugs harder to diagnose.
 *
 * Most of the codebase already throws ResponseStatusException(NOT_FOUND, ...)
 * directly, which works fine and is handled below. This type exists for the
 * few call sites that want a named, typed exception instead.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}