package com.example.java.algorithm.interview.implementation;

import java.util.*;

/**
 * 문제: Order Book Matching Engine (주문 매칭 엔진)
 *
 * 실무 시나리오:
 * 암호화폐 거래소나 주식 거래소의 주문 매칭 시스템을 구현하세요.
 * 매수 주문(BUY)과 매도 주문(SELL)을 가격-시간 우선순위로 매칭합니다.
 *
 * 매칭 규칙:
 * 1. Price Priority (가격 우선)
 *    - 매수: 높은 가격 우선
 *    - 매도: 낮은 가격 우선
 * 2. Time Priority (시간 우선)
 *    - 같은 가격이면 먼저 온 주문 우선
 *
 * 주문 타입:
 * - LIMIT: 지정가 주문 (특정 가격 이하/이상에서만 체결)
 * - MARKET: 시장가 주문 (즉시 체결, 가장 좋은 가격)
 *
 * 요구사항:
 * 1. addOrder(): 주문 추가 및 즉시 매칭 시도
 * 2. cancelOrder(): 주문 취소
 * 3. getOrderBook(): 현재 주문장 상태 조회
 * 4. 부분 체결 지원 (주문 수량 일부만 체결 가능)
 *
 * 예시:
 * OrderBookMatcher matcher = new OrderBookMatcher();
 *
 * matcher.addOrder(SELL, LIMIT, price=100, qty=10);  // 매도 주문
 * matcher.addOrder(SELL, LIMIT, price=101, qty=5);   // 매도 주문
 * matcher.addOrder(BUY, LIMIT, price=99, qty=10);    // 매수 주문 (매칭 안됨)
 * matcher.addOrder(BUY, LIMIT, price=100, qty=7);    // 매칭! 7개 체결
 * // 결과: SELL 100원 주문은 3개 남음
 */
public class OrderBookMatcher {

    public enum Side {
        BUY,
        SELL
    }

    public enum OrderType {
        LIMIT,    // 지정가
        MARKET    // 시장가
    }

    private long nextOrderId = 1;
    // 매수 주문: 가격 내림차순 (높은 가격 우선), 같은 가격이면 시간 오름차순
    private final TreeMap<Integer, LinkedList<Order>> buyOrders = new TreeMap<>(Comparator.reverseOrder());
    // 매도 주문: 가격 오름차순 (낮은 가격 우선), 같은 가격이면 시간 오름차순
    private final TreeMap<Integer, LinkedList<Order>> sellOrders = new TreeMap<>();
    private final Map<Long, Order> orderMap = new HashMap<>();
    private final List<Trade> tradeHistory = new ArrayList<>();

