package com.example.spacex.service;

import com.example.spacex.model.Mission;
import com.example.spacex.model.MissionStatus;
import com.example.spacex.model.Rocket;
import com.example.spacex.model.RocketStatus;
import com.example.spacex.repository.InMemoryMissionRepository;
import com.example.spacex.repository.InMemoryRocketRepository;
import com.example.spacex.repository.MissionRepository;
import com.example.spacex.repository.RocketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpaceXRepositoryFacadeTest {

    private SpaceXRepositoryFacade facade;

    @BeforeEach
    public void setUp() {
        final MissionRepository missionRepository = new InMemoryMissionRepository();
        final RocketRepository rocketRepository = new InMemoryRocketRepository();
        facade = new SpaceXRepositoryFacade(rocketRepository, missionRepository);
    }

    @Test
    public void shouldAddRocketsAndMissionsSuccessfully() {
        final Rocket rocket = facade.addRocket("Dragon One");
        final Mission mission = facade.addMission("Luna Mission");

        assertNotNull(rocket.getId());
        assertEquals(RocketStatus.ON_GROUND, rocket.getStatus());
        assertNotNull(mission.getId());
        assertEquals(MissionStatus.SCHEDULED, mission.getStatus());
    }

    @Test
    public void shouldThrowWhenAddingDuplicateRocketNames() {
        facade.addRocket("Duplicate");
        assertThrows(IllegalStateException.class, () -> facade.addRocket("Duplicate"));
    }

    @Test
    public void shouldThrowWhenAddingDuplicateMissionNames() {
        facade.addMission("DuplicateMission");
        assertThrows(IllegalStateException.class, () -> facade.addMission("DuplicateMission"));
    }

    @Test
    public void shouldAssignRocketToMissionAndChangeStatus() {
        final Rocket rocket = facade.addRocket("Falcon Heavy");
        final Mission mission = facade.addMission("Mars Transit");

        facade.assignRocketToMission(rocket.getId(), mission.getId());

        final Mission afterAssign = facade.getMission(mission.getId()).orElseThrow();
        assertEquals(1, afterAssign.getRocketIds().size());
        assertEquals(MissionStatus.IN_PROGRESS, afterAssign.getStatus());
    }

    @Test
    public void shouldNotAssignRocketTwice() {
        final Rocket rocket = facade.addRocket("Dragon X");
        final Mission m1 = facade.addMission("M1");
        final Mission m2 = facade.addMission("M2");

        facade.assignRocketToMission(rocket.getId(), m1.getId());

        assertThrows(IllegalStateException.class,
                () -> facade.assignRocketToMission(rocket.getId(), m2.getId()));
    }

    @Test
    public void shouldNotAssignRocketToEndedMission() {
        final Rocket rocket = facade.addRocket("Dragon Z");
        final Mission mission = facade.addMission("Test Mission");

        facade.changeMissionStatus(mission.getId(), MissionStatus.ENDED);

        assertThrows(IllegalStateException.class,
                () -> facade.assignRocketToMission(rocket.getId(), mission.getId()));
    }

    @Test
    public void shouldThrowWhenAssigningNonExistingRocketOrMission() {
        final UUID nonExistingRocket = UUID.randomUUID();
        final UUID nonExistingMission = UUID.randomUUID();
        final Mission mission = facade.addMission("ValidMission");
        final Rocket rocket = facade.addRocket("ValidRocket");

        assertThrows(IllegalArgumentException.class,
                () -> facade.assignRocketToMission(nonExistingRocket, mission.getId()));

        assertThrows(IllegalArgumentException.class,
                () -> facade.assignRocketToMission(rocket.getId(), nonExistingMission));
    }

    @Test
    public void shouldChangeRocketStatusToRepairAndUpdateMissionPending() {
        final Rocket rocket1 = facade.addRocket("Dragon 1");
        final Rocket rocket2 = facade.addRocket("Dragon 2");
        final Mission mission = facade.addMission("Luna 2");

        facade.assignRocketToMission(rocket1.getId(), mission.getId());
        facade.assignRocketToMission(rocket2.getId(), mission.getId());

        facade.changeRocketStatus(rocket1.getId(), RocketStatus.IN_REPAIR);

        final Mission updated = facade.getMission(mission.getId()).orElseThrow();
        assertEquals(MissionStatus.PENDING, updated.getStatus());
    }

    @Test
    public void shouldChangeRocketStatusAndKeepMissionInProgress() {
        final Rocket rocket = facade.addRocket("Falcon 9");
        final Mission mission = facade.addMission("Mars I");
        facade.assignRocketToMission(rocket.getId(), mission.getId());

        facade.changeRocketStatus(rocket.getId(), RocketStatus.IN_SPACE);

        final Mission updated = facade.getMission(mission.getId()).orElseThrow();
        assertEquals(MissionStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    public void shouldSetMissionStatusToScheduledWhenNoRocketsAssignedAfterUpdate() {
        final Mission mission = facade.addMission("EmptyMission");

        assertTrue(mission.getRocketIds().isEmpty());

        final Rocket rocket = facade.addRocket("TempRocket");
        facade.assignRocketToMission(rocket.getId(), mission.getId());

        mission.getRocketIds().clear();

        facade.changeRocketStatus(rocket.getId(), RocketStatus.ON_GROUND);

        final Mission updated = facade.getMission(mission.getId()).orElseThrow();
        assertEquals(MissionStatus.SCHEDULED, updated.getStatus());
    }

    @Test
    public void shouldThrowWhenChangingStatusOfNonExistingRocket() {
        assertThrows(IllegalArgumentException.class,
                () -> facade.changeRocketStatus(UUID.randomUUID(), RocketStatus.IN_SPACE));
    }

    @Test
    public void shouldChangeMissionStatusManually() {
        final Mission mission = facade.addMission("ManualChange");
        facade.changeMissionStatus(mission.getId(), MissionStatus.ENDED);
        final Mission updated = facade.getMission(mission.getId()).orElseThrow();
        assertEquals(MissionStatus.ENDED, updated.getStatus());
    }

    @Test
    public void shouldThrowWhenChangingStatusOfNonExistingMission() {
        assertThrows(IllegalArgumentException.class,
                () -> facade.changeMissionStatus(UUID.randomUUID(), MissionStatus.PENDING));
    }

    @Test
    public void shouldReturnSummaryOrderedByRocketCountThenName() {
        final Mission m1 = facade.addMission("Alpha");
        final Mission m2 = facade.addMission("Beta");
        final Rocket r1 = facade.addRocket("R1");
        final Rocket r2 = facade.addRocket("R2");

        facade.assignRocketToMission(r1.getId(), m1.getId());
        facade.assignRocketToMission(r2.getId(), m1.getId());

        final List<Mission> summary = facade.summary();

        assertFalse(summary.isEmpty());
        assertEquals(2, summary.size());
        assertEquals(m1.getId(), summary.getFirst().getId());
        assertEquals(m2.getId(), summary.getLast().getId());
    }

    @Test
    public void shouldHandleSummaryWithNoMissions() {
        final List<Mission> summary = facade.summary();
        assertTrue(summary.isEmpty());
    }

    @Test
    public void shouldListRocketsForMission() {
        final Rocket r1 = facade.addRocket("Dragon A");
        final Rocket r2 = facade.addRocket("Dragon B");
        final Mission mission = facade.addMission("Orbital Flight");

        facade.assignRocketToMission(r1.getId(), mission.getId());
        facade.assignRocketToMission(r2.getId(), mission.getId());

        final List<Rocket> rockets = facade.listRocketsForMission(mission.getId());
        assertEquals(2, rockets.size());
        assertTrue(rockets.stream().anyMatch(r -> r.getName().equals("Dragon A")));
    }

    @Test
    public void shouldThrowWhenListingRocketsForNonExistingMission() {
        assertThrows(IllegalArgumentException.class,
                () -> facade.listRocketsForMission(UUID.randomUUID()));
    }

    @Test
    public void shouldGetMissionAndRocketById() {
        final Mission mission = facade.addMission("RetrieveMission");
        final Rocket rocket = facade.addRocket("RetrieveRocket");

        final Optional<Mission> missionOpt = facade.getMission(mission.getId());
        final Optional<Rocket> rocketOpt = facade.getRocket(rocket.getId());

        assertTrue(missionOpt.isPresent());
        assertTrue(rocketOpt.isPresent());
        assertEquals(mission.getName(), missionOpt.get().getName());
        assertEquals(rocket.getName(), rocketOpt.get().getName());
    }

    @Test
    public void shouldReturnEmptyWhenMissionOrRocketNotFound() {
        assertTrue(facade.getMission(UUID.randomUUID()).isEmpty());
        assertTrue(facade.getRocket(UUID.randomUUID()).isEmpty());
    }
}
