package com.dronemission.missions.strategy;

/** Coordenada geográfica simple (grados decimales). */
public record GeoPoint(double latitude, double longitude) {

    private static final double EARTH_RADIUS_M = 6_371_000;

    /** Distancia en metros entre dos puntos (fórmula de Haversine). */
    public double distanceTo(GeoPoint other) {
        double dLat = Math.toRadians(other.latitude - latitude);
        double dLon = Math.toRadians(other.longitude - longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_M * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Desplaza el punto "east" metros al este y "north" metros al norte. */
    public GeoPoint offset(double eastMeters, double northMeters) {
        double dLat = northMeters / EARTH_RADIUS_M;
        double dLon = eastMeters / (EARTH_RADIUS_M * Math.cos(Math.toRadians(latitude)));
        return new GeoPoint(latitude + Math.toDegrees(dLat), longitude + Math.toDegrees(dLon));
    }

    /** Punto intermedio entre este y "target" (fraction entre 0 y 1). */
    public GeoPoint interpolate(GeoPoint target, double fraction) {
        return new GeoPoint(latitude + (target.latitude - latitude) * fraction,
                longitude + (target.longitude - longitude) * fraction);
    }
}
