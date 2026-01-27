import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideBooking } from './ride-booking';

describe('RideBooking', () => {
  let component: RideBooking;
  let fixture: ComponentFixture<RideBooking>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideBooking]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideBooking);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
