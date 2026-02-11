import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RideDetailsComponent } from './ride-details';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('RideDetailsComponent', () => {
  let component: RideDetailsComponent;
  let fixture: ComponentFixture<RideDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideDetailsComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ id: '1' })
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load ride details on init', () => {
    expect(component.rideId).toBe(1);
  });
});
