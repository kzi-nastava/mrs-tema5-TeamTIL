import { Component, ViewChild, ElementRef, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { InconsistencyReportService } from '../../services/inconcistency-report.service';
import { InconsistencyReportRequestDTO } from '../../../models/ride-dto.model';

@Component({
  selector: 'app-report-driver',
  imports: [
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule
  ],
  templateUrl: './report-driver.html',
  styleUrl: './report-driver.css',
})
export class ReportDriver {
  description: string = '';
  attachmentBase64?: string;
  rideId: number = 1;
  passengerEmail: string = '';
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { rideId: number, passengerEmail: string },
    private dialogRef: MatDialogRef<ReportDriver>,
    private reportService: InconsistencyReportService
  ) {}

  onUploadClick() {
    this.fileInput.nativeElement.click();
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        this.attachmentBase64 = reader.result?.toString().split(',')[1]; 
      };
      reader.readAsDataURL(file);
    }
  }

  submitReport() {
    const report: InconsistencyReportRequestDTO = {
      passengerEmail: this.data.passengerEmail,
      description: this.description,
      attachmentBase64: this.attachmentBase64
    };

    this.reportService.submitReport(this.data.rideId, report).subscribe({
      next: res => this.dialogRef.close(res),
      error: err => console.error(err)
    });
  }
}
