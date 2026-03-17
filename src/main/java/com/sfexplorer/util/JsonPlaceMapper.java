package com.sfexplorer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfexplorer.model.Category;
import com.sfexplorer.model.Place;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Converts whatever fields the dataset provides into a common Place model.
 * This is intentionally tolerant: DataSF datasets don't share a single schema.
 */
public final class JsonPlaceMapper {
    private JsonPlaceMapper() {}

    public static List<Place> map(Category category, JsonNode rootArray, String sourceUrl) {
        List<Place> out = new ArrayList<>();
        if (rootArray == null || !rootArray.isArray()) return out;

        for (JsonNode obj : rootArray) {
            if (obj == null || !obj.isObject()) continue;

            String name = switch (category) {
                case EAT_AND_DRINK -> pick(obj, "business_name", "restaurant_name", "name", "dba_name");
                case ATTRACTIONS -> pick(obj, "landmark_name", "name", "resource_name", "title");
                case EVENTS -> pick(obj, "event_name", "name", "title");
                case OUTDOOR -> pick(obj, "facility_name", "park_name", "name", "common_name");
            };

            if (name.isBlank()) name = "(Unnamed)";

            String address = buildAddress(obj);

            String desc = switch (category) {
                case EVENTS -> pick(obj, "event_description", "description", "details");
                default -> pick(obj, "description", "notes", "summary", "historic_context");
            };

            String extra = switch (category) {
                case EAT_AND_DRINK -> {
                    String score = pick(obj, "inspection_score", "score", "rating");
                    String date = pick(obj, "inspection_date", "date", "event_start_date");
                    String risk = pick(obj, "risk_category", "risk");
                    yield joinNonBlank("Score: " + score, "Risk: " + risk, "Date: " + trimIso(date));
                }
                case EVENTS -> {
                    String start = pick(obj, "event_start_date", "start_date", "start");
                    String end = pick(obj, "event_end_date", "end_date", "end");
                    String org = pick(obj, "org_name", "organization");
                    yield joinNonBlank(
                            org.isBlank() ? "" : "Org: " + org,
                            start.isBlank() ? "" : "Starts: " + trimIso(start),
                            end.isBlank() ? "" : "Ends: " + trimIso(end)
                    );
                }
                case OUTDOOR -> {
                    String type = pick(obj, "facility_type", "type");
                    String hours = pick(obj, "hours", "open_hours");
                    yield joinNonBlank(
                            type.isBlank() ? "" : "Type: " + type,
                            hours.isBlank() ? "" : "Hours: " + hours
                    );
                }
                case ATTRACTIONS -> {
                    String year = pick(obj, "year_built", "year", "designation_date");
                    String status = pick(obj, "status", "designation");
                    yield joinNonBlank(
                            year.isBlank() ? "" : "Year: " + year,
                            status.isBlank() ? "" : "Status: " + status
                    );
                }
            };

            out.add(new Place(category, name, address, desc, extra, sourceUrl));
        }

        return out;
    }

    private static String pick(JsonNode obj, String... keys) {
        for (String k : keys) {
            JsonNode v = obj.get(k);
            if (v != null && !v.isNull()) {
                String s = v.asText("").trim();
                if (!s.isBlank()) return s;
            }
        }
        return "";
    }

    private static String buildAddress(JsonNode obj) {
        // Try common Socrata patterns
        String address = pick(obj, "business_address", "address", "site_address", "location_address", "street_address");
        String city = pick(obj, "business_city", "city");
        String zip = pick(obj, "business_postal_code", "zip", "zipcode", "postal_code");

        if (address.isBlank()) {
            // Some datasets store nested location objects
            JsonNode loc = obj.get("location");
            if (loc != null && loc.isObject()) {
                address = pick(loc, "human_address", "address");
                if (address.startsWith("{") && address.contains("address")) {
                    // human_address can be a JSON string; keep it short if possible
                    address = address.replace("\n", " ").replace("\\", "");
                }
            }
        }

        return joinNonBlank(address, city, zip);
    }

    private static String joinNonBlank(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (t.isBlank()) continue;
            if (sb.length() > 0) sb.append("  ");
            sb.append(t);
        }
        return sb.toString().trim();
    }

    private static String trimIso(String maybeIso) {
        if (maybeIso == null) return "";
        String s = maybeIso.trim();
        // Example: 2025-04-11T19:40:52.000
        int t = s.indexOf('T');
        if (t > 0) return s.substring(0, t);
        return s;
    }
}
