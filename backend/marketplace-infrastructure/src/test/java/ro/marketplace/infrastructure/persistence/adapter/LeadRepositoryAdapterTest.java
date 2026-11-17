package ro.marketplace.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;
import ro.marketplace.domain.port.CurrentActorProvider;
import ro.marketplace.infrastructure.persistence.entity.LeadClaimEntity;
import ro.marketplace.infrastructure.persistence.entity.LeadEntity;
import ro.marketplace.infrastructure.persistence.repository.SpringDataLeadClaimRepository;
import ro.marketplace.infrastructure.persistence.repository.SpringDataLeadRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadRepositoryAdapterTest {

    @Mock
    private SpringDataLeadRepository leadRepository;

    @Mock
    private SpringDataLeadClaimRepository claimRepository;

    @Mock
    private CurrentActorProvider currentActorProvider;

    private LeadRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LeadRepositoryAdapter(leadRepository, claimRepository, currentActorProvider);
    }

    @Test
    @DisplayName("create inserts through the insert-only entity contract")
    void createPersists() {
        Lead lead = Lead.builder()
                .id(UUID.randomUUID())
                .title("Website")
                .status(LeadStatus.VALIDAT)
                .build();
        when(leadRepository.save(any(LeadEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Lead created = adapter.create(lead);

        verify(leadRepository).save(any(LeadEntity.class));
        assertThat(created.getId()).isEqualTo(lead.getId());
    }

    @Test
    @DisplayName("claims are appended, never updated")
    void createClaimPersists() {
        LeadClaim claim = LeadClaim.builder()
                .id(UUID.randomUUID())
                .leadId(UUID.randomUUID())
                .proId(UUID.randomUUID())
                .proName("Pro")
                .build();
        when(claimRepository.save(any(LeadClaimEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        adapter.createClaim(claim);

        verify(claimRepository).save(any(LeadClaimEntity.class));
    }

    @Test
    @DisplayName("slot reservation uses the domain lifecycle values and records the actor")
    void reserveSlotUsesDomainValues() {
        UUID leadId = UUID.randomUUID();
        when(currentActorProvider.currentActor()).thenReturn(Optional.of("pro@example.com"));
        when(leadRepository.reserveSlot(leadId, "validat", "in_asteptare", "pro@example.com"))
                .thenReturn(1);

        assertThat(adapter.reserveSlot(leadId)).isTrue();

        verify(leadRepository).reserveSlot(leadId, "validat", "in_asteptare", "pro@example.com");
    }

    @Test
    @DisplayName("assignment only transitions claimable leads and records the actor")
    void markAssignedUsesLifecycleGuard() {
        UUID leadId = UUID.randomUUID();
        when(currentActorProvider.currentActor()).thenReturn(Optional.empty());

        adapter.markAssignedIfFull(leadId);

        verify(leadRepository).markAssignedIfFull(
                leadId, "atribuit", "validat", "in_asteptare", "system");
    }
}
