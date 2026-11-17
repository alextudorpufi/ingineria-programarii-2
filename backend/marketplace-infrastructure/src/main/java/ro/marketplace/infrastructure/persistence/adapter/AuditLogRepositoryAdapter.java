package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.AuditLog;
import ro.marketplace.domain.port.AuditLogRepository;
import ro.marketplace.infrastructure.persistence.PageRequests;
import ro.marketplace.infrastructure.persistence.entity.AuditLogEntity;
import ro.marketplace.infrastructure.persistence.mapper.EntityMapper;
import ro.marketplace.infrastructure.persistence.repository.SpringDataAuditLogRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class AuditLogRepositoryAdapter implements AuditLogRepository {

    private final SpringDataAuditLogRepository springDataAuditLogRepository;

    public AuditLogRepositoryAdapter(SpringDataAuditLogRepository springDataAuditLogRepository) {
        this.springDataAuditLogRepository = Objects.requireNonNull(springDataAuditLogRepository, "springDataAuditLogRepository must not be null");
    }

    @Override
    @Transactional
    public AuditLog append(AuditLog auditLog) {
        Objects.requireNonNull(auditLog, "auditLog must not be null");
        AuditLogEntity entity = EntityMapper.toEntity(auditLog);
        // Audit history is append-only: the insert-only entity never turns a repeated id into an UPDATE.
        springDataAuditLogRepository.save(entity);
        return EntityMapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findRecentByEntityNameAndEntityId(String entityName, String entityId, int limit) {
        Objects.requireNonNull(entityName, "entityName must not be null");
        Objects.requireNonNull(entityId, "entityId must not be null");
        int safeLimit = PageRequests.limit(limit, PageRequests.DEFAULT_LIMIT);
        return springDataAuditLogRepository.findByEntityNameAndEntityIdOrderByCreatedAtDescIdDesc(
                        entityName, entityId, PageRequest.of(0, safeLimit)).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findRecent(int limit) {
        int safeLimit = PageRequests.limit(limit, PageRequests.DEFAULT_LIMIT);
        return springDataAuditLogRepository.findRecentAuditLogs(PageRequest.of(0, safeLimit)).stream()
                .map(EntityMapper::toDomain)
                .toList();
    }
}
