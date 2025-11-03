package uos.software.sirip.event.api.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uos.software.sirip.event.api.request.CreateEventRequest;
import uos.software.sirip.event.api.request.UpdateEventCapacityRequest;
import uos.software.sirip.event.api.request.UpdateEventRewardRequest;
import uos.software.sirip.event.api.request.UpdateEventScheduleRequest;
import uos.software.sirip.event.api.response.EventResponse;
import uos.software.sirip.event.application.EventCommandService;
import uos.software.sirip.event.application.EventSummary;

@RestController
@RequestMapping("/api/admin/events")
public class EventAdminController {

    private final EventCommandService eventCommandService;

    public EventAdminController(EventCommandService eventCommandService) {
        this.eventCommandService = eventCommandService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@RequestBody @Valid CreateEventRequest request) {
        EventSummary summary = eventCommandService.create(
            request.getTitle(),
            request.getDescription(),
            request.getRewardDescription(),
            request.getTotalCoupons(),
            request.getStartAt(),
            request.getEndAt()
        );
        return EventResponse.from(summary);
    }

    @PatchMapping("/{eventId}/reward")
    public EventResponse updateReward(@PathVariable Long eventId, @RequestBody @Valid UpdateEventRewardRequest request) {
        EventSummary summary = eventCommandService.updateReward(eventId, request.getRewardDescription());
        return EventResponse.from(summary);
    }

    @PatchMapping("/{eventId}/capacity")
    public EventResponse updateCapacity(@PathVariable Long eventId, @RequestBody @Valid UpdateEventCapacityRequest request) {
        EventSummary summary = eventCommandService.updateCapacity(eventId, request.getTotalCoupons());
        return EventResponse.from(summary);
    }

    @PatchMapping("/{eventId}/schedule")
    public EventResponse updateSchedule(@PathVariable Long eventId, @RequestBody @Valid UpdateEventScheduleRequest request) {
        EventSummary summary = eventCommandService.updateSchedule(eventId, request.getStartAt(), request.getEndAt());
        return EventResponse.from(summary);
    }

    @GetMapping("/{eventId}")
    public EventResponse get(@PathVariable Long eventId) {
        EventSummary summary = eventCommandService.get(eventId);
        return EventResponse.from(summary);
    }
}
