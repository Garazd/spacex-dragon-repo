package com.example.spacex.repository;

import com.example.spacex.model.Mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryMissionRepository implements MissionRepository {
    private final ConcurrentMap<UUID, Mission> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Mission> findById(final UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Mission> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Mission save(final Mission mission) {
        store.put(mission.getId(), mission);
        return mission;
    }

    @Override
    public void update(final Mission mission) {
        store.put(mission.getId(), mission);
    }
}
