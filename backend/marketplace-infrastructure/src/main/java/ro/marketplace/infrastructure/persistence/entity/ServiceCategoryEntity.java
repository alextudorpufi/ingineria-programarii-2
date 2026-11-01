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
import ro.marketplace.infrastructure.persistence.audit.AuditableEntity;

@Entity
@Table(name = "service_categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon_name")
    private String iconName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "avg_lead_price", nullable = false)
    private int avgLeadPrice;
}
