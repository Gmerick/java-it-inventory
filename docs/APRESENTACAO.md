# Apresentação: 5 a 10 minutos

## Preparação

1. Instale JDK e Maven, clone o repositório e execute `mvn clean verify` antes da entrevista.
2. Abra `InventoryService`, `InventoryRepository` e `InventoryTest` na IDE.
3. Deixe um terminal na raiz do repositório. `demo` é seguro para repetir e funciona sem internet após o build.

## Demonstração de 5 minutos

| Tempo | Ação | Explicação |
| --- | --- | --- |
| 0–1 min | Apresente o problema | Controlar materiais e rastrear movimentações de TI |
| 1–2 min | `java -jar target/app.jar demo` | Compra 5, saída 3, saldo 2, reposição e rejeição de saída 10 |
| 2–3 min | Abra `move` no repository | UPDATE protegido por condição e INSERT de auditoria na mesma transação |
| 3–4 min | Abra `summary` | List guarda produtos; Map guarda indicadores; Streams calculam totais |
| 4–5 min | Mostre os testes | Estoque negativo, rollback e duas retiradas concorrentes |

Para 10 minutos, execute os comandos persistentes do README com um SKU novo e rode `mvn test`.

## Sugestão de fala — adapte após estudar

“O projeto parte de uma rotina comum de suporte: controlar materiais de TI. Separei a entrada, as regras e o acesso ao banco. A principal regra é impedir saldo negativo e manter o histórico consistente. Usei JDBC com SQL parametrizado e uma transação para atualizar o saldo junto com a movimentação. Os indicadores usam Collections e Streams. É um laboratório de Java, sem implantação em produção.”

## Perguntas prováveis

**Por que uma transação?** Se o saldo fosse confirmado e a auditoria falhasse, o histórico deixaria de explicar o estoque. O rollback desfaz ambas as alterações.

**Por que validar no banco também?** Outra conexão pode tentar alterar o saldo ao mesmo tempo. O UPDATE condicionado protege o estado atual; CHECK e PK são proteções adicionais.

**Por que PreparedStatement?** Os valores são parâmetros, sem concatenação de entrada no SQL. Também facilita tipos e legibilidade.

**Por que record?** Para representar um snapshot imutável. A regra de reposição está próxima do dado que ela utiliza.

**Como comprovar persistência?** Execute `add` e `in`, termine o processo e execute `list` em um novo processo na mesma pasta.

## Exercício de domínio

Adicione um comando `restock` que liste apenas produtos que precisam de reposição. Escreva um teste para saldo abaixo, igual e acima do mínimo. Faça um commit separado com essa mudança.
