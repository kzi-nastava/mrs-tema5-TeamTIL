import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RateRideComponent } from './rate-ride';

describe('RateRide', () => {
  let component: RateRideComponent;
  let fixture: ComponentFixture<RateRideComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RateRideComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RateRideComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
