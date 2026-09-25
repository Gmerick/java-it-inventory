const {test,expect}=require('@playwright/test');
test('cadastro, entrada, saída, saldo insuficiente e histórico',async({page})=>{
 await page.goto('/');await page.getByRole('button',{name:'Novo produto',exact:true}).click();
 await page.getByLabel('Código do produto (SKU)').fill('MOUSE-UI');await page.getByLabel('Nome do produto').fill('Mouse de laboratório');await page.getByLabel('Estoque mínimo').fill('2');
 await page.getByRole('button',{name:'Cadastrar produto'}).click();
 const row=page.getByRole('row').filter({hasText:'MOUSE-UI'});await expect(row).toContainText('Repor estoque');
 await row.getByRole('button',{name:'Movimentar',exact:true}).click();await page.getByLabel('Quantidade',{exact:true}).fill('5');await page.getByLabel('Motivo',{exact:true}).fill('Compra de laboratório');await page.getByRole('button',{name:'Confirmar movimentação'}).click();await expect(row).toContainText('5 un.');
 await row.getByRole('button',{name:'Movimentar',exact:true}).click();await page.getByLabel('Tipo de movimentação').selectOption('out');await page.getByLabel('Quantidade',{exact:true}).fill('8');await page.getByLabel('Motivo',{exact:true}).fill('Saída inválida');await page.getByRole('button',{name:'Confirmar movimentação'}).click();await expect(page.locator('#form-error')).toContainText('saldo insuficiente');
 await page.getByLabel('Quantidade',{exact:true}).fill('3');await page.getByRole('button',{name:'Confirmar movimentação'}).click();await expect(row).toContainText('2 un.');
 await row.getByRole('button',{name:'Histórico de Mouse de laboratório'}).click();await expect(page.locator('.history li')).toHaveCount(2);await expect(page.locator('.history')).toContainText('Entrada · 5 unidades');
 await page.getByRole('button',{name:'Fechar',exact:true}).click();
 await page.getByRole('button',{name:'Novo produto',exact:true}).click();await page.getByLabel('Código do produto (SKU)').fill('MOUSE-UI');await page.getByLabel('Nome do produto').fill('Duplicado');await page.getByRole('button',{name:'Cadastrar produto'}).click();await expect(page.locator('#form-error')).toContainText('já está cadastrado');
});
test('API rejeita entrada inválida e conserva saldo',async({request})=>{
 const response=await request.post('/api/products',{data:{sku:'BAD',name:'Inválido',minimum:-1}});expect(response.status()).toBe(400);
 const list=await (await request.get('/api/products')).json();expect(list.find(x=>x.sku==='BAD')).toBeUndefined();
});
