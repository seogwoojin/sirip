package uos.software.sirip.coupon.api;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uos.software.sirip.config.security.CurrentUser;
import uos.software.sirip.coupon.api.response.CouponApplicationResponse;
import uos.software.sirip.coupon.api.response.CouponResponse;
import uos.software.sirip.coupon.application.CouponApplicationResult;
import uos.software.sirip.coupon.application.CouponApplicationService;
import uos.software.sirip.coupon.application.CouponSummary;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CouponController {

    private final CouponApplicationService couponApplicationService;

    /**
     * ✅ 로그인한 사용자(@CurrentUser) 기준으로 쿠폰 신청
     */
    @PostMapping("/events/{eventId}/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponApplicationResponse apply(
        @CurrentUser Long accountId,
        @PathVariable Long eventId) {
        CouponApplicationResult result = couponApplicationService.applyV2(accountId, eventId);
        return CouponApplicationResponse.from(result);
    }

    /**
     * ✅ 쿠폰 사용 (USER 자신이 소유한 쿠폰만 가능하도록 service 내부에서 검증)
     */
    @PostMapping("/coupons/{couponId}/redeem")
    public CouponResponse redeem(
        @CurrentUser Long accountId,
        @PathVariable Long couponId) {
        CouponSummary summary = couponApplicationService.redeem(couponId);
        return CouponResponse.from(summary);
    }

    /**
     * ✅ 노쇼 처리 (USER 본인 쿠폰만 가능하도록 검증)
     */
    @PostMapping("/coupons/{couponId}/no-show")
    public CouponResponse markNoShow(
        @CurrentUser Long accountId,
        @PathVariable Long couponId) {
        CouponSummary summary = couponApplicationService.markNoShow(accountId, couponId);
        return CouponResponse.from(summary);
    }

    /**
     * ✅ 로그인한 사용자의 쿠폰 목록 조회
     */
    @GetMapping("/users/me/coupons")
    public List<CouponResponse> listCoupons(@CurrentUser Long accountId) {
        return couponApplicationService.listUserCoupons(accountId)
            .stream()
            .map(CouponResponse::from)
            .collect(Collectors.toList());
    }
}
