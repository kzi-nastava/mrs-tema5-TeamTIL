import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminRideHistory } from './admin-ride-history';

describe('AdminRideHistory', () => {
  let component: AdminRideHistory;
  let fixture: ComponentFixture<AdminRideHistory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminRideHistory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminRideHistory);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
