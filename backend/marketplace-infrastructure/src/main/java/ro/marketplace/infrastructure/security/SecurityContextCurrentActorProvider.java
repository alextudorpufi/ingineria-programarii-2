package ro.marketplace.infrastructure.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ro.marketplace.domain.port.CurrentActorProvider;

import java.util.Optional;

@Component
public class SecurityContextCurrentActorProvider implements CurrentActorProvider {

    @Override
    public Optional<String> currentActor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .filter(authentication -> !(authentication instanceof AnonymousAuthenticationToken))
                .map(Authentication::getName);
    }
}
