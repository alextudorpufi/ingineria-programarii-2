package ro.marketplace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String email;
    private String role; // 'client', 'pro', 'admin'
    private String name;
    private String companyName;
    private String title;
    private String phone;
    private String avatarUrl;
    private String bio;
    private boolean verifiedBadge;
    private BigDecimal rating;
    private int reviewCount;
    private int creditsBalance;
    private BigDecimal hourlyRate;
    private BigDecimal clientBudget;
    private String responseTime;
    private int completedJobs;
    private BigDecimal trustScore;
    private String memberSince;

    @Builder.Default
    private List<String> coverageAreas = new ArrayList<>();

    @Builder.Default
    private List<String> servicesOffered = new ArrayList<>();

    @Builder.Default
    private List<String> categories = new ArrayList<>();
}
