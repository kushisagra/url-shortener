package com.kushagra.urlshortner.Exception;

public class UrlNotFoundException extends   RuntimeException {

    public UrlNotFoundException(String shortcode) {
       super("Short URL not found " + shortcode);
    }
}
