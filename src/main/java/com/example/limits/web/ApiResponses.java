package com.example.limits.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public final class ApiResponses {
    private ApiResponses() {}

    /** Построить URI ресурса: currentRequest + "/{id}" */
    public static URI buildLocationFromCurrent(String id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }

    /** 201 Created c Location, либо 200 OK — по флагу created */
    public static <T> ResponseEntity<T> createdOrOk(boolean created, String resourceId, T body) {
        if (created) {
            URI location = buildLocationFromCurrent(resourceId);
            return ResponseEntity.created(location).body(body);
        }
        return ResponseEntity.ok(body);
    }

    public static <T> ResponseEntity<T> created(String resourceId, T body) {
        return ResponseEntity.created(buildLocationFromCurrent(resourceId)).body(body);
    }
}
