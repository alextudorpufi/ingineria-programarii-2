package ro.marketplace.app.mapper;

import ro.marketplace.app.dto.CreateCustomerRequest;
import ro.marketplace.app.dto.CustomerResponse;
import ro.marketplace.app.dto.UpdateCustomerRequest;
import ro.marketplace.domain.model.Customer;

public final class CustomerDtoMapper {

    private CustomerDtoMapper() {
        // Static utility class
    }

    public static Customer toDomain(CreateCustomerRequest request) {
        if (request == null) {
            return null;
        }
        return Customer.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .build();
    }

    public static Customer toDomain(UpdateCustomerRequest request) {
        if (request == null) {
            return null;
        }
        return Customer.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .build();
    }

    public static CustomerResponse toResponse(Customer domain) {
        if (domain == null) {
            return null;
        }
        return new CustomerResponse(
                domain.getId(),
                domain.getName(),
                domain.getEmail(),
                domain.getPhone(),
                domain.getCreatedAt()
        );
    }
}
