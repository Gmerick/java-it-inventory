const {test,expect}=require('@playwright/test');
test('revisão visual desktop e celular',async({page,request})=>{
 for(const [sku,name,min,qty] of [['NOTE-01','Notebook Dell Latitude',2,8],['TECL-01','Teclado sem fio',3,12],['CABO-01','Cabo de rede CAT6',5,3],['ADAPT-01','Adaptador USB-C',2,6]]){
  await request.post('/api/products',{data:{sku,name,minimum:min}});await request.post(`/api/products/${sku}/movements`,{data:{delta:qty,reason:'Entrada de laboratório'}});
 }
 await page.goto('/');await expect(page.locator('#workspace')).toHaveAttribute('aria-busy','false');await expect(page.locator('#error')).toBeHidden();
 await page.screenshot({path:'test-results/overview-desktop.png',fullPage:true});
 await page.setViewportSize({width:390,height:844});
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
 await page.screenshot({path:'test-results/overview-mobile.png',fullPage:true});
});
