import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveRides } from './active-rides';

describe('ActiveRides', () => {
  let component: ActiveRides;
  let fixture: ComponentFixture<ActiveRides>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveRides]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiveRides);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
