package com.example.spacex.repository;

import com.example.spacex.model.Rocket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RocketRepository {
    Optional<Rocket> findById(UUID id);

    List<Rocket> findAll();

    Rocket save(Rocket rocket);

    void update(Rocket rocket);
}
