# Test262Converter
Test262Convert converts ECMAScript Test262 tests into a a standard unit testing format that uses result and expect variables. Result variables are declared in the format of var __result1 =
assert.sameValue(a, b, 'message'). The number in the result variable's name such as 1 for __result1 or 2 for __result2 is determined by the number of assertion tests that was above
it earlier in the file.


Test262Converter will convert every JavaScript file (files with the .js filetype) in the directory from which you launch it. If a file does use assert methods such as assert.sameValue(a, b),
assert.notSameValue(a, b), assert.throws(error, func), or assert(condition), then the file will be overwritten but with the exact same contents that it started with.
