package io.github.gmerick.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.sql.SQLException;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InventoryController {
  public record NewProduct(@NotBlank String sku, @NotBlank String name, @NotNull @Min(0) Integer minimum) {}
  public record NewMovement(@NotNull Integer delta, @NotBlank String reason) {}
  private final InventoryService service;
  public InventoryController(InventoryService service) { this.service = service; }
  @GetMapping("/products")
  public List<Product> products() throws SQLException { return service.list(); }
  @GetMapping("/summary")
  public Map<String, Long> summary() throws SQLException { return service.summary(); }
  @PostMapping("/products")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> create(@Valid @RequestBody NewProduct product) throws SQLException {
    service.add(product.sku(), product.name(), product.minimum());
    return Map.of("message", "Produto cadastrado.");
  }
  @PostMapping("/products/{sku}/movements")
  public Map<String, String> move(@PathVariable String sku, @Valid @RequestBody NewMovement movement) throws SQLException {
    service.move(sku, movement.delta(), movement.reason());
    return Map.of("message", "Movimentação registrada.");
  }
  @GetMapping("/products/{sku}/history")
  public List<Movement> history(@PathVariable String sku) throws SQLException { return service.history(sku); }
}
