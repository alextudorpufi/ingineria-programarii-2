package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.port.WalletTransactionRepository;
import ro.marketplace.infrastructure.persistence.PersistenceLimits;
import ro.marketplace.infrastructure.persistence.entity.WalletTransactionEntity;
import ro.marketplace.infrastructure.persistence.mapper.EntityMapper;
import ro.marketplace.infrastructure.persistence.repository.SpringDataWalletTransactionRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WalletTransactionRepositoryAdapter implements WalletTransactionRepository {

    private final SpringDataWalletTransactionRepository springDataWalletTransactionRepository;

    public WalletTransactionRepositoryAdapter(SpringDataWalletTransactionRepository springDataWalletTransactionRepository) {
        this.springDataWalletTransactionRepository = Objects.requireNonNull(springDataWalletTransactionRepository, "springDataWalletTransactionRepository must not be null");
    }

    @Override
    @Transactional
    public WalletTransaction save(WalletTransaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        WalletTransactionEntity entity = EntityMapper.toEntity(transaction);
        WalletTransactionEntity saved = springDataWalletTransactionRepository.save(entity);
        return EntityMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WalletTransaction> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return springDataWalletTransactionRepository.findById(id).map(EntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransaction> findByUserId(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return springDataWalletTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, PersistenceLimits.firstPage()).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }
}
