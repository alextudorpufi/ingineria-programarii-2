package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.Dispute;
import ro.marketplace.domain.model.DisputeStatus;
import ro.marketplace.domain.model.PageResult;
import ro.marketplace.domain.port.DisputeRepository;
import ro.marketplace.infrastructure.persistence.entity.DisputeEntity;
import ro.marketplace.infrastructure.persistence.mapper.EntityMapper;
import ro.marketplace.infrastructure.persistence.repository.SpringDataDisputeRepository;

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
    public PageResult<Dispute> findPage(int page, int size) {
        Page<DisputeEntity> result = springDataDisputeRepository.findAll(pageRequest(page, size));
        return toPage(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Dispute> findPageByProId(UUID proId, int page, int size) {
        Objects.requireNonNull(proId, "proId must not be null");
        Page<DisputeEntity> result = springDataDisputeRepository
                .findByProIdOrderByCreatedAtDesc(proId, pageRequest(page, size));
        return toPage(result);
    }

    private static PageRequest pageRequest(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.max(size, 1),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }

    private static PageResult<Dispute> toPage(Page<DisputeEntity> result) {
        return new PageResult<>(
                result.getContent().stream().map(EntityMapper::toDomain).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsOpenDispute(UUID leadId, UUID proId) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        Objects.requireNonNull(proId, "proId must not be null");
        return springDataDisputeRepository.existsByLeadIdAndProIdAndStatus(
                leadId, proId, DisputeStatus.IN_ANALIZA.getDbValue());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsOpenDisputeForLead(UUID leadId) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        return springDataDisputeRepository.existsByLeadIdAndStatus(
                leadId, DisputeStatus.IN_ANALIZA.getDbValue());
    }
}
