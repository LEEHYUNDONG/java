package com.example.java.algorithm.interview.implementation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 문제: 재고 관리 시스템
 *
 * 실무 시나리오:
 * 이커머스 플랫폼의 재고 관리 시스템을 구현하세요.
 * 동시에 여러 사용자가 같은 상품을 주문할 때 재고 부족이나 과다 판매를 방지해야 합니다.
 *
 * 요구사항:
 * 1. 재고 조회 (getStock)
 * 2. 재고 예약 (reserve) - 주문 시작 시 재고 확보
 * 3. 예약 확정 (confirmReservation) - 결제 완료 시 재고 차감
 * 4. 예약 취소 (cancelReservation) - 결제 실패 시 재고 복구
 * 5. 재고 추가 (addStock) - 입고 처리
 *
 * 제약사항:
 * - Thread-safe 해야 함 (동시성 제어 필수)
 * - 음수 재고 발생 불가
 * - 예약은 일정 시간 후 자동 만료되어야 함
 *
 * 비즈니스 로직:
 * - 실재고 = 물리적 재고
 * - 가용재고 = 실재고 - 예약수량
 * - 예약은 reservationId로 관리
 *
 * 예시:
 * inventory.addStock("ITEM-001", 100);              // 재고 100개 입고
 * inventory.reserve("ITEM-001", "ORDER-1", 10);     // 10개 예약
 * inventory.getAvailableStock("ITEM-001");          // 90 (100 - 10)
 * inventory.confirmReservation("ORDER-1");          // 예약 확정
 * inventory.getStock("ITEM-001");                   // 90 (실재고 차감)
 * inventory.getAvailableStock("ITEM-001");          // 90
 */
public class InventoryManager {

    private final Map<String, ProductInventory> inventoryMap = new ConcurrentHashMap<>();
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();

    /**
     * 재고 추가 (입고)
     */
    public void addStock(String productId, int quantity) {
        // TODO: 구현하세요
        // 힌트:
        // 1. inventoryMap에서 ProductInventory 가져오기 (없으면 생성)
        // 2. ProductInventory의 addStock 메서드 호출
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 재고 예약
     *
     * @param productId 상품 ID
     * @param reservationId 예약 ID (주문 ID 등)
     * @param quantity 예약 수량
     * @return true if reserved successfully, false if insufficient stock
     */
    public boolean reserve(String productId, String reservationId, int quantity) {
        // TODO: 구현하세요
        // 힌트:
        // 1. ProductInventory 조회
        // 2. 가용 재고 확인
        // 3. 가용 재고가 충분하면:
        //    - Reservation 객체 생성
        //    - reservations에 저장
        //    - ProductInventory의 reserve 메서드 호출
        // 4. 부족하면 false 반환
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 예약 확정 (결제 완료)
     */
    public boolean confirmReservation(String reservationId) {
        // TODO: 구현하세요
        // 힌트:
        // 1. reservations에서 Reservation 찾기
        // 2. ProductInventory의 confirmReservation 호출 (실재고 차감)
        // 3. reservations에서 제거
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 예약 취소 (결제 실패, 타임아웃 등)
     */
    public boolean cancelReservation(String reservationId) {
        // TODO: 구현하세요
        // 힌트:
        // 1. reservations에서 Reservation 찾기
        // 2. ProductInventory의 cancelReservation 호출 (예약 수량 복구)
        // 3. reservations에서 제거
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 실재고 조회
     */
    public int getStock(String productId) {
        ProductInventory inventory = inventoryMap.get(productId);
        return inventory == null ? 0 : inventory.getStock();
    }

    /**
     * 가용재고 조회 (실재고 - 예약수량)
     */
    public int getAvailableStock(String productId) {
        ProductInventory inventory = inventoryMap.get(productId);
        return inventory == null ? 0 : inventory.getAvailableStock();
    }

    /**
     * 상품별 재고 정보
     */
    private static class ProductInventory {
        private int stock;              // 실재고
        private int reservedQuantity;   // 예약 수량
        private final Lock lock = new ReentrantLock();

        public void addStock(int quantity) {
            lock.lock();
            try {
                stock += quantity;
            } finally {
                lock.unlock();
            }
        }

        public boolean reserve(int quantity) {
            lock.lock();
            try {
                if (getAvailableStock() >= quantity) {
                    reservedQuantity += quantity;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }

        public void confirmReservation(int quantity) {
            lock.lock();
            try {
                reservedQuantity -= quantity;
                stock -= quantity;
            } finally {
                lock.unlock();
            }
        }

        public void cancelReservation(int quantity) {
            lock.lock();
            try {
                reservedQuantity -= quantity;
            } finally {
                lock.unlock();
            }
        }

        public int getStock() {
            return stock;
        }

        public int getAvailableStock() {
            return stock - reservedQuantity;
        }
    }

    /**
     * 예약 정보
     */
    private static class Reservation {
        private final String productId;
        private final int quantity;

        public Reservation(String productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public String getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    // ========== 정답 (참고용) ==========

    public void addStockSolution(String productId, int quantity) {
        inventoryMap.computeIfAbsent(productId, k -> new ProductInventory())
            .addStock(quantity);
    }

    public boolean reserveSolution(String productId, String reservationId, int quantity) {
        ProductInventory inventory = inventoryMap.computeIfAbsent(
            productId, k -> new ProductInventory()
        );

        if (inventory.reserve(quantity)) {
            reservations.put(reservationId, new Reservation(productId, quantity));
            return true;
        }
        return false;
    }

    public boolean confirmReservationSolution(String reservationId) {
        Reservation reservation = reservations.remove(reservationId);
        if (reservation == null) return false;

        ProductInventory inventory = inventoryMap.get(reservation.getProductId());
        if (inventory != null) {
            inventory.confirmReservation(reservation.getQuantity());
            return true;
        }
        return false;
    }

    public boolean cancelReservationSolution(String reservationId) {
        Reservation reservation = reservations.remove(reservationId);
        if (reservation == null) return false;

        ProductInventory inventory = inventoryMap.get(reservation.getProductId());
        if (inventory != null) {
            inventory.cancelReservation(reservation.getQuantity());
            return true;
        }
        return false;
    }
}
