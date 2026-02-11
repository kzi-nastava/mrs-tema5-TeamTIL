import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportDriver } from './report-driver';

describe('ReportDriver', () => {
  let component: ReportDriver;
  let fixture: ComponentFixture<ReportDriver>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportDriver]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportDriver);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
