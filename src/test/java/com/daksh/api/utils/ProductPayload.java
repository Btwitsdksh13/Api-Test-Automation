package com.daksh.api.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/** Builds request bodies so tests stay readable. */
public final class ProductPayload {

    private ProductPayload() {}

    public static Map<String, Object> valid() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", "Test Product");
        body.put("price", 13.5);
        body.put("category", "electronics");
        body.put("brand", "QA Labs");
        return body;
    }
}
