package org.acme;

public record HelloEvent(String hello) {
    public static final String NAME = "HelloEvent";
}
