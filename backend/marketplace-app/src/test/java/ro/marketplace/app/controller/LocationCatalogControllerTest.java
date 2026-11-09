package ro.marketplace.app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ro.marketplace.app.security.SecurityConfig;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationCatalogController.class)
@Import(SecurityConfig.class)
class LocationCatalogControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void publicCatalogIncludesAllCountiesAndBucharestSeparatelyFromIlfov() throws Exception {
        mvc.perform(get("/api/locations/counties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(42))
                .andExpect(jsonPath("$[0].name").value("Alba"))
                .andExpect(jsonPath("$[*].id", hasItem("RO-B")))
                .andExpect(jsonPath("$[*].id", hasItem("RO-IF")))
                .andExpect(jsonPath("$[?(@.id == 'RO-IS')].name", hasItem("Iași")));
    }
}
