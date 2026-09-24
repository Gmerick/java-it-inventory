-- Execute com um cliente SQL conectado ao MESMO banco H2, com a CLI parada.
-- Saldo e reposição
SELECT sku, name, quantity, minimum FROM products WHERE quantity <= minimum ORDER BY sku;
-- Auditoria
SELECT p.sku, p.name, m.delta, m.reason, m.created_at
FROM products p JOIN movements m ON m.sku=p.sku ORDER BY m.id;
-- Reconciliação: deve retornar zero linhas
SELECT p.sku, p.quantity, COALESCE(SUM(m.delta),0) AS total_movimentos
FROM products p LEFT JOIN movements m ON p.sku=m.sku
GROUP BY p.sku,p.quantity HAVING p.quantity<>COALESCE(SUM(m.delta),0);
