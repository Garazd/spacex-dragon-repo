package com.example.spacex.repository;

import com.example.spacex.model.Mission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionRepository {
    Optional<Mission> findById(UUID id);

    List<Mission> findAll();

    Mission save(Mission mission);

    void update(Mission mission);
}
