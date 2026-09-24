package io.github.gmerick.inventory;

/** Snapshot imutável do estoque. */
public record Product(String sku, String name, int quantity, int minimum) {
  public boolean needsRestock() {
    return quantity <= minimum;
  }
}
