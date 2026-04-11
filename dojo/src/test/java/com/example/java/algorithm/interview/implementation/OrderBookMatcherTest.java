package com.example.java.algorithm.interview.implementation;

import com.example.java.algorithm.interview.implementation.OrderBookMatcher.OrderType;
import com.example.java.algorithm.interview.implementation.OrderBookMatcher.Side;
import com.example.java.algorithm.interview.implementation.OrderBookMatcher.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Order Book Matcher 테스트")
class OrderBookMatcherTest {

    private OrderBookMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new OrderBookMatcher();
    }

    @Test
    @DisplayName("매도 주문 추가 (매칭 없음)")
    void testAddSellOrderNoMatch() {
        long orderId = matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 10);

        assertThat(orderId).isGreaterThan(0);
        assertThat(matcher.getOrderBook().getSellOrders()).hasSize(1);
        assertThat(matcher.getTradeHistory()).isEmpty();
    }

    @Test
    @DisplayName("매수 주문 추가 (매칭 없음)")
    void testAddBuyOrderNoMatch() {
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 10);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 99, 10);

        assertThat(matcher.getOrderBook().getBuyOrders()).hasSize(1);
        assertThat(matcher.getOrderBook().getSellOrders()).hasSize(1);
        assertThat(matcher.getTradeHistory()).isEmpty();
    }

    @Test
    @DisplayName("완전 매칭 - 같은 수량")
    void testFullMatch() {
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 10);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 100, 10);

        List<Trade> trades = matcher.getTradeHistory();
        assertThat(trades).hasSize(1);
        assertThat(trades.get(0).getPrice()).isEqualTo(100);
        assertThat(trades.get(0).getQuantity()).isEqualTo(10);
        assertThat(matcher.getOrderBook().getSellOrders()).isEmpty();
        assertThat(matcher.getOrderBook().getBuyOrders()).isEmpty();
    }

    @Test
    @DisplayName("부분 매칭 - 매수 수량이 더 적음")
    void testPartialMatchBuyLess() {
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 10);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 100, 7);

        List<Trade> trades = matcher.getTradeHistory();
        assertThat(trades).hasSize(1);
        assertThat(trades.get(0).getQuantity()).isEqualTo(7);

        // 매도 주문 3개 남음
        assertThat(matcher.getOrderBook().getSellOrders().get(100).getFirst().getRemainingQuantity()).isEqualTo(3);
        assertThat(matcher.getOrderBook().getBuyOrders()).isEmpty();
    }

    @Test
    @DisplayName("부분 매칭 - 매도 수량이 더 적음")
    void testPartialMatchSellLess() {
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 5);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 100, 10);

        List<Trade> trades = matcher.getTradeHistory();
        assertThat(trades).hasSize(1);
        assertThat(trades.get(0).getQuantity()).isEqualTo(5);

        // 매수 주문 5개 남음
        assertThat(matcher.getOrderBook().getBuyOrders().get(100).getFirst().getRemainingQuantity()).isEqualTo(5);
        assertThat(matcher.getOrderBook().getSellOrders()).isEmpty();
    }

    @Test
    @DisplayName("가격 우선순위 - 매도 (낮은 가격 우선)")
    void testPricePriorityForSell() {
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 102, 5);
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 5);
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 101, 5);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 102, 10);

        List<Trade> trades = matcher.getTradeHistory();
        assertThat(trades).hasSize(2);
        assertThat(trades.get(0).getPrice()).isEqualTo(100);  // 가장 낮은 가격 먼저
        assertThat(trades.get(1).getPrice()).isEqualTo(101);
    }

    @Test
    @DisplayName("가격 우선순위 - 매수 (높은 가격 우선)")
    void testPricePriorityForBuy() {
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 98, 5);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 100, 5);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 99, 5);
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 98, 10);

        List<Trade> trades = matcher.getTradeHistory();
        assertThat(trades).hasSize(2);
        assertThat(trades.get(0).getPrice()).isEqualTo(100);  // 가장 높은 가격 먼저
        assertThat(trades.get(1).getPrice()).isEqualTo(99);
    }

    @Test
    @DisplayName("시간 우선순위 - 같은 가격일 때 먼저 온 주문 우선")
    void testTimePriority() {
        long order1 = matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 5);
        long order2 = matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 5);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 100, 7);

        List<Trade> trades = matcher.getTradeHistory();
        assertThat(trades).hasSize(2);
        assertThat(trades.get(0).getSellOrderId()).isEqualTo(order1);  // 먼저 온 주문
        assertThat(trades.get(0).getQuantity()).isEqualTo(5);
        assertThat(trades.get(1).getSellOrderId()).isEqualTo(order2);
        assertThat(trades.get(1).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("시장가 매수 주문 - 즉시 체결")
    void testMarketBuyOrder() {
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 5);
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 101, 5);
        matcher.addOrderSolution(Side.BUY, OrderType.MARKET, null, 8);

        List<Trade> trades = matcher.getTradeHistory();
        assertThat(trades).hasSize(2);
        assertThat(trades.get(0).getPrice()).isEqualTo(100);
        assertThat(trades.get(0).getQuantity()).isEqualTo(5);
        assertThat(trades.get(1).getPrice()).isEqualTo(101);
        assertThat(trades.get(1).getQuantity()).isEqualTo(3);

        // 시장가 주문은 주문장에 남지 않음
        assertThat(matcher.getOrderBook().getBuyOrders()).isEmpty();
    }

    @Test
    @DisplayName("시장가 매도 주문 - 즉시 체결")
    void testMarketSellOrder() {
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 100, 5);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 99, 5);
        matcher.addOrderSolution(Side.SELL, OrderType.MARKET, null, 8);

        List<Trade> trades = matcher.getTradeHistory();
        assertThat(trades).hasSize(2);
        assertThat(trades.get(0).getPrice()).isEqualTo(100);  // 높은 가격 먼저
        assertThat(trades.get(0).getQuantity()).isEqualTo(5);
        assertThat(trades.get(1).getPrice()).isEqualTo(99);
        assertThat(trades.get(1).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("주문 취소")
    void testCancelOrder() {
        long orderId = matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 10);

        boolean cancelled = matcher.cancelOrderSolution(orderId);
        assertThat(cancelled).isTrue();
        assertThat(matcher.getOrderBook().getSellOrders()).isEmpty();
    }

    @Test
    @DisplayName("부분 체결된 주문 취소")
    void testCancelPartiallyFilledOrder() {
        long orderId = matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 10);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 100, 7);

        boolean cancelled = matcher.cancelOrderSolution(orderId);
        assertThat(cancelled).isTrue();
        assertThat(matcher.getOrderBook().getSellOrders()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 주문 취소")
    void testCancelNonExistentOrder() {
        boolean cancelled = matcher.cancelOrderSolution(999L);
        assertThat(cancelled).isFalse();
    }

    @Test
    @DisplayName("완전 체결된 주문 취소 시도")
    void testCancelFullyFilledOrder() {
        long orderId = matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 10);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 100, 10);

        boolean cancelled = matcher.cancelOrderSolution(orderId);
        assertThat(cancelled).isFalse();  // 이미 완전 체결되어 주문장에 없음
    }

    @Test
    @DisplayName("복잡한 매칭 시나리오")
    void testComplexMatchingScenario() {
        // 매도 주문들
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 10);
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 101, 5);
        matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 102, 8);

        // 매수 주문들
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 99, 5);
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 100, 5);

        // 첫 번째 매칭 발생
        List<Trade> trades = matcher.getTradeHistory();
        assertThat(trades).hasSize(1);
        assertThat(trades.get(0).getPrice()).isEqualTo(100);
        assertThat(trades.get(0).getQuantity()).isEqualTo(5);

        // 추가 매수로 완전 매칭
        matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 102, 20);

        trades = matcher.getTradeHistory();
        assertThat(trades).hasSize(4);
        // 100원 5개 남음, 101원 5개, 102원 8개 = 총 18개 체결
        assertThat(trades.stream().mapToInt(Trade::getQuantity).sum()).isEqualTo(23);
    }

    @Test
    @DisplayName("주문 ID는 순차적으로 증가")
    void testOrderIdIncrement() {
        long id1 = matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 100, 10);
        long id2 = matcher.addOrderSolution(Side.BUY, OrderType.LIMIT, 99, 10);
        long id3 = matcher.addOrderSolution(Side.SELL, OrderType.LIMIT, 101, 10);

        assertThat(id2).isEqualTo(id1 + 1);
        assertThat(id3).isEqualTo(id2 + 1);
    }
}
