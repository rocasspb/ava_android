import { WasmLoader } from '../services/WasmLoader';

export interface Point {
    x: number;
    y: number;
}

/**
 * Checks if a point is inside a polygon using KMP Wasm logic.
 */
export async function isPointInPolygon(point: [number, number], polygon: number[][][]): Promise<boolean> {
    const wasm = await WasmLoader.getInstance();
    if (wasm.isPointInPolygonWasm) {
        return wasm.isPointInPolygonWasm(point[0], point[1], JSON.stringify(polygon));
    }
    
    // Fallback to TS implementation if Wasm not loaded (should not happen if awaited)
    return isPointInPolygonTS(point, polygon);
}

/**
 * Checks if a point is inside a MultiPolygon using KMP Wasm logic.
 */
export async function isPointInMultiPolygon(point: [number, number], multiPolygon: number[][][][]): Promise<boolean> {
    // KMP Wasm current bridge only has isPointInPolygonWasm which takes rings (List<List<List<Double>>>)
    // So we iterate through the polygons in the MultiPolygon.
    for (const polygon of multiPolygon) {
        if (await isPointInPolygon(point, polygon)) {
            return true;
        }
    }
    return false;
}

/**
 * TS Implementation as fallback.
 */
function isPointInPolygonTS(point: [number, number], polygon: number[][][]): boolean {
    const x = point[0];
    const y = point[1];
    let inside = false;

    for (const ring of polygon) {
        for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
            const xi = ring[i][0], yi = ring[i][1];
            const xj = ring[j][0], yj = ring[j][1];

            const intersect = ((yi > y) !== (yj > y))
                && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);

            if (intersect) inside = !inside;
        }
    }

    return inside;
}

export function getBounds(feature: any) {
    let minLng = 180, maxLng = -180, minLat = 90, maxLat = -90;

    const processRing = (ring: number[][]) => {
        ring.forEach(coord => {
            const [lng, lat] = coord;
            if (lng < minLng) minLng = lng;
            if (lng > maxLng) maxLng = lng;
            if (lat < minLat) minLat = lat;
            if (lat > maxLat) maxLat = lat;
        });
    };

    if (feature.geometry.type === 'Polygon') {
        processRing(feature.geometry.coordinates[0]);
    } else if (feature.geometry.type === 'MultiPolygon') {
        feature.geometry.coordinates.forEach((poly: any) => processRing(poly[0]));
    }

    return { minLng, maxLng, minLat, maxLat };
}
