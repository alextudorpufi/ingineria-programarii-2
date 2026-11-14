package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.Dispute;
import ro.marketplace.domain.model.DisputeStatus;
import ro.marketplace.domain.port.DisputeRepository;
import ro.marketplace.infrastructure.persistence.PersistenceLimits;
import ro.marketplace.infrastructure.persistence.entity.DisputeEntity;
import ro.marketplace.infrastructure.persistence.mapper.EntityMapper;
import ro.marketplace.infrastructure.persistence.repository.SpringDataDisputeRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DisputeRepositoryAdapter implements DisputeRepository {

    private final SpringDataDisputeRepository springDataDisputeRepository;

    public DisputeRepositoryAdapter(SpringDataDisputeRepository springDataDisputeRepository) {
        this.springDataDisputeRepository = Objects.requireNonNull(springDataDisputeRepository, "springDataDisputeRepository must not be null");
    }

    @Override
    @Transactional
    public Dispute save(Dispute dispute) {
        Objects.requireNonNull(dispute, "dispute must not be null");
        // Update the managed entity instead of rebuilding it: rebuilding loses the
        // @Version value, which makes Spring Data treat the row as new and persist
        // an INSERT for an existing primary key.
        DisputeEntity entity = springDataDisputeRepository.findById(dispute.getId())
                .map(existing -> {
                    EntityMapper.updateEntity(dispute, existing);
                    return existing;
                })
                .orElseGet(() -> EntityMapper.toEntity(dispute));
        DisputeEntity saved = springDataDisputeRepository.save(entity);
        return EntityMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dispute> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return springDataDisputeRepository.findById(id).map(EntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> findAll() {
        return springDataDisputeRepository.findAll(PersistenceLimits.firstPage()).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> findByProId(UUID proId) {
        Objects.requireNonNull(proId, "proId must not be null");
        return springDataDisputeRepository.findByProIdOrderByCreatedAtDesc(proId, PersistenceLimits.firstPage()).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> findByStatus(String status) {
        Objects.requireNonNull(status, "status must not be null");
        return springDataDisputeRepository.findByStatus(status, PersistenceLimits.firstPage()).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsOpenDispute(UUID leadId, UUID proId) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        Objects.requireNonNull(proId, "proId must not be null");
        return springDataDisputeRepository.existsByLeadIdAndProIdAndStatus(
                leadId, proId, DisputeStatus.IN_ANALIZA.getDbValue());
    }
}
