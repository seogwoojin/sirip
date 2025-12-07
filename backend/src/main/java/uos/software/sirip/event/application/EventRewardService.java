package uos.software.sirip.event.application;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uos.software.sirip.event.infra.external.OptimizeRequest;
import uos.software.sirip.event.infra.external.RewardOptimizeClient;
import uos.software.sirip.event.infra.jpa.Event;
import uos.software.sirip.event.infra.jpa.EventJpaRepository;

@Service
@RequiredArgsConstructor
public class EventRewardService {

    private final RewardOptimizeClient rewardOptimizeClient;
    private final EventJpaRepository eventRepository;

    @Transactional
    public EventSummary optimizeAndApplyReward(Long eventId, int targetParticipants) {

        Event event = eventRepository.getReferenceById(eventId);

        OptimizeRequest req = new OptimizeRequest();

        req.setTitle(event.getTitle());
        req.setEventType(event.getEventType());
        req.setOrganizerType(event.getOrganizerType());
        req.setTargetMajor(event.getTargetMajor());
        req.setTargetGrade(event.getTargetGrade());
        req.setWeekday(event.getWeekday());
        req.setBrandScore(event.getBrandScore());
        req.setDateGap(event.getDateGap(LocalDateTime.now()));
        req.setTargetParticipants(targetParticipants);

        // AI 모델에서 추천 reward 받기
        double recommendedReward = rewardOptimizeClient.optimizeReward(req);

        // 이벤트에 바로 적용
        event.changeRewardDescription(String.valueOf((int) recommendedReward));

        return toSummary(event, LocalDateTime.now());
    }

    private EventSummary toSummary(Event event, LocalDateTime now) {
        return new EventSummary(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getRewardDescription(),
                event.getTotalCoupons(),
                event.getRemainingCoupons(),
                event.getStartAt(),
                event.getEndAt(),
                event.isActive(now)
        );
    }
}
