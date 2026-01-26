import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VehicleRegistration } from './vehicle-registration';

describe('VehicleRegistration', () => {
  let component: VehicleRegistration;
  let fixture: ComponentFixture<VehicleRegistration>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VehicleRegistration]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VehicleRegistration);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
