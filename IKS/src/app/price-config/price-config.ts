import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PriceConfigService } from '../services/price-config.service';
import { PriceConfigDTO } from '../models/price-config-dto.model';
import { AuthService } from '../services/auth.service';
import { take } from 'rxjs/operators';

type VehicleType = 'STANDARD' | 'LUXURY' | 'VAN';

@Component({
  selector: 'app-price-config',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './price-config.html',
  styleUrls: ['./price-config.css'],
})
export class PriceConfig implements OnInit {
  tabs: { label: string; value: VehicleType }[] = [
    { label: 'Standard', value: 'STANDARD' },
    { label: 'Luxury',   value: 'LUXURY'   },
    { label: 'Van',      value: 'VAN'       },
  ];

  activeTab: VehicleType = 'STANDARD';

  configs: Record<VehicleType, PriceConfigDTO> = {
    STANDARD: { vehicleType: 'STANDARD', basePrice: 0, pricePerKm: 0 },
    LUXURY:   { vehicleType: 'LUXURY',   basePrice: 0, pricePerKm: 0 },
    VAN:      { vehicleType: 'VAN',      basePrice: 0, pricePerKm: 0 },
  };

  successMessage = '';
  errorMessage   = '';
  isLoading      = false;
  isSaving       = false;

  constructor(
    private priceConfigService: PriceConfigService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAllConfigs();
  }

  loadAllConfigs(): void {
    this.isLoading = true;
    const types: VehicleType[] = ['STANDARD', 'LUXURY', 'VAN'];
    let loaded = 0;

    types.forEach(type => {
      this.priceConfigService.getPriceConfig(type).subscribe({
        next: (data) => {
          this.configs[type] = data;
          loaded++;
          if (loaded === types.length) {
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        },
        error: (err) => {
          console.error(`Failed to load config for ${type}:`, err);
          loaded++;
          if (loaded === types.length) {
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        }
      });
    });
  }

  get current(): PriceConfigDTO {
    return this.configs[this.activeTab];
  }

  setTab(tab: VehicleType): void {
    this.activeTab = tab;
    this.clearMessages();
  }

  saveChanges(): void {
    this.clearMessages();
    this.isSaving = true;

    this.priceConfigService.updatePriceConfig(this.activeTab, this.current).subscribe({
      next: (updated) => {
        this.configs[this.activeTab] = updated;
        this.successMessage = 'Pricing updated successfully!';
        this.isSaving = false;
        this.cdr.detectChanges();
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => {
        console.error('Failed to save config:', err);
        this.errorMessage = 'Failed to save changes. Please try again.';
        this.isSaving = false;
        this.cdr.detectChanges();
      }
    });
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage   = '';
  }
}