import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PanicAlert } from '../../services/notification.service';

@Component({
  selector: 'app-panic-alert-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './panic-alert-modal.html',
  styleUrl: './panic-alert-modal.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PanicAlertModalComponent {
  @Input() alert: PanicAlert | null = null;
  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();
  @Output() markHandled = new EventEmitter<string>();

  onClose() {
    this.close.emit();
  }

  onMarkHandled() {
    if (this.alert) {
      this.markHandled.emit(this.alert.panicId);
      this.onClose();
    }
  }
}
