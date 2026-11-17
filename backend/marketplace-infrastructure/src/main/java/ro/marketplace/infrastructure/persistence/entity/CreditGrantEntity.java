package ro.marketplace.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_grants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditGrantEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "admin_id")
    private UUID adminId;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    /**
     * Append-only/create-only entity: a repeated id must fail instead of
     * silently turning the save into an UPDATE.
     */
    @Override
    public boolean isNew() {
        return true;
    }
}
