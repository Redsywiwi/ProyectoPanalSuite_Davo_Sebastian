package com.panalsuite.users.service;

import com.panalsuite.users.dto.LoginRequest;
import com.panalsuite.users.dto.UserRequest;
import com.panalsuite.users.dto.UserResponse;
import com.panalsuite.users.exception.ApiException;
import com.panalsuite.users.model.Role;
import com.panalsuite.users.model.User;
import com.panalsuite.users.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;
    private final SequenceService sequences;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, SequenceService sequences, PasswordEncoder encoder) {
        this.users = users;
        this.sequences = sequences;
        this.encoder = encoder;
    }

    public List<UserResponse> findAll() {
        return users.findAll(Sort.by("idUser")).stream().map(UserResponse::from).toList();
    }

    public UserResponse get(Long id) {
        return UserResponse.from(find(id));
    }

    public UserResponse create(UserRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
        long id = sequences.next("users");
        // Igual que la app: si no se indica correo se genera "<id>@gmail.com"
        String email = isBlank(req.email()) ? id + "@gmail.com" : req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        User user = new User(id, req.name().trim(), req.lastName().trim(), email,
                encoder.encode(req.password()), req.role(), req.active() == null || req.active());
        return UserResponse.from(users.save(user));
    }

    public UserResponse update(Long id, UserRequest req) {
        User user = find(id);
        String email = isBlank(req.email()) ? user.getEmail() : req.email().trim().toLowerCase();
        users.findByEmail(email)
                .filter(other -> !other.getIdUser().equals(id))
                .ifPresent(other -> {
                    throw new ApiException(HttpStatus.CONFLICT, "El correo ya está registrado");
                });
        boolean newActive = req.active() == null ? user.isActive() : req.active();
        if (isActiveAdmin(user) && (req.role() != Role.Administrator || !newActive)) {
            ensureNotLastAdmin();
        }
        user.setName(req.name().trim());
        user.setLastName(req.lastName().trim());
        user.setEmail(email);
        user.setRole(req.role());
        user.setActive(newActive);
        if (!isBlank(req.password())) {
            user.setPasswordHash(encoder.encode(req.password()));
        }
        return UserResponse.from(users.save(user));
    }

    public void delete(Long id) {
        User user = find(id);
        if (isActiveAdmin(user)) {
            ensureNotLastAdmin();
        }
        users.delete(user);
    }

    public UserResponse authenticate(LoginRequest req) {
        String key = req.identifier().trim();
        Optional<User> found = users.findByEmail(key.toLowerCase());
        if (found.isEmpty()) {
            found = parseId(key).flatMap(users::findById);
        }
        User user = found
                .filter(User::isActive)
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
        return UserResponse.from(user);
    }

    private User find(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + id));
    }

    private boolean isActiveAdmin(User user) {
        return user.getRole() == Role.Administrator && user.isActive();
    }

    // El sistema nunca puede quedarse sin un administrador activo
    private void ensureNotLastAdmin() {
        if (users.countByRoleAndActive(Role.Administrator, true) <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "No se puede modificar al último administrador activo");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static Optional<Long> parseId(String s) {
        try {
            return Optional.of(Long.parseLong(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
