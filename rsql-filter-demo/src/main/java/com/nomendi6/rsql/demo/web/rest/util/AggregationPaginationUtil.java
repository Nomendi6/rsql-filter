package com.nomendi6.rsql.demo.web.rest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;

public final class AggregationPaginationUtil {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AggregationPaginationUtil.class);

    public static void addAggregationToHeaders(HttpHeaders headers, Map<String, Object> aggregatedTotal) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(aggregatedTotal);
            headers.add("X-AGGREGATION", json);
        } catch (Exception e) {
            log.error("Error converting aggregatedTotal to JSON: " + aggregatedTotal, e);
        }
    }

    public static void addCustomAggregationToHeaders(HttpHeaders headers, String headerName, Map<String, Object> aggregatedTotal) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(aggregatedTotal);
            headers.add(headerName, json);
        } catch (Exception e) {
            log.error("Error converting custom aggregation to JSON: " + aggregatedTotal, e);
        }
    }
}
