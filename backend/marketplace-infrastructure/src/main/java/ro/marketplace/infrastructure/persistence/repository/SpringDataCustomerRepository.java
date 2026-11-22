package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.CustomerEntity;

import java.util.UUID;

@Repository
public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    boolean existsByEmail(String email);
}
