package uos.software.sirip.event.api.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uos.software.sirip.config.security.CurrentUser;
import uos.software.sirip.event.api.request.*;
import uos.software.sirip.event.api.response.EventResponse;
import uos.software.sirip.event.application.EventCommandService;
import uos.software.sirip.event.application.EventSummary;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/events")
public class EventAdminController {

    private final EventCommandService eventCommandService;

    /**
     * ✅ 관리자(현재 로그인 사용자) 기반 이벤트 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@CurrentUser Long accountId,
        @RequestBody @Valid CreateEventRequest request) {
        EventSummary summary = eventCommandService.create(
            accountId,
            request.getTitle(),
            request.getDescription(),
            request.getRewardDescription(),
            request.getTotalCoupons(),
            request.getStartAt(),
            request.getEndAt()
        );
        return EventResponse.from(summary);
    }

    /**
     * ✅ 보상 수정
     */
    @PatchMapping("/{eventId}/reward")
    public EventResponse updateReward(@CurrentUser Long accountId,
        @PathVariable Long eventId,
        @RequestBody @Valid UpdateEventRewardRequest request) {
        EventSummary summary = eventCommandService.updateReward(accountId, eventId,
            request.getRewardDescription());
        return EventResponse.from(summary);
    }

//    /** ✅ 발급량 수정 */
//    @PatchMapping("/{eventId}/capacity")
//    public EventResponse updateCapacity(@CurrentUser Long accountId,
//                                        @PathVariable Long eventId,
//                                        @RequestBody @Valid UpdateEventCapacityRequest request) {
//        EventSummary summary = eventCommandService.updateCapacity(accountId, eventId, request.getTotalCoupons());
//        return EventResponse.from(summary);
//    }

    /**
     * ✅ 일정 수정
     */
    @PatchMapping("/{eventId}/schedule")
    public EventResponse updateSchedule(@CurrentUser Long accountId,
        @PathVariable Long eventId,
        @RequestBody @Valid UpdateEventScheduleRequest request) {
        EventSummary summary = eventCommandService.updateSchedule(
            accountId, eventId, request.getStartAt(), request.getEndAt());
        return EventResponse.from(summary);
    }

    /**
     * ✅ 관리자 소유 이벤트 조회
     */
    @GetMapping("/{eventId}")
    public EventResponse get(@CurrentUser Long accountId,
        @PathVariable Long eventId) {
        EventSummary summary = eventCommandService.get(accountId, eventId);
        return EventResponse.from(summary);
    }
}
