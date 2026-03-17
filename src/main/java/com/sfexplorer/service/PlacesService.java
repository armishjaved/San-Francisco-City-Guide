package com.sfexplorer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfexplorer.model.Category;
import com.sfexplorer.model.Place;
import com.sfexplorer.util.JsonPlaceMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PlacesService {
    private final SocrataClient client = new SocrataClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // DataSF (Socrata) "resource" endpoints.
    // (App does NOT require an app token for small student use.)
    private static final String BASE = "https://data.sfgov.org/resource/";

    // Restaurants / inspections (historic but good demo data)
    private static final String RESTAURANTS = BASE + "pyih-qa8i.json?$limit=50";

    // Landmarks
    private static final String LANDMARKS = BASE + "rzic-39gi.json?$limit=50";

    // Our415 events (children/youth/family activities)
    private static final String EVENTS = BASE + "8i3s-ih2a.json?$limit=50";

    // Rec & Park facilities
    private static final String PARKS = BASE + "ib5c-xgwu.json?$limit=50";

    public List<Place> fetch(Category category) throws Exception {
        String url = switch (category) {
            case EAT_AND_DRINK -> RESTAURANTS;
            case ATTRACTIONS -> LANDMARKS;
            case EVENTS -> EVENTS;
            case OUTDOOR -> PARKS;
        };

        String json;
        try {
            json = client.get(url);
        } catch (Exception networkFail) {
            // Fallback to local sample data so the project always runs.
            json = loadLocalSample(category);
        }

        JsonNode root = mapper.readTree(json);
        return JsonPlaceMapper.map(category, root, url);
    }

    private String loadLocalSample(Category category) throws Exception {
        String path = switch (category) {
            case EAT_AND_DRINK -> "/sample-data/restaurants.json";
            case ATTRACTIONS -> "/sample-data/landmarks.json";
            case EVENTS -> "/sample-data/events.json";
            case OUTDOOR -> "/sample-data/parks.json";
        };

        try (InputStream is = PlacesService.class.getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Missing resource: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
