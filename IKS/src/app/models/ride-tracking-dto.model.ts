export interface RideTrackingDTO {
    startAddress?: string;
    startLatitude?: number;
    startLongitude?: number;
    endAddress?: string;
    endLatitude?: number;
    endLongitude?: number;
    startTime?: string;
    driverName?: string;
    driverPhone?: string;
    vehicleType?: string;
}