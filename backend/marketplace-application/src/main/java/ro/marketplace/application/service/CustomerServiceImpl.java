package ro.marketplace.application.service;

import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.model.Customer;
import ro.marketplace.domain.port.CustomerRepository;
import ro.marketplace.domain.port.UnitOfWork;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public CustomerServiceImpl(CustomerRepository customerRepository, UnitOfWork unitOfWork, Clock clock) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "customerRepository must not be null");
        this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public Customer createCustomer(Customer customer) {
        Objects.requireNonNull(customer, "customer must not be null");
        return unitOfWork.executeInTransaction(() -> {
            if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
                throw new ConflictException("Customer already exists with email: " + customer.getEmail());
            }
            if (customer.getId() == null) {
                customer.setId(UUID.randomUUID());
            }
            if (customer.getCreatedAt() == null) {
                customer.setCreatedAt(LocalDateTime.now(clock));
            }
            return customerRepository.save(customer);
        });
    }

    @Override
    public Customer getCustomerById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer updateCustomer(UUID id, Customer updatedData) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(updatedData, "updatedData must not be null");

        return unitOfWork.executeInTransaction(() -> {
            Customer existing = getCustomerById(id);

            if (updatedData.getEmail() != null
                    && !updatedData.getEmail().equalsIgnoreCase(existing.getEmail())
                    && customerRepository.existsByEmail(updatedData.getEmail())) {
                throw new ConflictException("Email already in use: " + updatedData.getEmail());
            }

            existing.setName(updatedData.getName());
            existing.setEmail(updatedData.getEmail());
            existing.setPhone(updatedData.getPhone());

            return customerRepository.save(existing);
        });
    }

    @Override
    public void deleteCustomer(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        unitOfWork.executeInTransaction(() -> {
            if (customerRepository.findById(id).isEmpty()) {
                throw new ResourceNotFoundException("Customer not found with id: " + id);
            }
            customerRepository.deleteById(id);
            return null;
        });
    }
}
