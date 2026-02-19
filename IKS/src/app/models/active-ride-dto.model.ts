export interface ActiveRideAdminDTO {
  rideId: number;
  driverFirstName: string;
  driverLastName: string;
  driverEmail: string;
  driverPhone: string;
  driverProfilePicture: string | null;
  driverRating: number;
  vehicleModel: string;
  vehicleType: string;
  licensePlate: string;
  passengerFirstName: string;
  passengerLastName: string;
  passengerPhone: string;
  passengerProfilePicture: string | null;
  startAddress: string;
  endAddress: string;
  rideStatus: string;
  startTime: string;
  estimatedEndTime: string | null;
  price: number;
  distanceKm: number;
  vehicleLat: number | null;
  vehicleLon: number | null;
}