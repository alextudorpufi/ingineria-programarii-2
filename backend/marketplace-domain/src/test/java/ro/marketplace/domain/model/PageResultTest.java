package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageResultTest {

    @Test
    @DisplayName("computes total pages and whether another page exists")
    void computesNavigationMetadata() {
        PageResult<String> first = new PageResult<>(List.of("a", "b"), 5, 0, 2);

        assertThat(first.totalPages()).isEqualTo(3);
        assertThat(first.hasNext()).isTrue();
    }

    @Test
    @DisplayName("reports no next page on the last and on an empty result")
    void reportsNoNextPageAtTheEnd() {
        assertThat(new PageResult<>(List.of("e"), 5, 2, 2).hasNext()).isFalse();
        assertThat(new PageResult<>(List.of(), 0, 0, 20).totalPages()).isZero();
        assertThat(new PageResult<>(List.of(), 0, 0, 20).hasNext()).isFalse();
    }

    @Test
    @DisplayName("defensively copies content and tolerates null")
    void copiesContent() {
        List<String> source = new java.util.ArrayList<>(List.of("a"));
        PageResult<String> page = new PageResult<>(source, 1, 0, 10);
        source.add("b");

        assertThat(page.content()).containsExactly("a");
        assertThat(new PageResult<String>(null, 0, 0, 10).content()).isEmpty();
    }

    @Test
    @DisplayName("rejects impossible paging arguments")
    void rejectsInvalidArguments() {
        assertThatThrownBy(() -> new PageResult<>(List.of(), 0, -1, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PageResult<>(List.of(), 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PageResult<>(List.of(), -1, 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
