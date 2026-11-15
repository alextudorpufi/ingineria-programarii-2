package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.ServiceCategoryEntity;

import java.util.Optional;

@Repository
public interface SpringDataServiceCategoryRepository extends JpaRepository<ServiceCategoryEntity, String> {

    @Query("select c.avgLeadPrice from ServiceCategoryEntity c where c.id = :id")
    Optional<Integer> findAvgLeadPriceById(@Param("id") String id);
}
