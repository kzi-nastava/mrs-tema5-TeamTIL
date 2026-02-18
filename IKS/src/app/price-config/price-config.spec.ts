import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PriceConfig } from './price-config';

describe('PriceConfig', () => {
  let component: PriceConfig;
  let fixture: ComponentFixture<PriceConfig>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PriceConfig]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PriceConfig);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
