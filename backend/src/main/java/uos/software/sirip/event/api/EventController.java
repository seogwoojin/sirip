package uos.software.sirip.event.api;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uos.software.sirip.event.api.response.EventResponse;
import uos.software.sirip.event.application.EventQueryService;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventQueryService eventQueryService;

    @GetMapping
    public List<EventResponse> listEvents() {
        return eventQueryService.listEvents()
            .stream()
            .map(EventResponse::from)
            .collect(Collectors.toList());
    }
}
