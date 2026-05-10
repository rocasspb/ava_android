
import { instantiate } from './AvaAwaAnd-shared-test.uninstantiated.mjs';
import "./custom-formatters.js"

const exports = (await instantiate({
})).exports;

export const {
memory,
_initialize,
startUnitTests
} = exports


