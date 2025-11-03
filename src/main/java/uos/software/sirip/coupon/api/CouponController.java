package uos.software.sirip.coupon.api;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uos.software.sirip.coupon.api.response.CouponApplicationResponse;
import uos.software.sirip.coupon.api.response.CouponResponse;
import uos.software.sirip.coupon.application.CouponApplicationResult;
import uos.software.sirip.coupon.application.CouponApplicationService;
import uos.software.sirip.coupon.application.CouponSummary;

@RestController
@RequestMapping("/api")
public class CouponController {

    private final CouponApplicationService couponApplicationService;

    public CouponController(CouponApplicationService couponApplicationService) {
        this.couponApplicationService = couponApplicationService;
    }

    @PostMapping("/events/{eventId}/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponApplicationResponse apply(@PathVariable Long eventId, @RequestParam Long accountId) {
        CouponApplicationResult result = couponApplicationService.apply(accountId, eventId);
        return CouponApplicationResponse.from(result);
    }

    @PostMapping("/coupons/{couponId}/redeem")
    public CouponResponse redeem(@PathVariable Long couponId) {
        CouponSummary summary = couponApplicationService.redeem(couponId);
        return CouponResponse.from(summary);
    }

    @PostMapping("/coupons/{couponId}/no-show")
    public CouponResponse markNoShow(@PathVariable Long couponId) {
        CouponSummary summary = couponApplicationService.markNoShow(couponId);
        return CouponResponse.from(summary);
    }

    @GetMapping("/users/{accountId}/coupons")
    public List<CouponResponse> listCoupons(@PathVariable Long accountId) {
        return couponApplicationService.listUserCoupons(accountId)
            .stream()
            .map(CouponResponse::from)
            .collect(Collectors.toList());
    }
}
