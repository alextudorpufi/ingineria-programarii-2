package ro.marketplace.application.service;

import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.domain.model.User;

import java.util.UUID;

public interface ProfessionalProfileService {
    User get(UUID userId);
    User update(UUID userId, Update update);

    record Update(String name, String companyName, String title, String phone,
                  String bio, ProfessionalProfile profile) { }
}
