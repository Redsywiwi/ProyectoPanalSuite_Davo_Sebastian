package com.panalsuite.users.config;

import com.panalsuite.users.model.Role;
import com.panalsuite.users.model.Team;
import com.panalsuite.users.model.User;
import com.panalsuite.users.repository.TeamRepository;
import com.panalsuite.users.repository.UserRepository;
import com.panalsuite.users.service.SequenceService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// Carga los mismos usuarios de prueba que la app (assets/users.json) si la coleccion esta vacia
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final TeamRepository teams;
    private final SequenceService sequences;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, TeamRepository teams, SequenceService sequences, PasswordEncoder encoder) {
        this.users = users;
        this.teams = teams;
        this.sequences = sequences;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() == 0) {
            String hash = encoder.encode("1");
            users.saveAll(List.of(
                    new User(1L, "Redsy", "Raccoon", "commonuser@gmail.com", hash, Role.CommonUser, true),
                    new User(2L, "Aki", "Repanda", "commonuser2@gmail.com", hash, Role.CommonUser, true),
                    new User(3L, "Myo", "Bearsu", "commonuser3@gmail.com", hash, Role.CommonUser, true),
                    new User(4L, "Kofi", "Wah", "operator@gmail.com", hash, Role.Operator, true),
                    new User(5L, "Macu", "Waton", "operator2@gmail.com", hash, Role.Operator, true),
                    new User(6L, "Manchas", "Yuchudui", "operator3@gmail.com", hash, Role.Operator, true),
                    new User(7L, "Naru", "Waton", "operator4@gmail.com", hash, Role.Operator, true),
                    new User(8L, "Yoba", "Waton", "operator5@gmail.com", hash, Role.Operator, true),
                    new User(9L, "Goku", "le gana", "supervisor@gmail.com", hash, Role.Supervisor, true),
                    new User(10L, "Rambley", "the raccoon", "management@gmail.com", hash, Role.Management, true),
                    new User(11L, "Livelyapio", "Panzon", "admin@gmail.com", hash, Role.Administrator, true)));
            sequences.ensureAtLeast("users", 11);
        }
        if (teams.count() == 0) {
            teams.saveAll(List.of(
                    new Team(1L, "Team 1", List.of(4L, 5L, 7L)),
                    new Team(2L, "Team 2", List.of(6L, 4L, 8L)),
                    new Team(3L, "Team 3", List.of(7L, 8L, 5L))));
            sequences.ensureAtLeast("teams", 3);
        }
    }
}
