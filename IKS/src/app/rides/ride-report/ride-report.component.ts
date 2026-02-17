import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { RideService } from '../../rides/services/ride.service';
import { RideStatsResponseDTO, RideStatsDayDTO } from '../../models/ride-stats.model';

@Component({
  selector: 'app-ride-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ride-report.component.html',
  styleUrls: ['./ride-report.component.css']
})
export class RideReportComponent implements OnInit {

  currentUser: any = null;
  isAdmin    = false;
  isDriver   = false;
  isPassenger = false;

  dateFrom = '';
  dateTo   = '';
  adminRole        = 'DRIVER';
  adminFilterEmail = '';

  stats: RideStatsResponseDTO | null = null;
  loading = false;
  error   = '';

  readonly svgWidth  = 900;
  readonly svgHeight = 180;
  readonly padLeft   = 10;
  readonly padRight  = 10;
  readonly padTop    = 16;
  readonly padBottom = 24;
  readonly gridCount = 4;

  get gridLines(): number[] {
    const lines: number[] = [];
    const h = this.svgHeight - this.padBottom - this.padTop;
    for (let i = 0; i <= this.gridCount; i++) {
      lines.push(this.padTop + (h / this.gridCount) * i);
    }
    return lines;
  }

  tooltip = { visible: false, x: 0, y: 0, date: '', value: '' };

  constructor(
    private authService: AuthService,
    private rideService: RideService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    //postavi datume
    const now = new Date();
    const from = new Date();
    from.setDate(now.getDate() - 30);
    this.dateFrom = this.toDateString(from);
    this.dateTo   = this.toDateString(now);

    //citaj korisnika sihrono iz localStorage
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      const user = JSON.parse(stored);
      this.currentUser = user;
      this.isAdmin     = user.userType === 'ADMINISTRATOR';
      this.isDriver    = user.userType === 'DRIVER';
      this.isPassenger = user.userType === 'REGISTERED_USER';
      //pokreni ucitavanje odmah
      this.loadStats();
    }

    //subscribe samo zbog logout-a
    this.authService.currentUser$.subscribe(user => {
      if (!user) {
        this.currentUser = null;
        return;
      }
      //ako se email promenio, reload
      if (user.email !== this.currentUser?.email) {
        this.currentUser = user;
        this.isAdmin     = user.userType === 'ADMINISTRATOR';
        this.isDriver    = user.userType === 'DRIVER';
        this.isPassenger = user.userType === 'REGISTERED_USER';
        this.loadStats();
      }
    });
  }

  setLastDays(n: number): void {
    const now = new Date();
    const from = new Date();
    from.setDate(now.getDate() - n);
    this.dateFrom = this.toDateString(from);
    this.dateTo   = this.toDateString(now);
    this.loadStats();
  }

  private toDateString(d: Date): string {
    return d.toISOString().split('T')[0];
  }

  private toDateTime(dateStr: string, end = false): string {
    if (!dateStr) return '';
    return end ? `${dateStr}T23:59:59` : `${dateStr}T00:00:00`;
  }

  loadStats(): void {
    if (!this.currentUser) return;

    this.loading = true;
    this.error   = '';
    this.stats   = null;
    this.cdr.detectChanges();

    const from = this.dateFrom ? this.toDateTime(this.dateFrom) : undefined;
    const to   = this.dateTo   ? this.toDateTime(this.dateTo, true) : undefined;

    let obs;
    if (this.isAdmin) {
      obs = this.rideService.getAdminStats(this.adminRole, this.adminFilterEmail, from, to);
    } else if (this.isDriver) {
      obs = this.rideService.getDriverStats(this.currentUser.email, from, to);
    } else {
      obs = this.rideService.getUserStats(this.currentUser.email, from, to);
    }

    obs.subscribe({
      next: (data) => {
        this.stats   = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error   = 'Failed to load statistics. Please try again.';
        this.loading = false;
        this.cdr.detectChanges();
        console.error('[RideReport] Error:', err);
      }
    });
  }

  private getMax(field: keyof RideStatsDayDTO): number {
    if (!this.stats || this.stats.days.length === 0) return 1;
    const max = Math.max(...this.stats.days.map(d => d[field] as number));
    return max === 0 ? 1 : max;
  }

  getXForIndex(i: number): number {
    if (!this.stats) return 0;
    const n = this.stats.days.length;
    if (n <= 1) return (this.svgWidth - this.padLeft - this.padRight) / 2 + this.padLeft;
    const step = (this.svgWidth - this.padLeft - this.padRight) / (n - 1);
    return this.padLeft + i * step;
  }

  private getY(value: number, max: number): number {
    const h = this.svgHeight - this.padBottom - this.padTop;
    return this.padTop + h - (value / max) * h;
  }

  getPoints(field: keyof RideStatsDayDTO): string {
    return this.getPointsArray(field).map(p => `${p.x},${p.y}`).join(' ');
  }

  getPointsArray(field: keyof RideStatsDayDTO): { x: number; y: number }[] {
    if (!this.stats) return [];
    const max = this.getMax(field);
    return this.stats.days.map((d, i) => ({
      x: this.getXForIndex(i),
      y: this.getY(d[field] as number, max)
    }));
  }

  getAreaPath(field: keyof RideStatsDayDTO): string {
    if (!this.stats || this.stats.days.length === 0) return '';
    const pts = this.getPointsArray(field);
    const bottom = this.svgHeight - this.padBottom;
    const first = pts[0];
    const last  = pts[pts.length - 1];
    const linePoints = pts.map(p => `${p.x},${p.y}`).join(' L ');
    return `M ${first.x},${bottom} L ${linePoints} L ${last.x},${bottom} Z`;
  }

  get xLabels(): string[] {
    if (!this.stats) return [];
    const days = this.stats.days;
    const total = days.length;
    const maxLabels = Math.min(10, total);
    const step = total <= maxLabels ? 1 : Math.floor(total / maxLabels);
    return days.map((d, i) => {
      if (i % step === 0 || i === total - 1) {
        const date = new Date(d.date);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
      }
      return '';
    });
  }

  showTooltip(event: MouseEvent, index: number, field: keyof RideStatsDayDTO): void {
    if (!this.stats) return;
    const day = this.stats.days[index];
    const svgEl = (event.target as SVGElement).closest('svg')!;
    const svgRect = svgEl.getBoundingClientRect();
    const containerRect = (event.target as SVGElement).closest('.chart-area')!.getBoundingClientRect();
    const pts = this.getPointsArray(field);
    const pt  = pts[index];
    const scaleX = svgRect.width  / this.svgWidth;
    const scaleY = svgRect.height / this.svgHeight;
    this.tooltip = {
      visible: true,
      x: pt.x * scaleX + (svgRect.left - containerRect.left),
      y: pt.y * scaleY + (svgRect.top  - containerRect.top),
      date: new Date(day.date).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' }),
      value: field === 'ridesCount'
        ? `${day.ridesCount} ride${day.ridesCount !== 1 ? 's' : ''}`
        : field === 'distanceKm'
          ? `${day.distanceKm.toFixed(1)} km`
          : `${Math.round(day.moneyAmount).toLocaleString()} RSD`
    };
  }

  hideTooltip(): void {
    this.tooltip.visible = false;
  }
}
