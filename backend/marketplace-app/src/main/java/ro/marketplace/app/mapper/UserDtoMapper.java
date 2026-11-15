package ro.marketplace.app.mapper;

import ro.marketplace.app.dto.AuthDtos.UserProfileDto;
import ro.marketplace.domain.model.User;

import java.util.List;

/**
 * Single place that exposes a user profile through the API. Controllers must
 * use this mapper so newly added sensitive fields are never serialized by accident.
 */
public final class UserDtoMapper {

    private UserDtoMapper() {
    }

    public static UserProfileDto toProfile(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getName(),
                user.getCompanyName(),
                user.getTitle(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getBio(),
                user.isVerifiedBadge(),
                user.isProVerified(),
                user.getRating(),
                user.getReviewCount(),
                user.getCreditsBalance(),
                user.getHourlyRate(),
                user.getClientBudget(),
                user.getResponseTime(),
                user.getCompletedJobs(),
                user.getTrustScore(),
                user.getMemberSince(),
                safeList(user.getCoverageAreas()),
                safeList(user.getServicesOffered()),
                safeList(user.getCategories())
        );
    }

    private static List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }
}
