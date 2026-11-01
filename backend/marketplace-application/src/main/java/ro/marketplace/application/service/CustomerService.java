package ro.marketplace.application.service;

import ro.marketplace.domain.model.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    Customer getCustomerById(UUID id);

    List<Customer> getAllCustomers();

    Customer updateCustomer(UUID id, Customer customer);

    void deleteCustomer(UUID id);
}
