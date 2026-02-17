import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RateRideComponent } from '../rate-ride/rate-ride';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { RideService } from '../../services/ride.service';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../services/auth.service';

describe('RateRideComponent - Functionality 2.8: Rating vehicle and driver', () => {

  let component: RateRideComponent;
  let fixture: ComponentFixture<RateRideComponent>;
  let rideServiceSpy: jasmine.SpyObj<RideService>;
  let dialogRefSpy: jasmine.SpyObj<MatDialogRef<RateRideComponent>>;
  let snackBar: MatSnackBar;
  let authServiceSpy: any;
  let mockRide: any;

  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

  function formatDate(date: Date) {
    return {
      date: `${date.getDate()} ${months[date.getMonth()]} ${date.getFullYear()}`,
      endTime: `${date.getHours()}:${date.getMinutes()}`
    };
  }

  beforeEach(async () => {
    rideServiceSpy = jasmine.createSpyObj('RideService', ['rateRide']);
    dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    
    const now = new Date();
    mockRide = {
      id: 123,
      ...formatDate(now)
    };

    authServiceSpy = {
      currentUser$: of({ email: 'test@test.com' })
    };

    await TestBed.configureTestingModule({
      imports: [
        RateRideComponent,
        MatDialogModule,
        MatSnackBarModule
      ],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: mockRide },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: RideService, useValue: rideServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RateRideComponent);
    component = fixture.componentInstance;
    snackBar = TestBed.inject(MatSnackBar);
    fixture.detectChanges();
  });

  // ==================== GROUP 1: BASIC TEST ====================
  describe('Initialization', () => {
    it('TEST 1: Should create component', () => {
      expect(component).toBeTruthy();
    });
  });

  // ==================== GROUP 2: RATING SETTERS ====================
  describe('Rating setters', () => {
    it('TEST 2: Should set driver rating correctly', () => {
      component.setDriverRating(4);
      expect(component.driverRating).toBe(4);
    });

    it('TEST 3: Should set vehicle rating correctly', () => {
      component.setVehicleRating(5);
      expect(component.vehicleRating).toBe(5);
    });
  });

  // ==================== GROUP 3: VALIDATION ====================
  describe('Validation', () => {
    it('TEST 4: Should prevent submission if ratings are zero', () => {
      spyOn(snackBar, 'open');

      component.driverRating = 0;
      component.vehicleRating = 0;
      component.submit();

      expect(snackBar.open).toHaveBeenCalledWith(
        'Please rate both driver and vehicle before submitting!',
        'Close',
        { duration: 3000 }
      );
      expect(rideServiceSpy.rateRide).not.toHaveBeenCalled();
    });

    it('TEST 5: Should prevent submission if deadline expired', () => {
      spyOn(snackBar, 'open');

      const oldDate = new Date(Date.now() - 5 * 24 * 60 * 60 * 1000);
      component.ride = { id: 123, ...formatDate(oldDate) };

      component.driverRating = 5;
      component.vehicleRating = 5;
      component.submit();

      expect(snackBar.open).toHaveBeenCalledWith(
        'Rating deadline has expired!',
        'Close',
        { duration: 3000 }
      );
      expect(rideServiceSpy.rateRide).not.toHaveBeenCalled();
    });
  });

  // ==================== GROUP 4: SUCCESSFUL SUBMISSION ====================
  describe('Submit success', () => {
    it('TEST 6: Should call rateRide and close dialog on success', () => {
      rideServiceSpy.rateRide.and.returnValue(of({ message: 'Success' }));

      component.driverRating = 5;
      component.vehicleRating = 4;
      component.comment = 'Great ride';

      component.submit();

      expect(rideServiceSpy.rateRide).toHaveBeenCalledWith(123, {
        userEmail: 'test@test.com',
        driverRating: 5,
        vehicleRating: 4,
        comment: 'Great ride'
      });

      expect(dialogRefSpy.close).toHaveBeenCalledWith({ message: 'Success' });
    });
    
    it('TEST 7: Should allow empty comment', () => {
      rideServiceSpy.rateRide.and.returnValue(of({}));

      component.driverRating = 5;
      component.vehicleRating = 5;
      component.comment = '';

      component.submit();

      expect(rideServiceSpy.rateRide).toHaveBeenCalledWith(123, {
        userEmail: 'test@test.com',
        driverRating: 5,
        vehicleRating: 5,
        comment: ''
      });
    });
  });

  // ==================== GROUP 5: ERROR HANDLING ====================
  describe('Submit error', () => {
    it('TEST 8: Should show snackBar on backend error', () => {
      rideServiceSpy.rateRide.and.returnValue(
        throwError(() => new Error('Server error'))
      );
      spyOn(snackBar, 'open');

      component.driverRating = 5;
      component.vehicleRating = 5;
      component.submit();

      expect(snackBar.open).toHaveBeenCalledWith(
        'Failed to submit rating: Server error',
        'Close',
        { duration: 3000 }
      );

      expect(dialogRefSpy.close).not.toHaveBeenCalled();
    });
  });

  // ==================== GROUP 6: INVALID USER ====================
  describe('Invalid user handling', () => {
    it('TEST 9: Should NOT submit if userEmail is missing', () => {

      authServiceSpy.currentUser$ = of(null);

      component.driverRating = 5;
      component.vehicleRating = 5;

      component.submit();

      expect(rideServiceSpy.rateRide).not.toHaveBeenCalled();
      expect(dialogRefSpy.close).not.toHaveBeenCalled();
    });
  });

  // ==================== GROUP 7: CLOSE DIALOG ====================
  describe('Dialog behavior', () => {
    it('TEST 10: Should close dialog manually', () => {
      component.close();
      expect(dialogRefSpy.close).toHaveBeenCalled();
    });
  });

  // ==================== GROUP 8: isWithinDeadline ====================
  describe('Deadline logic (isWithinDeadline)', () => {
    beforeEach(() => {
      jasmine.clock().install();
    });

    afterEach(() => {
      jasmine.clock().uninstall();
    });

    it('TEST 11: Should return true if ride ended less than 3 days ago', () => {
      const fixedNow = new Date(2026, 1, 16, 12, 0);
      jasmine.clock().mockDate(fixedNow);

      component.ride = {
        id: 123,
        ...formatDate(fixedNow)
      };

      expect(component.isWithinDeadline()).toBeTrue();
    });

    it('TEST 12: Should return false if ride ended more than 3 days ago', () => {
      const oldDate = new Date(Date.now() - 5 * 24 * 60 * 60 * 1000);
      component.ride = { id: 123, ...formatDate(oldDate) };
      expect(component.isWithinDeadline()).toBeFalse();
    });

    it('TEST 13: Should allow rating exactly at 3-day boundary', () => {
      const fixedNow = new Date(2026, 1, 16, 12, 0); // 16 Feb 2026 12:00
      jasmine.clock().mockDate(fixedNow);

      const boundaryDate = new Date(fixedNow.getTime() - 3 * 24 * 60 * 60 * 1000);
      
      component.ride = {
        id: 123,
        ...formatDate(boundaryDate)
      };

      expect(component.isWithinDeadline()).toBeTrue();
    });
  });
});