    /**
     * 주문 추가 및 매칭
     *
     * @return 생성된 주문 ID
     */
    public long addOrder(Side side, OrderType type, Integer price, int quantity) {
        // TODO: 구현하세요
        // 힌트:
        // 1. Order 객체 생성
        // 2. MARKET 주문이면 price를 적절히 설정 (BUY: Integer.MAX_VALUE, SELL: 0)
        // 3. matchOrder() 호출하여 매칭 시도
        // 4. 남은 수량이 있으면 주문장에 추가
        // 5. orderMap에 저장
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 주문 취소
     */
    public boolean cancelOrder(long orderId) {
        // TODO: 구현하세요
        // 힌트:
        // 1. orderMap에서 주문 찾기
        // 2. 해당 주문을 buyOrders 또는 sellOrders에서 제거
        // 3. orderMap에서도 제거
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 주문 매칭 시도
     */
    private void matchOrder(Order order) {
        // TODO: 구현하세요
        // 힌트:
        // 1. order가 BUY면 sellOrders와 매칭, SELL이면 buyOrders와 매칭
        // 2. 가격이 맞는 상대 주문을 찾아서 체결
        // 3. 수량만큼 차감하고 Trade 기록
        // 4. 주문이 완전히 체결되면 주문장에서 제거
        TreeMap<Integer, LinkedList<Order>> oppositeBook =
            order.side == Side.BUY ? sellOrders : buyOrders;

        // 매칭 가능한 주문 찾기
        while (order.remainingQuantity > 0 && !oppositeBook.isEmpty()) {
            Map.Entry<Integer, LinkedList<Order>> entry = oppositeBook.firstEntry();
            Integer bestPrice = entry.getKey();
            LinkedList<Order> orders = entry.getValue();

            // 가격 매칭 확인
            if (order.side == Side.BUY && order.price < bestPrice) break;
            if (order.side == Side.SELL && order.price > bestPrice) break;

            Order oppositeOrder = orders.getFirst();
            int matchQuantity = Math.min(order.remainingQuantity, oppositeOrder.remainingQuantity);
            int tradePrice = oppositeOrder.price; // 먼저 온 주문의 가격으로 체결

            // 체결 기록
            Trade trade = new Trade(
                order.orderId,
                oppositeOrder.orderId,
                tradePrice,
                matchQuantity
            );
            tradeHistory.add(trade);

            // 수량 차감
            order.remainingQuantity -= matchQuantity;
            oppositeOrder.remainingQuantity -= matchQuantity;

            // 완전 체결된 주문 제거
            if (oppositeOrder.remainingQuantity == 0) {
                orders.removeFirst();
                orderMap.remove(oppositeOrder.orderId);
                if (orders.isEmpty()) {
                    oppositeBook.remove(bestPrice);
                }
            }
        }
    }

    /**
     * 현재 주문장 상태 조회
     */
    public OrderBook getOrderBook() {
        return new OrderBook(
            new TreeMap<>(buyOrders),
            new TreeMap<>(sellOrders)
        );
    }

    /**
     * 체결 내역 조회
     */
    public List<Trade> getTradeHistory() {
        return new ArrayList<>(tradeHistory);
    }

    public static class Order {
        final long orderId;
        final Side side;
        final OrderType type;
        final int price;
        final int quantity;
        int remainingQuantity;
        final long timestamp;

        public Order(long orderId, Side side, OrderType type, Integer price,
                    int quantity, long timestamp) {
            this.orderId = orderId;
            this.side = side;
            this.type = type;
            this.price = price;
            this.quantity = quantity;
            this.remainingQuantity = quantity;
            this.timestamp = timestamp;
        }

        public long getOrderId() { return orderId; }
        public Side getSide() { return side; }
        public int getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public int getRemainingQuantity() { return remainingQuantity; }
    }

    public static class Trade {
        final long buyOrderId;
        final long sellOrderId;
        final int price;
        final int quantity;

        public Trade(long buyOrderId, long sellOrderId, int price, int quantity) {
            this.buyOrderId = buyOrderId;
            this.sellOrderId = sellOrderId;
            this.price = price;
            this.quantity = quantity;
        }

        public long getBuyOrderId() { return buyOrderId; }
        public long getSellOrderId() { return sellOrderId; }
        public int getPrice() { return price; }
        public int getQuantity() { return quantity; }
    }

    public static class OrderBook {
        final TreeMap<Integer, LinkedList<Order>> buyOrders;
        final TreeMap<Integer, LinkedList<Order>> sellOrders;

        public OrderBook(TreeMap<Integer, LinkedList<Order>> buyOrders,
                        TreeMap<Integer, LinkedList<Order>> sellOrders) {
            this.buyOrders = buyOrders;
            this.sellOrders = sellOrders;
        }

        public TreeMap<Integer, LinkedList<Order>> getBuyOrders() { return buyOrders; }
        public TreeMap<Integer, LinkedList<Order>> getSellOrders() { return sellOrders; }
    }

    // ========== 정답 (참고용) ==========

    public long addOrderSolution(Side side, OrderType type, Integer price, int quantity) {
        // MARKET 주문의 가격 설정
        if (type == OrderType.MARKET) {
            price = side == Side.BUY ? Integer.MAX_VALUE : 0;
        }

        Order order = new Order(nextOrderId++, side, type, price, quantity, System.nanoTime());

        // 매칭 시도
        matchOrder(order);

        // 남은 수량이 있으면 주문장에 추가 (MARKET 주문은 추가하지 않음)
        if (order.remainingQuantity > 0 && type == OrderType.LIMIT) {
            TreeMap<Integer, LinkedList<Order>> book = side == Side.BUY ? buyOrders : sellOrders;
            book.computeIfAbsent(price, k -> new LinkedList<>()).add(order);
            orderMap.put(order.orderId, order);
        }

        return order.orderId;
    }

    public boolean cancelOrderSolution(long orderId) {
        Order order = orderMap.remove(orderId);
        if (order == null) return false;

        TreeMap<Integer, LinkedList<Order>> book =
            order.side == Side.BUY ? buyOrders : sellOrders;

        LinkedList<Order> orders = book.get(order.price);
        if (orders != null) {
            orders.remove(order);
            if (orders.isEmpty()) {
                book.remove(order.price);
            }
        }
        return true;
    }
}
