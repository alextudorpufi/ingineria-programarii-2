package ro.marketplace.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestsTest {

    private static final Sort SORT =
            Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    @Test
    @DisplayName("page size is capped so a caller cannot request an unbounded page")
    void capsPageSize() {
        PageRequest request = PageRequests.of(0, 5_000, SORT);

        assertThat(request.getPageSize()).isEqualTo(PageRequests.MAX_PAGE_SIZE);
    }

    @Test
    @DisplayName("negative page and non-positive size are normalised")
    void normalisesLowBounds() {
        PageRequest request = PageRequests.of(-3, 0, SORT);

        assertThat(request.getPageNumber()).isZero();
        assertThat(request.getPageSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("the supplied sort is preserved")
    void keepsSort() {
        PageRequest request = PageRequests.of(2, 20, SORT);

        assertThat(request.getSort()).isEqualTo(SORT);
        assertThat(request.getPageNumber()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("limits fall back and are capped independently of page size")
    void capsLimits() {
        assertThat(PageRequests.limit(0, 50)).isEqualTo(50);
        assertThat(PageRequests.limit(-1, 50)).isEqualTo(50);
        assertThat(PageRequests.limit(10, 50)).isEqualTo(10);
        assertThat(PageRequests.limit(10_000, 50)).isEqualTo(PageRequests.MAX_PAGE_SIZE);
    }
}
