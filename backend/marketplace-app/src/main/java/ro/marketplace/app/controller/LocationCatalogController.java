package ro.marketplace.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.marketplace.domain.model.RomanianCounty;

import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/locations")
public class LocationCatalogController {
    public record CountyOption(String id, String name) {}

    @GetMapping("/counties")
    public List<CountyOption> counties() {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("ro-RO"));
        return Arrays.stream(RomanianCounty.values())
                .map(county -> new CountyOption(county.id(), county.displayName()))
                .sorted((left, right) -> collator.compare(left.name(), right.name()))
                .toList();
    }
}
