package com.panalsuite.users.service;

import com.panalsuite.users.dto.TeamRequest;
import com.panalsuite.users.exception.ApiException;
import com.panalsuite.users.model.Role;
import com.panalsuite.users.model.Team;
import com.panalsuite.users.model.User;
import com.panalsuite.users.repository.TeamRepository;
import com.panalsuite.users.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {
    private final TeamRepository teams;
    private final UserRepository users;
    private final SequenceService sequences;

    public TeamService(TeamRepository teams, UserRepository users, SequenceService sequences) {
        this.teams = teams;
        this.users = users;
        this.sequences = sequences;
    }

    public List<Team> findAll() {
        return teams.findAll(Sort.by("name"));
    }

    public Team get(Long id) {
        return teams.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipo no encontrado: " + id));
    }

    public Team create(TeamRequest req) {
        String name = req.name().trim();
        if (teams.existsByNameIgnoreCase(name)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un equipo con ese nombre");
        }
        Team team = new Team(sequences.next("teams"), name, validMembers(req.memberIds()));
        return teams.save(team);
    }

    public Team update(Long id, TeamRequest req) {
        Team team = get(id);
        String name = req.name().trim();
        teams.findByNameIgnoreCase(name)
                .filter(other -> !other.getIdTeam().equals(id))
                .ifPresent(other -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Ya existe un equipo con ese nombre");
                });
        team.setName(name);
        team.setMemberIds(validMembers(req.memberIds()));
        return teams.save(team);
    }

    public void delete(Long id) {
        teams.delete(get(id));
    }

    // Los miembros deben existir y ser operadores
    private List<Long> validMembers(List<Long> ids) {
        List<Long> distinct = ids == null ? new ArrayList<>() : new ArrayList<>(ids.stream().distinct().toList());
        List<User> found = users.findAllById(distinct);
        boolean allOperators = found.size() == distinct.size()
                && found.stream().allMatch(u -> u.getRole() == Role.Operator);
        if (!allOperators) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Todos los miembros deben ser operadores existentes");
        }
        return distinct;
    }
}
