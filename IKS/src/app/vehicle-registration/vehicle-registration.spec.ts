import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VehicleRegistrationComponent } from './vehicle-registration';

describe('VehicleRegistration', () => {
  let component: VehicleRegistrationComponent;
  let fixture: ComponentFixture<VehicleRegistrationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VehicleRegistrationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VehicleRegistrationComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
