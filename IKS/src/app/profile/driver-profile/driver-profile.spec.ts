import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DriverProfileComponent } from './driver-profile';

describe('DriverProfileComponent', () => {
  let component: DriverProfileComponent;
  let fixture: ComponentFixture<DriverProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverProfileComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverProfileComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});