package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.Customer;
import ro.marketplace.domain.model.PageResult;
import ro.marketplace.domain.port.CustomerRepository;
import ro.marketplace.infrastructure.persistence.PageRequests;
import ro.marketplace.infrastructure.persistence.entity.CustomerEntity;
import ro.marketplace.infrastructure.persistence.mapper.EntityMapper;
import ro.marketplace.infrastructure.persistence.repository.SpringDataCustomerRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final SpringDataCustomerRepository springDataRepository;

    public CustomerRepositoryAdapter(SpringDataCustomerRepository springDataRepository) {
        this.springDataRepository = Objects.requireNonNull(springDataRepository, "springDataRepository must not be null");
    }

    @Override
    @Transactional
    public Customer save(Customer customer) {
        Objects.requireNonNull(customer, "customer must not be null");
        CustomerEntity entity = EntityMapper.toEntity(customer);
        CustomerEntity saved = springDataRepository.save(entity);
        return EntityMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return springDataRepository.findById(id).map(EntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Customer> findPage(int page, int size) {
        Page<CustomerEntity> result = springDataRepository.findAll(
                PageRequests.of(page, size,
                        Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        return new PageResult<>(
                result.getContent().stream().map(EntityMapper::toDomain).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        Objects.requireNonNull(email, "email must not be null");
        return springDataRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        springDataRepository.deleteById(id);
    }
}
