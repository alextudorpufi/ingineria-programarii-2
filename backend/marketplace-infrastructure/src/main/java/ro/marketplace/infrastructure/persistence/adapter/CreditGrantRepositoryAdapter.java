package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.CreditGrant;
import ro.marketplace.domain.port.CreditGrantRepository;
import ro.marketplace.infrastructure.persistence.entity.CreditGrantEntity;
import ro.marketplace.infrastructure.persistence.mapper.EntityMapper;
import ro.marketplace.infrastructure.persistence.repository.SpringDataCreditGrantRepository;

import java.util.Objects;
import java.util.UUID;

@Repository
public class CreditGrantRepositoryAdapter implements CreditGrantRepository {

    private final SpringDataCreditGrantRepository springDataCreditGrantRepository;

    public CreditGrantRepositoryAdapter(SpringDataCreditGrantRepository springDataCreditGrantRepository) {
        this.springDataCreditGrantRepository = Objects.requireNonNull(springDataCreditGrantRepository,
                "springDataCreditGrantRepository must not be null");
    }

    @Override
    @Transactional
    public CreditGrant append(CreditGrant grant) {
        Objects.requireNonNull(grant, "grant must not be null");
        CreditGrantEntity entity = EntityMapper.toEntity(grant);
        // One lifetime grant per user is enforced by a unique index; insert-only
        // semantics make an accidental re-save fail instead of mutating the grant.
        springDataCreditGrantRepository.save(entity);
        return EntityMapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public long sumAmountByUserId(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return springDataCreditGrantRepository.sumAmountByUserId(userId);
    }
}
