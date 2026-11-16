package ro.marketplace.application.service;

import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;

import java.util.UUID;

public interface LeadService {

    Lead createLead(Lead lead);

    Lead getLeadById(UUID id);

    LeadPage getLeadsPage(String categoryId, LeadStatus status, int page, int size);

    LeadClaim claimLead(UUID leadId, UUID proId, String proName);

    Lead closeLead(UUID leadId, String actor);
}
