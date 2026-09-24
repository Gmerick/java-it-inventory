package io.github.gmerick.inventory;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;

class InventoryTest {
  InventoryService service;
  String url;

  @BeforeEach
  void setup() throws SQLException {
    url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
    service = new InventoryService(new InventoryRepository(url));
    service.add("mouse", "Mouse", 2);
  }

  @Test
  void movementPersistsAndReportsRestock() throws Exception {
    service.move("mouse", 5, "Compra");
    service.move("MOUSE", -3, "Entrega");
    var reopened = new InventoryService(new InventoryRepository(url));
    assertEquals(2, reopened.list().get(0).quantity());
    assertEquals(2, reopened.history("mouse").size());
    assertEquals(Map.of("produtos", 1L, "unidades", 2L, "reposicao", 1L), reopened.summary());
  }

  @Test
  void insufficientStockDoesNotWriteHistory() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> service.move("mouse", -1, "Entrega"));
    assertEquals(0, service.list().get(0).quantity());
    assertTrue(service.history("mouse").isEmpty());
  }

  @Test
  void duplicateSkuIsRejectedByDatabase() {
    assertThrows(SQLException.class, () -> service.add("MOUSE", "Outro", 0));
  }

  @Test
  void invalidInputIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> service.move("mouse", 0, "Motivo"));
    assertThrows(IllegalArgumentException.class, () -> service.add("x", " ", 0));
    assertThrows(IllegalArgumentException.class, () -> service.add("x", "Nome", -1));
    assertThrows(
        IllegalArgumentException.class, () -> service.add("x'; DROP TABLE products;--", "Nome", 0));
  }

  @Test
  void missingProductCannotMove() {
    assertThrows(IllegalArgumentException.class, () -> service.move("missing", 1, "Compra"));
  }

  @Test
  void transactionRollsBackWhenAuditInsertFails() throws Exception {
    try (Connection c = DriverManager.getConnection(url, "sa", "");
        Statement s = c.createStatement()) {
      s.execute("ALTER TABLE movements ADD CONSTRAINT reason_limit CHECK(LENGTH(reason)<5)");
    }
    assertThrows(SQLException.class, () -> service.move("mouse", 1, "Motivo longo"));
    assertEquals(0, service.list().get(0).quantity());
    assertTrue(service.history("mouse").isEmpty());
  }

  @Test
  void concurrentWithdrawalsCannotOverdraw() throws Exception {
    service.move("mouse", 1, "Compra");
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    Callable<Boolean> withdraw =
        () -> {
          start.await();
          try {
            service.move("mouse", -1, "Entrega");
            return true;
          } catch (IllegalArgumentException e) {
            return false;
          }
        };
    try {
      Future<Boolean> a = pool.submit(withdraw), b = pool.submit(withdraw);
      start.countDown();
      assertNotEquals(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
      assertEquals(0, service.list().get(0).quantity());
      assertEquals(2, service.history("mouse").size());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void overflowDoesNotChangeStock() throws Exception {
    service.move("mouse", Integer.MAX_VALUE, "Compra");
    assertThrows(IllegalArgumentException.class, () -> service.move("mouse", 1, "Compra"));
    assertEquals(Integer.MAX_VALUE, service.list().get(0).quantity());
  }
}
