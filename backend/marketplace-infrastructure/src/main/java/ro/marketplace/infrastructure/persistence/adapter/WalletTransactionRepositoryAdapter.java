package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.PageResult;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.port.WalletTransactionRepository;
import ro.marketplace.infrastructure.persistence.entity.WalletTransactionEntity;
import ro.marketplace.infrastructure.persistence.mapper.EntityMapper;
import ro.marketplace.infrastructure.persistence.repository.SpringDataWalletTransactionRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class WalletTransactionRepositoryAdapter implements WalletTransactionRepository {

    private final SpringDataWalletTransactionRepository springDataWalletTransactionRepository;

    public WalletTransactionRepositoryAdapter(SpringDataWalletTransactionRepository springDataWalletTransactionRepository) {
        this.springDataWalletTransactionRepository = Objects.requireNonNull(springDataWalletTransactionRepository, "springDataWalletTransactionRepository must not be null");
    }

    @Override
    @Transactional
    public WalletTransaction append(WalletTransaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        WalletTransactionEntity entity = EntityMapper.toEntity(transaction);
        // The ledger is append-only: persist never turns a repeated id into an UPDATE.
        springDataWalletTransactionRepository.save(entity);
        return EntityMapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<WalletTransaction> findPageByUserId(UUID userId, int page, int size) {
        Objects.requireNonNull(userId, "userId must not be null");
        Page<WalletTransactionEntity> result = springDataWalletTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(Math.max(page, 0), Math.max(size, 1)));
        return new PageResult<>(
                result.getContent().stream().map(EntityMapper::toDomain).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransaction> findByUserIdAndLeadId(UUID userId, UUID leadId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(leadId, "leadId must not be null");
        return springDataWalletTransactionRepository.findByUserIdAndLeadIdOrderByCreatedAtAsc(userId, leadId).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }
}
