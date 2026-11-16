package ro.marketplace.application.service;

import ro.marketplace.domain.model.Customer;
import ro.marketplace.domain.model.PageResult;

import java.util.UUID;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    Customer getCustomerById(UUID id);

    PageResult<Customer> getCustomers(int page, int size);

    Customer updateCustomer(UUID id, Customer customer);

    void deleteCustomer(UUID id);
}
