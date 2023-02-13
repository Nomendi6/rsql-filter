import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'app-object',
        data: { pageTitle: 'AppObjects' },
        loadChildren: () => import('./app-object/app-object.module').then(m => m.AppObjectModule),
      },
      {
        path: 'product-type',
        data: { pageTitle: 'ProductTypes' },
        loadChildren: () => import('./product-type/product-type.module').then(m => m.ProductTypeModule),
      },
      {
        path: 'product',
        data: { pageTitle: 'Products' },
        loadChildren: () => import('./product/product.module').then(m => m.ProductModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
