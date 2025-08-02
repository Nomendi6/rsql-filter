package com.nomendi6.rsql.demo.web.rest.util;

import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Define a http header for lists
 */
public final class ListHeaderUtil {

    public static final String X_TOTAL_COUNT = "X-Total-Count";

    private ListHeaderUtil() {}

    public static HttpHeaders generateListHttpHeaders(String baseUrl, String filter, int datasetSize) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_TOTAL_COUNT, Long.toString(datasetSize));
        String link = generateUri(baseUrl, filter);
        headers.add(HttpHeaders.LINK, link);
        return headers;
    }

    private static String generateUri(String baseUrl, String filter) {
        if (filter == null) return UriComponentsBuilder.fromUriString(baseUrl).toUriString();
        else return UriComponentsBuilder.fromUriString(baseUrl).queryParam("filter", filter).toUriString();
    }
}
