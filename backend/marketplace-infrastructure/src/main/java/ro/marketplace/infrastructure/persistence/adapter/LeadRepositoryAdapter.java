package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;
import ro.marketplace.domain.port.LeadRepository;
import ro.marketplace.infrastructure.persistence.PersistenceLimits;
import ro.marketplace.infrastructure.persistence.entity.LeadClaimEntity;
import ro.marketplace.infrastructure.persistence.entity.LeadEntity;
import ro.marketplace.infrastructure.persistence.mapper.EntityMapper;
import ro.marketplace.infrastructure.persistence.repository.SpringDataLeadClaimRepository;
import ro.marketplace.infrastructure.persistence.repository.SpringDataLeadRepository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LeadRepositoryAdapter implements LeadRepository {

    private final SpringDataLeadRepository springDataLeadRepository;
    private final SpringDataLeadClaimRepository springDataLeadClaimRepository;

    public LeadRepositoryAdapter(SpringDataLeadRepository springDataLeadRepository,
                                 SpringDataLeadClaimRepository springDataLeadClaimRepository) {
        this.springDataLeadRepository = Objects.requireNonNull(springDataLeadRepository, "springDataLeadRepository must not be null");
        this.springDataLeadClaimRepository = Objects.requireNonNull(springDataLeadClaimRepository, "springDataLeadClaimRepository must not be null");
    }

    @Override
    @Transactional
    public Lead save(Lead lead) {
        Objects.requireNonNull(lead, "lead must not be null");
        LeadEntity entity = EntityMapper.toEntity(lead);
        LeadEntity saved = springDataLeadRepository.save(entity);
        return EntityMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lead> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return springDataLeadRepository.findById(id).map(EntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lead> findAll() {
        return springDataLeadRepository.findAll(PersistenceLimits.firstPage()).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lead> findByStatus(LeadStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        return springDataLeadRepository.findByStatus(status.getDbValue(), PersistenceLimits.firstPage()).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lead> findByCategoryId(String categoryId) {
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        return springDataLeadRepository.findByCategoryId(categoryId, PersistenceLimits.firstPage()).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        springDataLeadRepository.deleteById(id);
    }

    @Override
    @Transactional
    public LeadClaim saveClaim(LeadClaim claim) {
        Objects.requireNonNull(claim, "claim must not be null");
        LeadClaimEntity entity = EntityMapper.toEntity(claim);
        LeadClaimEntity saved = springDataLeadClaimRepository.save(entity);
        return EntityMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadClaim> findClaimsByLeadId(UUID leadId) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        return springDataLeadClaimRepository.findByLeadId(leadId).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadClaim> findClaimsByLeadIds(Collection<UUID> leadIds) {
        Objects.requireNonNull(leadIds, "leadIds must not be null");
        if (leadIds.isEmpty()) {
            return List.of();
        }
        return springDataLeadClaimRepository.findByLeadIdIn(leadIds).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsClaimByLeadIdAndProId(UUID leadId, UUID proId) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        Objects.requireNonNull(proId, "proId must not be null");
        return springDataLeadClaimRepository.existsByLeadIdAndProId(leadId, proId);
    }

    @Override
    @Transactional
    public boolean reserveSlot(UUID leadId) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        return springDataLeadRepository.reserveSlot(leadId) == 1;
    }

    @Override
    @Transactional
    public void releaseSlot(UUID leadId) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        springDataLeadRepository.releaseSlot(leadId);
    }

    @Override
    @Transactional
    public void markAssignedIfFull(UUID leadId) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        springDataLeadRepository.markAssignedIfFull(leadId);
    }
}
