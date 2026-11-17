package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.port.UserRepository;
import ro.marketplace.infrastructure.persistence.entity.UserEntity;
import ro.marketplace.infrastructure.persistence.mapper.EntityMapper;
import ro.marketplace.infrastructure.persistence.mapper.ProfessionalProfileMapper;
import ro.marketplace.infrastructure.persistence.repository.SpringDataUserRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = Objects.requireNonNull(springDataUserRepository, "springDataUserRepository must not be null");
    }

    @Override
    @Transactional
    public User create(User user) {
        Objects.requireNonNull(user, "user must not be null");
        UserEntity entity = EntityMapper.toEntity(user);
        UserEntity saved = springDataUserRepository.save(entity);
        return EntityMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public User updateProfessionalProfile(User user, long expectedProfileVersion) {
        UserEntity entity = springDataUserRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profilul nu a fost găsit."));
        var profile = entity.getProfessionalProfile();
        if (profile == null || profile.getVersion() == null || profile.getVersion() != expectedProfileVersion) {
            throw new ConflictException("Profilul a fost modificat între timp. Reîncarcă datele înainte de a salva.");
        }
        entity.setName(user.getName());
        entity.setCompanyName(user.getCompanyName());
        entity.setTitle(user.getTitle());
        entity.setPhone(user.getPhone());
        entity.setBio(user.getBio());
        ProfessionalProfileMapper.updateEntity(user.getProfessionalProfile(), profile);
        // Touch every save, including identity-only edits, so @Version protects the whole form.
        profile.setUpdatedAt(LocalDateTime.now());
        springDataUserRepository.flush();
        return EntityMapper.toDomain(entity);
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
    public boolean existsByEmail(String email) {
        Objects.requireNonNull(email, "email must not be null");
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public OptionalInt addCredits(UUID userId, int amount) {
        Objects.requireNonNull(userId, "userId must not be null");
        return springDataUserRepository.findByIdForUpdate(userId)
                .map(user -> {
                    user.setCreditsBalance(user.getCreditsBalance() + amount);
                    springDataUserRepository.flush();
                    return OptionalInt.of(user.getCreditsBalance());
                })
                .orElseGet(OptionalInt::empty);
    }

    @Override
    @Transactional
    public OptionalInt deductCredits(UUID userId, int amount) {
        Objects.requireNonNull(userId, "userId must not be null");
        return springDataUserRepository.findByIdForUpdate(userId)
                .map(user -> {
                    if (user.getCreditsBalance() < amount) {
                        return OptionalInt.empty();
                    }
                    user.setCreditsBalance(user.getCreditsBalance() - amount);
                    springDataUserRepository.flush();
                    return OptionalInt.of(user.getCreditsBalance());
                })
                .orElseGet(OptionalInt::empty);
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
    public boolean verifyPro(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return springDataUserRepository.verifyPro(userId) == 1;
    }
}
