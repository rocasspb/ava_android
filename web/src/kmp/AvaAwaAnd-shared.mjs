
import { instantiate } from './AvaAwaAnd-shared.uninstantiated.mjs';


const exports = (await instantiate({
})).exports;

export const {
isPointInPolygonWasm,
calculateTerrainMetricsWasm,
generateRasterWasm,
memory,
_initialize
} = exports


