import groovy.transform.TailRecursive

class MathWizard {

    @TailRecursive
    static long sumLongRange(long n, long accumulator = 0) {
        if (n == 0) return accumulator
        return sumLongRange(n - 1, accumulator + n)
    }
}