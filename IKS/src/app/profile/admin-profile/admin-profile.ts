import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-profile.html',
  styleUrls: ['./admin-profile.css']
})
export class AdminProfileComponent {
  // pratimo koji je tab aktivan
  activeTab: string = 'profile';

  setActiveTab(tabName: string) {
    this.activeTab = tabName;
  }
}