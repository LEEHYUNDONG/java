package com.example.java.algorithm.interview.implementation;

import com.example.java.algorithm.interview.implementation.CouponEngine.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Coupon Engine 테스트")
class CouponEngineTest {

    private CouponEngine engine;
    private LocalDateTime currentTime;

    @BeforeEach
    void setUp() {
        engine = new CouponEngine();
        currentTime = LocalDateTime.of(2024, 1, 15, 12, 0);
    }

    @Test
    @DisplayName("쿠폰 없이 주문")
    void testOrderWithoutCoupon() {
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(order, Collections.emptyList(), currentTime);

        assertThat(result.getOriginalAmount()).isEqualTo(50000);
        assertThat(result.getDiscountAmount()).isEqualTo(0);
        assertThat(result.getFinalProductAmount()).isEqualTo(50000);
        assertThat(result.getShippingFee()).isEqualTo(3000);
        assertThat(result.getTotalAmount()).isEqualTo(53000);
    }

    @Test
    @DisplayName("정률 할인 쿠폰 적용")
    void testPercentageCoupon() {
        Coupon coupon = new Coupon(
            "COUPON-10P", CouponType.PERCENTAGE, 10, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(order, List.of(coupon), currentTime);

        assertThat(result.getDiscountAmount()).isEqualTo(5000);  // 10%
        assertThat(result.getFinalProductAmount()).isEqualTo(45000);
        assertThat(result.getTotalAmount()).isEqualTo(48000);  // 45000 + 3000
    }

    @Test
    @DisplayName("정률 할인 최대 금액 제한")
    void testPercentageCouponWithMaxDiscount() {
        Coupon coupon = new Coupon(
            "COUPON-10P", CouponType.PERCENTAGE, 10, 3000, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(order, List.of(coupon), currentTime);

        assertThat(result.getDiscountAmount()).isEqualTo(3000);  // 최대 3000원
        assertThat(result.getFinalProductAmount()).isEqualTo(47000);
    }

    @Test
    @DisplayName("정액 할인 쿠폰 적용")
    void testFixedAmountCoupon() {
        Coupon coupon = new Coupon(
            "COUPON-5000", CouponType.FIXED_AMOUNT, 5000, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(order, List.of(coupon), currentTime);

        assertThat(result.getDiscountAmount()).isEqualTo(5000);
        assertThat(result.getFinalProductAmount()).isEqualTo(45000);
        assertThat(result.getTotalAmount()).isEqualTo(48000);
    }

    @Test
    @DisplayName("무료 배송 쿠폰 적용")
    void testFreeShippingCoupon() {
        Coupon coupon = new Coupon(
            "COUPON-FREE", CouponType.FREE_SHIPPING, 0, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(order, List.of(coupon), currentTime);

        assertThat(result.getDiscountAmount()).isEqualTo(0);  // 상품 할인 없음
        assertThat(result.getShippingFee()).isEqualTo(0);  // 배송비 무료
        assertThat(result.getTotalAmount()).isEqualTo(50000);
        assertThat(result.getCouponDiscounts().get("COUPON-FREE")).isEqualTo(3000);
    }

    @Test
    @DisplayName("여러 쿠폰 중복 적용")
    void testMultipleCoupons() {
        Coupon percentage = new Coupon(
            "COUPON-10P", CouponType.PERCENTAGE, 10, 5000, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Coupon fixedAmount = new Coupon(
            "COUPON-3000", CouponType.FIXED_AMOUNT, 3000, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Coupon freeShipping = new Coupon(
            "COUPON-FREE", CouponType.FREE_SHIPPING, 0, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(
            order, List.of(percentage, fixedAmount, freeShipping), currentTime
        );

        // 50000 * 0.9 = 45000 (정률 5000원 할인)
        // 45000 - 3000 = 42000 (정액 3000원 할인)
        // 배송비 무료
        assertThat(result.getDiscountAmount()).isEqualTo(8000);  // 5000 + 3000
        assertThat(result.getFinalProductAmount()).isEqualTo(42000);
        assertThat(result.getShippingFee()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualTo(42000);
    }

    @Test
    @DisplayName("최소 주문 금액 미달로 쿠폰 미적용")
    void testMinOrderAmountNotMet() {
        Coupon coupon = new Coupon(
            "COUPON-10P", CouponType.PERCENTAGE, 10, null, 100000,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(order, List.of(coupon), currentTime);

        assertThat(result.getDiscountAmount()).isEqualTo(0);
        assertThat(result.getAppliedCoupons()).isEmpty();
    }

    @Test
    @DisplayName("쿠폰 유효기간 확인 - 기간 전")
    void testCouponNotYetValid() {
        Coupon coupon = new Coupon(
            "COUPON-10P", CouponType.PERCENTAGE, 10, null, 0,
            currentTime.plusDays(1), currentTime.plusDays(7), true, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(order, List.of(coupon), currentTime);

        assertThat(result.getDiscountAmount()).isEqualTo(0);
        assertThat(result.getAppliedCoupons()).isEmpty();
    }

    @Test
    @DisplayName("쿠폰 유효기간 확인 - 기간 만료")
    void testCouponExpired() {
        Coupon coupon = new Coupon(
            "COUPON-10P", CouponType.PERCENTAGE, 10, null, 0,
            currentTime.minusDays(7), currentTime.minusDays(1), true, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(order, List.of(coupon), currentTime);

        assertThat(result.getDiscountAmount()).isEqualTo(0);
        assertThat(result.getAppliedCoupons()).isEmpty();
    }

    @Test
    @DisplayName("카테고리별 쿠폰 적용")
    void testCategoryCoupon() {
        Coupon fashionCoupon = new Coupon(
            "FASHION-10P", CouponType.PERCENTAGE, 10, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, Set.of("FASHION")
        );
        Order fashionOrder = new Order(50000, 3000, Set.of("FASHION"));
        Order electronicsOrder = new Order(50000, 3000, Set.of("ELECTRONICS"));

        DiscountResult fashionResult = engine.calculateDiscountSolution(fashionOrder, List.of(fashionCoupon), currentTime);
        DiscountResult electronicsResult = engine.calculateDiscountSolution(electronicsOrder, List.of(fashionCoupon), currentTime);

        assertThat(fashionResult.getDiscountAmount()).isEqualTo(5000);
        assertThat(electronicsResult.getDiscountAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("중복 사용 불가 쿠폰")
    void testNonStackableCoupon() {
        Coupon stackable = new Coupon(
            "STACK-1", CouponType.PERCENTAGE, 10, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Coupon nonStackable = new Coupon(
            "NON-STACK-1", CouponType.FIXED_AMOUNT, 5000, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), false, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(
            order, List.of(stackable, nonStackable), currentTime
        );

        // stackable 쿠폰과 함께 적용 가능
        assertThat(result.getAppliedCoupons()).hasSize(2);
        assertThat(result.getAppliedCoupons()).contains("STACK-1", "NON-STACK-1");
    }

    @Test
    @DisplayName("정액 할인이 상품 금액보다 큰 경우")
    void testFixedAmountExceedsProductAmount() {
        Coupon coupon = new Coupon(
            "COUPON-100K", CouponType.FIXED_AMOUNT, 100000, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        DiscountResult result = engine.calculateDiscountSolution(order, List.of(coupon), currentTime);

        // 상품 금액을 초과할 수 없음
        assertThat(result.getFinalProductAmount()).isEqualTo(0);
        assertThat(result.getDiscountAmount()).isEqualTo(50000);
    }

    @Test
    @DisplayName("쿠폰 적용 순서 확인 (PERCENTAGE -> FIXED -> FREE_SHIPPING)")
    void testCouponApplicationOrder() {
        Coupon freeShipping = new Coupon(
            "FREE", CouponType.FREE_SHIPPING, 0, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Coupon fixedAmount = new Coupon(
            "FIXED", CouponType.FIXED_AMOUNT, 5000, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Coupon percentage = new Coupon(
            "PERCENT", CouponType.PERCENTAGE, 10, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Order order = new Order(50000, 3000, Set.of("FASHION"));

        // 순서와 무관하게 PERCENTAGE -> FIXED -> FREE_SHIPPING 순서로 적용
        DiscountResult result = engine.calculateDiscountSolution(
            order, List.of(freeShipping, fixedAmount, percentage), currentTime
        );

        // 50000 * 0.9 = 45000 (PERCENTAGE)
        // 45000 - 5000 = 40000 (FIXED)
        // 배송비 무료 (FREE_SHIPPING)
        assertThat(result.getFinalProductAmount()).isEqualTo(40000);
        assertThat(result.getShippingFee()).isEqualTo(0);
    }

    @Test
    @DisplayName("복잡한 시나리오 - 여러 조건 통합")
    void testComplexScenario() {
        Coupon percentage = new Coupon(
            "COUPON-20P", CouponType.PERCENTAGE, 20, 10000, 30000,
            currentTime.minusDays(1), currentTime.plusDays(1), true, Set.of("FASHION")
        );
        Coupon fixedAmount = new Coupon(
            "COUPON-3000", CouponType.FIXED_AMOUNT, 3000, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );
        Coupon freeShipping = new Coupon(
            "FREE-SHIP", CouponType.FREE_SHIPPING, 0, null, 0,
            currentTime.minusDays(1), currentTime.plusDays(1), true, null
        );

        Order order = new Order(100000, 3000, Set.of("FASHION", "ELECTRONICS"));

        DiscountResult result = engine.calculateDiscountSolution(
            order, List.of(percentage, fixedAmount, freeShipping), currentTime
        );

        // 100000 * 0.8 = 80000 (20% 할인 = 20000원이지만 최대 10000원)
        // 90000 - 3000 = 87000
        // 배송비 무료
        assertThat(result.getDiscountAmount()).isEqualTo(13000);  // 10000 + 3000
        assertThat(result.getFinalProductAmount()).isEqualTo(87000);
        assertThat(result.getShippingFee()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualTo(87000);
        assertThat(result.getAppliedCoupons()).containsExactlyInAnyOrder("COUPON-20P", "COUPON-3000", "FREE-SHIP");
    }
}
