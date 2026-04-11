package com.example.java.algorithm.interview.implementation;

import java.time.LocalDateTime;
import java.util.*;


public class CouponEngine {

    public enum CouponType {
        PERCENTAGE,      // 정률 할인
        FIXED_AMOUNT,    // 정액 할인
        FREE_SHIPPING    // 무료 배송
    }

    /**
     * 쿠폰 정보
     */
    public static class Coupon {
        private final String couponId;
        private final CouponType type;
        private final int discountValue;           // PERCENTAGE: 퍼센트(10 = 10%), FIXED: 금액
        private final Integer maxDiscountAmount;   // 최대 할인 금액 (null이면 제한 없음)
        private final int minOrderAmount;          // 최소 주문 금액
        private final LocalDateTime validFrom;
        private final LocalDateTime validUntil;
        private final boolean stackable;           // 다른 쿠폰과 중복 가능 여부
        private final Set<String> applicableCategories;  // 적용 가능 카테고리 (null이면 전체)

        public Coupon(String couponId, CouponType type, int discountValue,
                     Integer maxDiscountAmount, int minOrderAmount,
                     LocalDateTime validFrom, LocalDateTime validUntil,
                     boolean stackable, Set<String> applicableCategories) {
            this.couponId = couponId;
            this.type = type;
            this.discountValue = discountValue;
            this.maxDiscountAmount = maxDiscountAmount;
            this.minOrderAmount = minOrderAmount;
            this.validFrom = validFrom;
            this.validUntil = validUntil;
            this.stackable = stackable;
            this.applicableCategories = applicableCategories;
        }

        // Getters
        public String getCouponId() { return couponId; }
        public CouponType getType() { return type; }
        public int getDiscountValue() { return discountValue; }
        public Integer getMaxDiscountAmount() { return maxDiscountAmount; }
        public int getMinOrderAmount() { return minOrderAmount; }
        public LocalDateTime getValidFrom() { return validFrom; }
        public LocalDateTime getValidUntil() { return validUntil; }
        public boolean isStackable() { return stackable; }
        public Set<String> getApplicableCategories() { return applicableCategories; }
    }

    /**
     * 주문 정보
     */
    public static class Order {
        private final int productAmount;    // 상품 금액
        private final int shippingFee;      // 배송비
        private final Set<String> categories; // 주문에 포함된 카테고리

        public Order(int productAmount, int shippingFee, Set<String> categories) {
            this.productAmount = productAmount;
            this.shippingFee = shippingFee;
            this.categories = categories;
        }

        public int getProductAmount() { return productAmount; }
        public int getShippingFee() { return shippingFee; }
        public Set<String> getCategories() { return categories; }
    }

    /**
     * 할인 계산 결과
     */
    public static class DiscountResult {
        private final int originalAmount;       // 원래 상품 금액
        private final int discountAmount;       // 총 할인 금액
        private final int finalProductAmount;   // 할인 후 상품 금액
        private final int shippingFee;          // 최종 배송비
        private final int totalAmount;          // 최종 결제 금액
        private final List<String> appliedCoupons;  // 적용된 쿠폰 ID 목록
        private final Map<String, Integer> couponDiscounts; // 쿠폰별 할인 금액

        public DiscountResult(int originalAmount, int discountAmount, int finalProductAmount,
                            int shippingFee, int totalAmount,
                            List<String> appliedCoupons, Map<String, Integer> couponDiscounts) {
            this.originalAmount = originalAmount;
            this.discountAmount = discountAmount;
            this.finalProductAmount = finalProductAmount;
            this.shippingFee = shippingFee;
            this.totalAmount = totalAmount;
            this.appliedCoupons = appliedCoupons;
            this.couponDiscounts = couponDiscounts;
        }

        public int getOriginalAmount() { return originalAmount; }
        public int getDiscountAmount() { return discountAmount; }
        public int getFinalProductAmount() { return finalProductAmount; }
        public int getShippingFee() { return shippingFee; }
        public int getTotalAmount() { return totalAmount; }
        public List<String> getAppliedCoupons() { return appliedCoupons; }
        public Map<String, Integer> getCouponDiscounts() { return couponDiscounts; }
    }

