package io.github.gmerick.inventory;

import java.sql.SQLException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class InventoryErrors {
  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ProblemDetail> input(IllegalArgumentException e) { return error(HttpStatus.BAD_REQUEST, e.getMessage()); }
  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  ResponseEntity<ProblemDetail> validation(Exception e) { return error(HttpStatus.BAD_REQUEST, "Confira os campos obrigatórios e os números informados."); }
  @ExceptionHandler(SQLException.class)
  ResponseEntity<ProblemDetail> database(SQLException e) {
    return "23505".equals(e.getSQLState()) ? error(HttpStatus.CONFLICT, "Este SKU já está cadastrado.")
        : error(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar. Tente novamente.");
  }
  private ResponseEntity<ProblemDetail> error(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, message));
  }
}
