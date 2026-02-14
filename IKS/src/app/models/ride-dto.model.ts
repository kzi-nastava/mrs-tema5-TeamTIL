export interface RideRequestDTO {
  locations: string[];
  passengerEmails: string[];
  vehicleType: string;
  babyFriendly: boolean;
  petFriendly: boolean;
  scheduledTime: string | null;
}

export interface RideCreatedResponseDTO {
  rideId: number;
  status: string;
  estimatedPrice: number;
  driverName: string;
  driverEmail: string;
  vehicleInfo: string;
  message: string;
  startTime: string;
  estimatedEndTime: string;
  distanceKm: number;
  durationMin: number;
}

export interface RideHistoryDTO {
  id: number;
  passengerEmail: string;
  driverEmail: string;
  startLocation: string;
  endLocation: string;
  status: string;
  price: number;
  createdAt: string;
}

export interface LocationResponseDTO {
  name: string;
  latitude: string;
  longitude: string;
}

export interface LinkedPassengerDTO {
  firstName: string;
  lastName: string;
  email: string;
}

export interface RideDetailsResponseDTO {
  id: number;
  passengerFirstName: string;
  passengerLastName: string;
  passengerProfilePictureUrl: string | null;
  passengerPhoneNumber: string;
  driverFirstName: string;
  driverLastName: string;
  driverProfilePictureUrl: string | null;
  driverPhoneNumber: string;
  driverRating: number | null;
  route: LocationResponseDTO[];
  linkedPassengers: LinkedPassengerDTO[];
  status: string;
  startTime: string;
  estimatedEndTime: string;
  price: number;
  distance: number;
  duration: number;
  rideRating: number | null;
  rideComment: string | null;
  panicSent: boolean;
  reportedIssues: string[];
}

export interface InconsistencyReportRequestDTO {
  passengerEmail: string;
  description: string;
  attachmentBase64?: string;
}

export interface InconsistencyReportResponseDTO {
  id: number;
  message: string;
}