    /**
     * 쿠폰 할인 계산
     *
     * @param order 주문 정보
     * @param coupons 사용할 쿠폰 목록
     * @param currentTime 현재 시간 (쿠폰 유효기간 확인용)
     * @return 할인 계산 결과
     */
    /**
     * 문제: 쿠폰 할인 계산 엔진
     *
     * 실무 시나리오:
     * 이커머스 플랫폼의 다양한 쿠폰 할인을 계산하는 엔진을 구현하세요.
     * 여러 개의 쿠폰이 중복 적용될 수 있으며, 복잡한 비즈니스 규칙을 처리해야 합니다.
     *
     * 쿠폰 타입:
     * 1. PERCENTAGE: 정률 할인 (예: 10% 할인)
     * 2. FIXED_AMOUNT: 정액 할인 (예: 5,000원 할인)
     * 3. FREE_SHIPPING: 무료 배송
     *
     * 비즈니스 규칙:
     * 1. 쿠폰 적용 순서: PERCENTAGE -> FIXED_AMOUNT -> FREE_SHIPPING
     * 2. 최소 주문 금액 조건 확인
     * 3. 최대 할인 금액 제한
     * 4. 쿠폰 유효 기간 확인
     * 5. 중복 사용 불가 쿠폰 처리
     * 6. 카테고리별 적용 가능 쿠폰
     *
     * 할인 계산 순서:
     * 상품 금액 -> 정률 할인 -> 정액 할인 -> 최종 금액 + 배송비
     *
     * 예시:
     * 상품 금액: 50,000원
     * 배송비: 3,000원
     * 쿠폰1: 10% 할인 (최대 5,000원)
     * 쿠폰2: 3,000원 할인
     * 쿠폰3: 무료 배송
     *
     * 계산:
     * 1. 50,000 * 0.9 = 45,000 (정률 할인, 5,000원 할인)
     * 2. 45,000 - 3,000 = 42,000 (정액 할인)
     * 3. 42,000 + 0 = 42,000 (무료 배송)
     */
    public DiscountResult calculateDiscount(Order order, List<Coupon> coupons, LocalDateTime currentTime) {

        return new DiscountResult(0, 0, 0, 0, 0, Collections.emptyList(), Collections.emptyMap());
    }

    /**
     * 쿠폰 유효성 검증
     */
    private boolean isValidCoupon(Coupon coupon, Order order, LocalDateTime currentTime) {
        // 유효 기간 확인
        if (currentTime.isBefore(coupon.getValidFrom()) ||
            currentTime.isAfter(coupon.getValidUntil())) {
            return false;
        }

        // 최소 주문 금액 확인
        if (order.getProductAmount() < coupon.getMinOrderAmount()) {
            return false;
        }

        // 카테고리 확인
        if (coupon.getApplicableCategories() != null &&
            !coupon.getApplicableCategories().isEmpty()) {
            boolean categoryMatch = false;
            for (String category : order.getCategories()) {
                if (coupon.getApplicableCategories().contains(category)) {
                    categoryMatch = true;
                    break;
                }
            }
            if (!categoryMatch) {
                return false;
            }
        }

        return true;
    }

    // ========== 정답 (참고용) ==========

    public DiscountResult calculateDiscountSolution(Order order, List<Coupon> coupons, LocalDateTime currentTime) {
        int currentAmount = order.getProductAmount();
        int currentShippingFee = order.getShippingFee();
        List<String> appliedCoupons = new ArrayList<>();
        Map<String, Integer> couponDiscounts = new HashMap<>();

        // 유효한 쿠폰만 필터링
        List<Coupon> validCoupons = new ArrayList<>();
        for (Coupon coupon : coupons) {
            if (isValidCoupon(coupon, order, currentTime)) {
                validCoupons.add(coupon);
            }
        }

        // 중복 사용 불가 쿠폰 확인
        boolean hasNonStackable = false;
        for (Coupon coupon : validCoupons) {
            if (!coupon.isStackable()) {
                if (hasNonStackable) {
                    // 중복 사용 불가 쿠폰이 여러 개면 첫 번째만 사용
                    break;
                }
                hasNonStackable = true;
            }
        }

        // 쿠폰 타입별 분류
        List<Coupon> percentageCoupons = new ArrayList<>();
        List<Coupon> fixedAmountCoupons = new ArrayList<>();
        List<Coupon> freeShippingCoupons = new ArrayList<>();

        for (Coupon coupon : validCoupons) {
            switch (coupon.getType()) {
                case PERCENTAGE:
                    percentageCoupons.add(coupon);
                    break;
                case FIXED_AMOUNT:
                    fixedAmountCoupons.add(coupon);
                    break;
                case FREE_SHIPPING:
                    freeShippingCoupons.add(coupon);
                    break;
            }
        }

        // 1. PERCENTAGE 쿠폰 적용
        for (Coupon coupon : percentageCoupons) {
            int discount = (currentAmount * coupon.getDiscountValue()) / 100;
            if (coupon.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, coupon.getMaxDiscountAmount());
            }
            currentAmount -= discount;
            appliedCoupons.add(coupon.getCouponId());
            couponDiscounts.put(coupon.getCouponId(), discount);
        }

        // 2. FIXED_AMOUNT 쿠폰 적용
        for (Coupon coupon : fixedAmountCoupons) {
            int discount = Math.min(coupon.getDiscountValue(), currentAmount);
            currentAmount -= discount;
            appliedCoupons.add(coupon.getCouponId());
            couponDiscounts.put(coupon.getCouponId(), discount);
        }

        // 3. FREE_SHIPPING 쿠폰 적용
        if (!freeShippingCoupons.isEmpty()) {
            Coupon shippingCoupon = freeShippingCoupons.get(0);
            appliedCoupons.add(shippingCoupon.getCouponId());
            couponDiscounts.put(shippingCoupon.getCouponId(), currentShippingFee);
            currentShippingFee = 0;
        }

        int totalDiscount = order.getProductAmount() - currentAmount;
        int totalAmount = currentAmount + currentShippingFee;

        return new DiscountResult(
            order.getProductAmount(),
            totalDiscount,
            currentAmount,
            currentShippingFee,
            totalAmount,
            appliedCoupons,
            couponDiscounts
        );
    }
}
