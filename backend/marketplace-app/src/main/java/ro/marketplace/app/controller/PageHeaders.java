package ro.marketplace.app.controller;

import org.springframework.http.HttpHeaders;
import ro.marketplace.domain.model.PageResult;

/**
 * Adds pagination metadata to list responses without changing their JSON shape,
 * so existing clients keep working while new clients can detect truncation and
 * request further pages.
 */
final class PageHeaders {

    static final String TOTAL_COUNT = "X-Total-Count";
    static final String PAGE = "X-Page";
    static final String PAGE_SIZE = "X-Page-Size";

    private PageHeaders() {
    }

    static HttpHeaders of(PageResult<?> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(TOTAL_COUNT, Long.toString(result.totalElements()));
        headers.add(PAGE, Integer.toString(result.page()));
        headers.add(PAGE_SIZE, Integer.toString(result.size()));
        return headers;
    }
}
