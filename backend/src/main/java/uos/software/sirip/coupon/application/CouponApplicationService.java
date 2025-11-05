package uos.software.sirip.coupon.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uos.software.sirip.coupon.domain.Coupon;
import uos.software.sirip.coupon.domain.CouponRepository;
import uos.software.sirip.coupon.domain.CouponStatus;
import uos.software.sirip.coupon.domain.Penalty;
import uos.software.sirip.coupon.domain.PenaltyRepository;
import uos.software.sirip.coupon.exception.ActivePenaltyException;
import uos.software.sirip.coupon.exception.CouponNotFoundException;
import uos.software.sirip.coupon.exception.DuplicateApplicationException;
import uos.software.sirip.coupon.exception.EventClosedException;
import uos.software.sirip.coupon.exception.InvalidCouponStateException;
import uos.software.sirip.event.domain.Event;
import uos.software.sirip.event.domain.EventRepository;
import uos.software.sirip.event.exception.EventNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponApplicationService {

    private static final int PENALTY_MONTHS = 6;

    private final CouponRepository couponRepository;
    private final PenaltyRepository penaltyRepository;
    private final EventRepository eventRepository;
    private final Clock clock;

    public CouponApplicationResult apply(Long accountId, Long eventId) {
        LocalDateTime now = LocalDateTime.now(clock);
        penaltyRepository.findActiveByAccountId(accountId, now).ifPresent(penalty -> {
            throw new ActivePenaltyException(accountId);
        });

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.isActive(now)) {
            throw new EventClosedException(eventId);
        }

        Coupon existingCoupon = couponRepository.findByEventIdAndAccountId(eventId, accountId)
            .orElse(null);
        if (existingCoupon != null) {
            if (existingCoupon.getStatus() == CouponStatus.WAITING) {
                return CouponApplicationResult.queued(CouponSummary.from(existingCoupon));
            }
            if (!existingCoupon.getStatus().isTerminal()) {
                throw new DuplicateApplicationException(accountId, eventId);
            }
        }

        if (event.getRemainingCoupons() > 0) {
            Coupon issued = Coupon.issued(null, eventId, accountId, now, now);
            Coupon saved = couponRepository.save(issued);
            eventRepository.save(event.decrementRemaining());
            return CouponApplicationResult.issued(CouponSummary.from(saved));
        }

        int queuePosition = couponRepository.countWaitingByEventId(eventId) + 1;
        Coupon waiting = Coupon.waiting(null, eventId, accountId, now, queuePosition);
        Coupon saved = couponRepository.save(waiting);
        return CouponApplicationResult.queued(CouponSummary.from(saved));
    }

    public CouponSummary redeem(Long couponId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CouponNotFoundException(couponId));
        if (!coupon.getStatus().isIssued()) {
            throw new InvalidCouponStateException("Coupon must be issued to redeem");
        }
        Coupon redeemed = coupon.redeem(now);
        Coupon saved = couponRepository.save(redeemed);
        return CouponSummary.from(saved);
    }

    public CouponSummary markNoShow(Long couponId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CouponNotFoundException(couponId));
        if (!coupon.getStatus().isIssued()) {
            throw new InvalidCouponStateException("Only issued coupons can be marked as no-show");
        }
        Coupon updated = coupon.markNoShow(now);
        Coupon saved = couponRepository.save(updated);
        Event event = eventRepository.findById(saved.getEventId())
            .orElseThrow(() -> new EventNotFoundException(saved.getEventId()));
        Event incremented = event.incrementRemaining();
        eventRepository.save(incremented);
        penaltyRepository.save(
            Penalty.create(saved.getAccountId(), now, now.plusMonths(PENALTY_MONTHS)));
        promoteNextWaiting(incremented.getId());
        return CouponSummary.from(saved);
    }

    public List<CouponSummary> listUserCoupons(Long accountId) {
        return couponRepository.findByAccountId(accountId)
            .stream()
            .map(CouponSummary::from)
            .collect(Collectors.toList());
    }

    public void fillWaitlist(Long eventId) {
        promoteNextWaiting(eventId);
    }

    private void promoteNextWaiting(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException(eventId));
        List<Coupon> waitlist = couponRepository.findWaitingByEventIdOrderByQueuePosition(eventId);
        if (waitlist.isEmpty() || event.getRemainingCoupons() <= 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        int promotedCount = 0;
        while (event.getRemainingCoupons() > 0 && promotedCount < waitlist.size()) {
            Coupon next = waitlist.get(promotedCount);
            Coupon promoted = next.promoteFromWaitlist(now);
            couponRepository.save(promoted);
            event = eventRepository.save(event.decrementRemaining());
            promotedCount++;
        }

        for (int index = promotedCount; index < waitlist.size(); index++) {
            Coupon candidate = waitlist.get(index);
            Coupon updated = candidate.withQueuePosition(index - promotedCount + 1);
            couponRepository.save(updated);
        }
    }
}
