package io.github.gmerick.inventory;

import java.sql.SQLException;

public final class InventoryApp {
  public static void main(String[] args) {
    try {
      run(args);
    } catch (IllegalArgumentException e) {
      System.err.println("Entrada inválida: " + e.getMessage());
      System.exit(2);
    } catch (SQLException e) {
      System.err.println(
          "23505".equals(e.getSQLState())
              ? "SKU já cadastrado."
              : "Falha no banco de dados. Verifique o caminho e se outro processo usa o arquivo.");
      System.exit(3);
    }
  }

  static void run(String[] args) throws SQLException {
    if (args.length == 0 || args[0].equals("help")) {
      System.out.println(
          "Comandos: add SKU \"Nome\" MINIMO | in SKU QTD \"Motivo\" | out SKU QTD \"Motivo\" |"
              + " list | history SKU | summary | demo");
      return;
    }
    String url =
        args[0].equals("demo")
            ? "jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1"
            : System.getenv().getOrDefault("INVENTORY_DB_URL", "jdbc:h2:file:./data/inventory");
    InventoryService service = new InventoryService(new InventoryRepository(url));
    switch (args[0]) {
      case "add" -> {
        length(args, 4);
        service.add(args[1], args[2], Integer.parseInt(args[3]));
        System.out.println("Produto cadastrado.");
      }
      case "in", "out" -> {
        length(args, 4);
        int quantity = Integer.parseInt(args[2]);
        if (quantity <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva.");
        service.move(args[1], args[0].equals("out") ? -quantity : quantity, args[3]);
        System.out.println("Movimentação registrada.");
      }
      case "list" -> {
        length(args, 1);
        service.list().forEach(System.out::println);
      }
      case "history" -> {
        length(args, 2);
        service.history(args[1]).forEach(System.out::println);
      }
      case "summary" -> {
        length(args, 1);
        System.out.println(service.summary());
      }
      case "demo" -> {
        length(args, 1);
        service.add("MOUSE-01", "Mouse USB", 2);
        service.move("MOUSE-01", 5, "Compra fictícia");
        service.move("MOUSE-01", 3 * -1, "Entrega fictícia");
        service.list().forEach(System.out::println);
        service.history("MOUSE-01").forEach(System.out::println);
        System.out.println(service.summary());
        try {
          service.move("MOUSE-01", -10, "Saída inválida");
        } catch (IllegalArgumentException e) {
          System.out.println("Bloqueio esperado: " + e.getMessage());
        }
      }
      default -> throw new IllegalArgumentException("Comando desconhecido. Use help.");
    }
  }

  private static void length(String[] args, int expected) {
    if (args.length != expected)
      throw new IllegalArgumentException("Quantidade incorreta de argumentos. Use help.");
  }
}
