import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BillsComponent } from './bills.component';
import { BillListComponent } from './bill-list/bill-list.component';
import { BillFormComponent } from './bill-form/bill-form.component';

@NgModule({
  declarations: [
    BillsComponent,
    BillListComponent,
    BillFormComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      { path: '', component: BillListComponent },
      { path: 'new', component: BillFormComponent },
      { path: 'view/:id', component: BillListComponent }
    ])
  ]
})
export class BillsModule { }
