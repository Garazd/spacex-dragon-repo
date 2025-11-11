package com.example.spacex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static com.example.spacex.model.MissionStatus.SCHEDULED;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Mission {
    private UUID id;
    private String name;
    private MissionStatus status;
    private Set<UUID> rocketIds;

    public static Mission create(final String name) {
        return Mission.builder()
                .id(UUID.randomUUID())
                .name(name)
                .status(SCHEDULED)
                .rocketIds(Collections.synchronizedSet(new LinkedHashSet<>()))
                .build();
    }
}
