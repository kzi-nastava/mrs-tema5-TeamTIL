import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrackRide } from './track-ride';

describe('TrackRide', () => {
  let component: TrackRide;
  let fixture: ComponentFixture<TrackRide>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrackRide]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrackRide);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
