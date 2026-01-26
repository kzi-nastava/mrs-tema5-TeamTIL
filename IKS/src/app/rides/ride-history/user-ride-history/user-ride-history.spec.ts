import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserRideHistory } from './user-ride-history';

describe('UserRideHistory', () => {
  let component: UserRideHistory;
  let fixture: ComponentFixture<UserRideHistory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserRideHistory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserRideHistory);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
