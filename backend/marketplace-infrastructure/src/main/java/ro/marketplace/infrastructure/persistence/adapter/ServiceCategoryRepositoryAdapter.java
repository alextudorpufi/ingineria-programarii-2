package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.port.ServiceCategoryRepository;
import ro.marketplace.infrastructure.persistence.repository.SpringDataServiceCategoryRepository;

import java.util.Objects;

@Repository
public class ServiceCategoryRepositoryAdapter implements ServiceCategoryRepository {

    private final SpringDataServiceCategoryRepository springDataServiceCategoryRepository;

    public ServiceCategoryRepositoryAdapter(SpringDataServiceCategoryRepository springDataServiceCategoryRepository) {
        this.springDataServiceCategoryRepository = Objects.requireNonNull(
                springDataServiceCategoryRepository, "springDataServiceCategoryRepository must not be null");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return springDataServiceCategoryRepository.existsById(id);
    }
}
