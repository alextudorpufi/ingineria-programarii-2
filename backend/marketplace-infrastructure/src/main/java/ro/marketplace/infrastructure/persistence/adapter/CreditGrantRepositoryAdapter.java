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
    public CreditGrant save(CreditGrant grant) {
        Objects.requireNonNull(grant, "grant must not be null");
        CreditGrantEntity saved = springDataCreditGrantRepository.save(EntityMapper.toEntity(grant));
        return EntityMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public long sumAmountByUserId(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return springDataCreditGrantRepository.sumAmountByUserId(userId);
    }
}
