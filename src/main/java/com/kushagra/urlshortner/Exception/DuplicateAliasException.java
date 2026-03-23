package com.kushagra.urlshortner.Exception;

public class DuplicateAliasException extends RuntimeException {
    public DuplicateAliasException(String alias) {
        super("Custom alias is already taken: " + alias);
    }
}