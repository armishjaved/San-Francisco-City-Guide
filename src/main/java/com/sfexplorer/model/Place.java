package com.sfexplorer.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;

public class Place {
    private final Category category;
    private final String name;
    private final String address;
    private final String description;
    private final String extra; // rating/date/etc
    private final String sourceUrl;

    public Place(Category category, String name, String address, String description, String extra, String sourceUrl) {
        this.category = category;
        this.name = name == null ? "" : name;
        this.address = address == null ? "" : address;
        this.description = description == null ? "" : description;
        this.extra = extra == null ? "" : extra;
        this.sourceUrl = sourceUrl == null ? "" : sourceUrl;
    }

    public Category getCategory() { return category; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getExtra() { return extra; }
    public String getSourceUrl() { return sourceUrl; }

    /**
     * Stable identifier derived from category + name + address.
     * Useful for database keys and concurrency-safe counters.
     */
    public String getPlaceId() {
        String raw = category.name() + "|" + name + "|" + address;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            // URL-safe and compact
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            // Extremely unlikely; fall back to hashCode
            return Integer.toHexString(raw.hashCode());
        }
    }

    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name.isBlank() ? "(Unnamed)" : name).append("\n\n");
        if (!address.isBlank()) sb.append("Address: ").append(address).append("\n");
        if (!extra.isBlank()) sb.append(extra).append("\n");
        if (!description.isBlank()) sb.append("\n").append(description).append("\n");
        if (!sourceUrl.isBlank()) sb.append("\nSource: ").append(sourceUrl).append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        if (!extra.isBlank()) return name + "  •  " + extra;
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Place)) return false;
        Place place = (Place) o;
        return category == place.category &&
                Objects.equals(name, place.name) &&
                Objects.equals(address, place.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, name, address);
    }
}
