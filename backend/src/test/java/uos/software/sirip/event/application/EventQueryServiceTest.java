package uos.software.sirip.event.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uos.software.sirip.event.domain.Event;
import uos.software.sirip.event.domain.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventQueryServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private EventQueryService eventQueryService;

    @Test
    @DisplayName("행사 목록을 시작 시간 순으로 반환한다")
    void listEvents() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 11, 1, 12, 0);
        given(clock.instant()).willReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        Event first = new Event(
            2L,
            "가을 축제",
            "동아리 공연과 부스 운영",
            "푸드트럭 쿠폰",
            200,
            150,
            fixedNow.minusDays(1),
            fixedNow.plusDays(1)
        );
        Event second = new Event(
            1L,
            "AI 세미나",
            "신기술 특강",
            "스타벅스 기프트카드",
            100,
            90,
            fixedNow.plusDays(1),
            fixedNow.plusDays(2)
        );

        given(eventRepository.findAll()).willReturn(List.of(second, first));

        List<EventSummary> summaries = eventQueryService.listEvents();

        assertThat(summaries)
            .extracting(EventSummary::id)
            .containsExactly(2L, 1L);
        assertThat(summaries.get(0).active()).isTrue();
        assertThat(summaries.get(1).active()).isFalse();
    }
}
