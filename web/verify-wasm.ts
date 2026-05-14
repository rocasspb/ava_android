import { WasmLoader } from './src/services/WasmLoader';

async function verify() {
    console.log('Verifying Wasm module...');
    try {
        const wasm = await WasmLoader.getInstance();
        console.log('Wasm exports keys:', Object.keys(wasm));
        
        // In Kotlin/Wasm, the exports are often on the default export or specific exports
        // If we use binaries.library(), it should export the @JsExport functions.
        
        if (wasm.isPointInPolygonWasm) {
            console.log('SUCCESS: isPointInPolygonWasm is exported');
            const rings = [[ [0, 0], [0, 10], [10, 10], [10, 0], [0, 0] ]];
            const result = wasm.isPointInPolygonWasm(5, 5, JSON.stringify(rings));
            console.log('Call isPointInPolygonWasm(5, 5, ...):', result);
        } else {
            console.log('FAILURE: isPointInPolygonWasm is NOT exported');
            // Check nested exports if any
            if (wasm.exports) {
                 console.log('Wasm.exports keys:', Object.keys(wasm.exports));
            }
        }
    } catch (e) {
        console.error('Verification failed:', e);
    }
}

verify();
