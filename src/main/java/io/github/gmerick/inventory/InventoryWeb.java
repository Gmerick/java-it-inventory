package io.github.gmerick.inventory;

import java.sql.SQLException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
public class InventoryWeb {
  @Bean
  InventoryService inventoryService(@Value("${inventory.db-url}") String url) throws SQLException {
    return new InventoryService(new InventoryRepository(url));
  }
}
