package ro.marketplace.domain.port;

import ro.marketplace.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    List<Customer> findAll();

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteById(UUID id);
}
