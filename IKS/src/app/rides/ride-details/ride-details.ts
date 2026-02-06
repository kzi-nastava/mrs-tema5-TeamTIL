import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import * as L from 'leaflet';

@Component({
  selector: 'app-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ride-details.html',
  styleUrl: './ride-details.css'
})
export class RideDetailsComponent implements OnInit, AfterViewInit {
  rideId: number | null = null;
  private map!: L.Map;
  
  // Mock data - kasnije će se povlačiti iz baze
  rideDetails = {
    id: 1,
    route: {
      start: 'Stražilovska',
      end: 'Bulevar Kralja Petra I',
      waypoints: []
    },
    driver: {
      name: 'Petar Petrovic',
      rating: 4.8,
      vehicle: 'Kia Sportage'
    },
    totalPrice: 1480,
    currency: 'RSD',
    status: 'Completed',
    startTime: new Date('2025-03-14T15:32:00'),
    endTime: new Date('2025-03-14T16:05:00'),
    duration: 33, // minutes
    distance: 8.2, // km
    passengers: [
      {
        name: 'John Doe',
        phone: '+381 123 456 789'
      }
    ],
    rating: 5,
    reportedIssues: [
      'Driver was very rude',
      'Unsafe driving'
    ]
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.rideId = +params['id'];
      // Ovde će se pozivati servis za učitavanje podataka iz baze
      // this.loadRideDetails(this.rideId);
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
    }, 100);
  }

  private initMap(): void {
    const mapElement = document.getElementById('rideDetailsMap');
    if (!mapElement) return;
    
    const noviSadLat = 45.265;
    const noviSadLng = 19.800;

    this.map = L.map('rideDetailsMap').setView([noviSadLat, noviSadLng], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
  }

  changeRating(): void {
    // Logika za promenu ocjene
    console.log('Change rating');
  }

  reportIssue(): void {
    // Logika za prijavljivanje problema
    console.log('Report issue');
  }

  rebookRide(): void {
    // Logika za ponovno rezervisanje vožnje
    console.log('Rebook ride');
  }

  scheduleForLater(): void {
    // Logika za zakazivanje kasnije
    console.log('Schedule for later');
  }

  closeRideInfo(): void {
    // Zatvaranje panela sa informacijama
    console.log('Close ride info');
  }
}
