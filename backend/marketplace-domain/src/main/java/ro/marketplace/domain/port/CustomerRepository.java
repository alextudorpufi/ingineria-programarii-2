package ro.marketplace.domain.port;

import ro.marketplace.domain.model.Customer;
import ro.marketplace.domain.model.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    PageResult<Customer> findPage(int page, int size);

    boolean existsByEmail(String email);

    void deleteById(UUID id);
}
