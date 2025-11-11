package com.example.spacex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static com.example.spacex.model.RocketStatus.ON_GROUND;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Rocket {
    private UUID id;
    private String name;
    private RocketStatus status;
    private UUID assignedMissionId;

    public static Rocket create(final String name) {
        return Rocket.builder()
                .id(UUID.randomUUID())
                .name(name)
                .status(ON_GROUND)
                .assignedMissionId(null)
                .build();
    }
}
