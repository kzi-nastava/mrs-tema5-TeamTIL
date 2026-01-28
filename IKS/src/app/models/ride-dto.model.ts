export interface RideRequestDTO {
  locations: string[];
  passengerEmails: string[];
  vehicleType: string;
  babyFriendly: boolean;
  petFriendly: boolean;
  scheduledTime: string | null;
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