import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BundleListPageRoutingModule } from './bundle-list-routing.module';

import { BundleListPage } from './bundle-list.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BundleListPageRoutingModule
  ],
  declarations: [BundleListPage]
})
export class BundleListPageModule {}
