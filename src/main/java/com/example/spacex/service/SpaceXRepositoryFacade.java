package com.example.spacex.service;

import com.example.spacex.model.Mission;
import com.example.spacex.model.MissionStatus;
import com.example.spacex.model.Rocket;
import com.example.spacex.model.RocketStatus;
import com.example.spacex.repository.InMemoryMissionRepository;
import com.example.spacex.repository.InMemoryRocketRepository;
import com.example.spacex.repository.MissionRepository;
import com.example.spacex.repository.RocketRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SpaceXRepositoryFacade {
    private final RocketRepository rocketRepository;
    private final MissionRepository missionRepository;

    public SpaceXRepositoryFacade() {
        this.rocketRepository = new InMemoryRocketRepository();
        this.missionRepository = new InMemoryMissionRepository();
    }

    public SpaceXRepositoryFacade(final RocketRepository rocketRepository, final MissionRepository missionRepository) {
        this.rocketRepository = Objects.requireNonNull(rocketRepository, "rocketRepository must not be null");
        this.missionRepository = Objects.requireNonNull(missionRepository, "missionRepository must not be null");
    }

    public Rocket addRocket(final String rocketName) {
        final boolean exists = rocketRepository.findAll().stream()
                .anyMatch(rocket -> rocket.getName().equalsIgnoreCase(rocketName));
        if (exists) {
            throw new IllegalStateException("Rocket with name '" + rocketName + "' already exists.");
        }
        final Rocket rocket = Rocket.create(rocketName);
        return rocketRepository.save(rocket);
    }

    public Mission addMission(final String missionName) {
        final boolean exists = missionRepository.findAll().stream()
                .anyMatch(mission -> mission.getName().equalsIgnoreCase(missionName));
        if (exists) {
            throw new IllegalStateException("Mission with name '" + missionName + "' already exists.");
        }
        final Mission mission = Mission.create(missionName);
        return missionRepository.save(mission);
    }

    public void assignRocketToMission(final UUID rocketId, final UUID missionId) {
        final Rocket rocket = rocketRepository.findById(rocketId)
                .orElseThrow(() -> new IllegalArgumentException("Rocket not found"));
        final Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));

        if (mission.getStatus() == MissionStatus.ENDED) {
            throw new IllegalStateException("Cannot assign rockets to an ended mission");
        }

        if (rocket.getAssignedMissionId() != null) {
            throw new IllegalStateException("Rocket already assigned to another mission");
        }

        mission.getRocketIds().add(rocket.getId());
        rocket.setAssignedMissionId(mission.getId());

        rocketRepository.update(rocket);
        missionRepository.update(mission);

        updateMissionStatus(mission);
    }

    public void changeRocketStatus(final UUID rocketId, final RocketStatus newStatus) {
        final Rocket rocket = rocketRepository.findById(rocketId)
                .orElseThrow(() -> new IllegalArgumentException("Rocket not found"));

        rocket.setStatus(newStatus);
        rocketRepository.update(rocket);

        if (rocket.getAssignedMissionId() != null) {
            final Mission mission = missionRepository.findById(rocket.getAssignedMissionId())
                    .orElseThrow(() -> new IllegalArgumentException("Mission not found for rocket"));
            updateMissionStatus(mission);
        }
    }

    public void changeMissionStatus(final UUID missionId, final MissionStatus newStatus) {
        final Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        mission.setStatus(newStatus);
        missionRepository.update(mission);
    }

    private void updateMissionStatus(final Mission mission) {
        final boolean anyInRepair = mission.getRocketIds().stream()
                .map(rocketRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(r -> r.getStatus() == RocketStatus.IN_REPAIR);

        if (anyInRepair) {
            mission.setStatus(MissionStatus.PENDING);
        } else if (mission.getRocketIds().isEmpty()) {
            mission.setStatus(MissionStatus.SCHEDULED);
        } else {
            mission.setStatus(MissionStatus.IN_PROGRESS);
        }

        missionRepository.update(mission);
    }

    public List<Mission> summary() {
        return missionRepository.findAll().stream()
                .sorted(Comparator.comparingInt((Mission m) -> m.getRocketIds().size()).reversed()
                        .thenComparing(Mission::getName, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    public List<Rocket> listRocketsForMission(final UUID missionId) {
        final Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        return mission.getRocketIds().stream()
                .map(rocketRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Optional<Mission> getMission(final UUID missionId) {
        return missionRepository.findById(missionId);
    }

    public Optional<Rocket> getRocket(final UUID rocketId) {
        return rocketRepository.findById(rocketId);
    }
}
