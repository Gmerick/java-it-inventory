package io.github.gmerick.inventory;

import java.sql.SQLException;
import java.util.*;

public final class InventoryService {
  private final InventoryRepository repository;

  public InventoryService(InventoryRepository repository) {
    this.repository = repository;
  }

  private String sku(String value) {
    if (value == null || !value.trim().matches("[a-zA-Z0-9_-]{1,30}"))
      throw new IllegalArgumentException(
          "SKU deve ter 1 a 30 letras, números, hífen ou sublinhado.");
    return value.trim().toUpperCase(Locale.ROOT);
  }

  private String text(String value, int max) {
    if (value == null || value.isBlank() || value.trim().length() > max)
      throw new IllegalArgumentException("Texto obrigatório; limite de " + max + " caracteres.");
    return value.trim();
  }

  public void add(String sku, String name, int minimum) throws SQLException {
    if (minimum < 0) throw new IllegalArgumentException("Estoque mínimo não pode ser negativo.");
    repository.add(sku(sku), text(name, 120), minimum);
  }

  public void move(String sku, int delta, String reason) throws SQLException {
    if (delta == 0) throw new IllegalArgumentException("Movimentação deve ser diferente de zero.");
    repository.move(sku(sku), delta, text(reason, 200));
  }

  public List<Product> list() throws SQLException {
    return repository.list();
  }

  public List<Movement> history(String sku) throws SQLException {
    return repository.history(sku(sku));
  }

  public Map<String, Long> summary() throws SQLException {
    List<Product> products = list();
    Map<String, Long> result = new LinkedHashMap<>();
    result.put("produtos", (long) products.size());
    result.put("unidades", products.stream().mapToLong(Product::quantity).sum());
    result.put("reposicao", products.stream().filter(Product::needsRestock).count());
    return Collections.unmodifiableMap(result);
  }
}
