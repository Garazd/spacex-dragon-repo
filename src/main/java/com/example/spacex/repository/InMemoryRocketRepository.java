package com.example.spacex.repository;

import com.example.spacex.model.Rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryRocketRepository implements RocketRepository {
    private final ConcurrentMap<UUID, Rocket> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Rocket> findById(final UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Rocket> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Rocket save(final Rocket rocket) {
        store.put(rocket.getId(), rocket);
        return rocket;
    }

    @Override
    public void update(final Rocket rocket) {
        store.put(rocket.getId(), rocket);
    }
}
