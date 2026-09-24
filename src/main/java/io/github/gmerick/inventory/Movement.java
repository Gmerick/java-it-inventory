package io.github.gmerick.inventory;

public record Movement(long id, String sku, int delta, String reason, String createdAt) {}
