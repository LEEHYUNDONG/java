package com.example.java.algorithm.interview.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Inventory Manager 테스트")
class InventoryManagerTest {

    private InventoryManager manager;

    @BeforeEach
    void setUp() {
        manager = new InventoryManager();
    }

    @Test
    @DisplayName("재고 추가")
    void testAddStock() {
        manager.addStockSolution("ITEM-001", 100);

        assertThat(manager.getStock("ITEM-001")).isEqualTo(100);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(100);
    }

    @Test
    @DisplayName("재고 예약 성공")
    void testReserveSuccess() {
        manager.addStockSolution("ITEM-001", 100);

        boolean reserved = manager.reserveSolution("ITEM-001", "ORDER-1", 10);
        assertThat(reserved).isTrue();
        assertThat(manager.getStock("ITEM-001")).isEqualTo(100);  // 실재고 유지
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(90);  // 가용재고 감소
    }

    @Test
    @DisplayName("재고 부족으로 예약 실패")
    void testReserveFailDueToInsufficientStock() {
        manager.addStockSolution("ITEM-001", 10);

        boolean reserved = manager.reserveSolution("ITEM-001", "ORDER-1", 20);
        assertThat(reserved).isFalse();
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(10);
    }

    @Test
    @DisplayName("예약 확정 - 실재고 차감")
    void testConfirmReservation() {
        manager.addStockSolution("ITEM-001", 100);
        manager.reserveSolution("ITEM-001", "ORDER-1", 10);

        boolean confirmed = manager.confirmReservationSolution("ORDER-1");
        assertThat(confirmed).isTrue();
        assertThat(manager.getStock("ITEM-001")).isEqualTo(90);  // 실재고 차감
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(90);  // 예약 해제
    }

    @Test
    @DisplayName("예약 취소 - 재고 복구")
    void testCancelReservation() {
        manager.addStockSolution("ITEM-001", 100);
        manager.reserveSolution("ITEM-001", "ORDER-1", 10);

        boolean cancelled = manager.cancelReservationSolution("ORDER-1");
        assertThat(cancelled).isTrue();
        assertThat(manager.getStock("ITEM-001")).isEqualTo(100);  // 실재고 유지
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(100);  // 예약 해제
    }

    @Test
    @DisplayName("존재하지 않는 예약 확정")
    void testConfirmNonExistentReservation() {
        boolean confirmed = manager.confirmReservationSolution("NON-EXISTENT");
        assertThat(confirmed).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 예약 취소")
    void testCancelNonExistentReservation() {
        boolean cancelled = manager.cancelReservationSolution("NON-EXISTENT");
        assertThat(cancelled).isFalse();
    }

    @Test
    @DisplayName("여러 예약 처리")
    void testMultipleReservations() {
        manager.addStockSolution("ITEM-001", 100);

        manager.reserveSolution("ITEM-001", "ORDER-1", 30);
        manager.reserveSolution("ITEM-001", "ORDER-2", 20);
        manager.reserveSolution("ITEM-001", "ORDER-3", 10);

        assertThat(manager.getStock("ITEM-001")).isEqualTo(100);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(40);

        manager.confirmReservationSolution("ORDER-1");
        assertThat(manager.getStock("ITEM-001")).isEqualTo(70);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(40);

        manager.cancelReservationSolution("ORDER-2");
        assertThat(manager.getStock("ITEM-001")).isEqualTo(70);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(60);
    }

    @Test
    @DisplayName("등록되지 않은 상품 조회")
    void testGetStockForNonExistentProduct() {
        assertThat(manager.getStock("NON-EXISTENT")).isEqualTo(0);
        assertThat(manager.getAvailableStock("NON-EXISTENT")).isEqualTo(0);
    }

    @Test
    @DisplayName("예약 가능한 최대 수량 테스트")
    void testReserveExactAvailableStock() {
        manager.addStockSolution("ITEM-001", 100);
        manager.reserveSolution("ITEM-001", "ORDER-1", 30);

        boolean reserved = manager.reserveSolution("ITEM-001", "ORDER-2", 70);
        assertThat(reserved).isTrue();
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(0);
    }

    @Test
    @DisplayName("동시 예약 요청 - Thread Safety")
    void testConcurrentReservations() throws InterruptedException {
        manager.addStockSolution("ITEM-001", 100);

        int threadCount = 10;
        int reserveAmountPerThread = 15;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            int orderId = i;
            Thread thread = new Thread(() -> {
                try {
                    latch.countDown();
                    latch.await();  // 모든 스레드가 동시에 시작
                    boolean reserved = manager.reserveSolution("ITEM-001", "ORDER-" + orderId, reserveAmountPerThread);
                    if (reserved) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // 100개 재고에 15개씩 예약하면 최대 6개만 성공 (90개), 7개는 실패
        assertThat(successCount.get()).isEqualTo(6);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(10);
    }

    @Test
    @DisplayName("동시 예약 확정 및 취소 - Thread Safety")
    void testConcurrentConfirmAndCancel() throws InterruptedException {
        manager.addStockSolution("ITEM-001", 100);

        // 10개의 예약 생성
        for (int i = 0; i < 10; i++) {
            manager.reserveSolution("ITEM-001", "ORDER-" + i, 5);
        }

        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            int orderId = i;
            Thread thread = new Thread(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    if (orderId % 2 == 0) {
                        manager.confirmReservationSolution("ORDER-" + orderId);
                    } else {
                        manager.cancelReservationSolution("ORDER-" + orderId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // 5개 확정 (실재고 차감 25), 5개 취소 (예약 해제 25)
        // 실재고: 100 - 25 = 75
        // 가용재고: 75 - 0 = 75
        assertThat(manager.getStock("ITEM-001")).isEqualTo(75);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(75);
    }

    @Test
    @DisplayName("재고 추가 누적 테스트")
    void testAccumulativeStockAddition() {
        manager.addStockSolution("ITEM-001", 50);
        manager.addStockSolution("ITEM-001", 30);
        manager.addStockSolution("ITEM-001", 20);

        assertThat(manager.getStock("ITEM-001")).isEqualTo(100);
    }

    @Test
    @DisplayName("복잡한 재고 관리 시나리오")
    void testComplexInventoryScenario() {
        manager.addStockSolution("ITEM-001", 100);

        // 3개 예약
        manager.reserveSolution("ITEM-001", "ORDER-1", 30);
        manager.reserveSolution("ITEM-001", "ORDER-2", 20);
        manager.reserveSolution("ITEM-001", "ORDER-3", 10);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(40);

        // 1개 확정
        manager.confirmReservationSolution("ORDER-1");
        assertThat(manager.getStock("ITEM-001")).isEqualTo(70);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(40);

        // 1개 취소
        manager.cancelReservationSolution("ORDER-2");
        assertThat(manager.getStock("ITEM-001")).isEqualTo(70);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(60);

        // 재고 추가
        manager.addStockSolution("ITEM-001", 30);
        assertThat(manager.getStock("ITEM-001")).isEqualTo(100);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(90);

        // 남은 예약 확정
        manager.confirmReservationSolution("ORDER-3");
        assertThat(manager.getStock("ITEM-001")).isEqualTo(90);
        assertThat(manager.getAvailableStock("ITEM-001")).isEqualTo(90);
    }
}
