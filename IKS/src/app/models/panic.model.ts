// Backend API Request/Response (matches PanicRequestDTO and PanicResponseDTO)
export interface BackendPanicRequest {
  rideId: number;
  locationId: number;
  userType: 'DRIVER' | 'REGISTERED_USER';
  accountEmail: string;
  latitude?: number;
  longitude?: number;
}

export interface BackendPanicResponse {
  panicId: number;
  rideId: number;
  locationId: number;
  userType: 'DRIVER' | 'REGISTERED_USER';
  accountEmail: string;
  timestamp: string;
  handled?: boolean;
}

// Legacy interfaces (for internal use/notifications)
export interface PanicAlert {
  panicId: string;
  rideId: string;
  reportedBy: 'DRIVER' | 'REGISTERED_USER';
  location: string;
  latitude: number;
  longitude: number;
  timestamp: string;
  vehicleName?: string;
  vehicleLicensePlate?: string;
}

export interface PanicAlertRequest {
  rideId: string;
  reportedBy: 'DRIVER' | 'REGISTERED_USER';
  location: string;
  latitude: number;
  longitude: number;
  vehicleName?: string;
  vehicleLicensePlate?: string;
}

// Updated to match new backend response format
export interface PanicAlertResponse {
  panicId: number | string;  // Backend can return either
  id?: number;               // Also support 'id' field from backend
  rideId: number;
  reportedBy: 'DRIVER' | 'REGISTERED_USER';
  locationAddress: string;
  latitude: number;
  longitude: number;
  timestamp: string;
  handled: boolean;
  vehicleName?: string;
  vehicleLicensePlate?: string;
  // Original fields for compatibility
  location?: string;
  locationId?: number;
  registeredUserId?: number;
  driverId?: number;
}
