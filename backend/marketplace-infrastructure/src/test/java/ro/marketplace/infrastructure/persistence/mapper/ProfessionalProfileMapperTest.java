package ro.marketplace.infrastructure.persistence.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.marketplace.domain.model.UserRole;
import ro.marketplace.infrastructure.persistence.entity.UserEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalProfileMapperTest {

    @Test
    @DisplayName("only the domain professional role gets an implicit profile")
    void createsProfileForProfessionalRole() {
        UserEntity pro = UserEntity.builder()
                .id(UUID.randomUUID())
                .role(UserRole.PRO.getDbValue())
                .build();

        assertThat(ProfessionalProfileMapper.toEntity(null, pro)).isNotNull();
    }

    @Test
    @DisplayName("other roles do not get an implicit profile")
    void skipsProfileForOtherRoles() {
        UserEntity client = UserEntity.builder()
                .id(UUID.randomUUID())
                .role(UserRole.CLIENT.getDbValue())
                .build();

        assertThat(ProfessionalProfileMapper.toEntity(null, client)).isNull();
    }
}
