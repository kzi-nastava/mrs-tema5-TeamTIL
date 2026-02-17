export interface RideStatsDayDTO {
  date: string;
  ridesCount: number;
  distanceKm: number;
  moneyAmount: number;
}

export interface RideStatsResponseDTO {
  days: RideStatsDayDTO[];
  totalRides: number;
  totalDistanceKm: number;
  totalMoney: number;
  avgRidesPerDay: number;
  avgDistancePerDay: number;
  avgMoneyPerDay: number;
}
