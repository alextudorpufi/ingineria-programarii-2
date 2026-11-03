package ro.marketplace.application.service;

import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;

import java.util.List;
import java.util.UUID;

public interface LeadService {

    Lead createLead(Lead lead);

    Lead getLeadById(UUID id);

    List<Lead> getAllLeads();

    List<Lead> getLeadsByCategory(String categoryId);

    List<Lead> getLeadsByStatus(LeadStatus status);

    LeadPage getLeadsPage(String categoryId, LeadStatus status, int page, int size);

    LeadClaim claimLead(UUID leadId, UUID proId, String proName);
}
