package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.port.UserRepository;
import ro.marketplace.infrastructure.persistence.PersistenceLimits;
import ro.marketplace.infrastructure.persistence.entity.UserEntity;
import ro.marketplace.infrastructure.persistence.mapper.EntityMapper;
import ro.marketplace.infrastructure.persistence.repository.SpringDataUserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = Objects.requireNonNull(springDataUserRepository, "springDataUserRepository must not be null");
    }

    @Override
    @Transactional
    public User save(User user) {
        Objects.requireNonNull(user, "user must not be null");
        UserEntity entity = EntityMapper.toEntity(user);
        UserEntity saved = springDataUserRepository.save(entity);
        return EntityMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return springDataUserRepository.findById(id).map(EntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email, "email must not be null");
        return springDataUserRepository.findByEmail(email).map(EntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return springDataUserRepository.findAll(PersistenceLimits.firstPage()).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(String role) {
        Objects.requireNonNull(role, "role must not be null");
        return springDataUserRepository.findByRole(role, PersistenceLimits.firstPage()).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        Objects.requireNonNull(email, "email must not be null");
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        springDataUserRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean addCredits(UUID userId, int amount) {
        Objects.requireNonNull(userId, "userId must not be null");
        return springDataUserRepository.addCredits(userId, amount) == 1;
    }

    @Override
    @Transactional
    public boolean deductCredits(UUID userId, int amount) {
        Objects.requireNonNull(userId, "userId must not be null");
        return springDataUserRepository.deductCredits(userId, amount) == 1;
    }

    @Override
    @Transactional
    public boolean registerFailedLogin(UUID userId, int maxAttempts, LocalDateTime lockoutUntil) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(lockoutUntil, "lockoutUntil must not be null");
        return springDataUserRepository.registerFailedLogin(userId, maxAttempts, lockoutUntil) == 1;
    }

    @Override
    @Transactional
    public boolean resetLoginState(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return springDataUserRepository.resetLoginState(userId) == 1;
    }

    @Override
    @Transactional
    public boolean verifyProAndAddCredits(UUID userId, int amount) {
        Objects.requireNonNull(userId, "userId must not be null");
        return springDataUserRepository.verifyProAndAddCredits(userId, amount) == 1;
    }
}
