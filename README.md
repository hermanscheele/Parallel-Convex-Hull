
# Project Description:

The project involved implementing both sequential and parallel versions of a convex hull algorithm using Java. The convex hull problem entails finding the smallest convex boundary that encloses a set of points in a 2D plane.

# Sequential Algorithm: This version followed a standard convex hull algorithm where points are processed one after another to identify those on the convex boundary.

# Parallel Algorithm: The parallel implementation utilized Java threads to split the point set into either two or four subsets, depending on the number of threads. Each thread worked concurrently to find points on the convex hull within its designated subset. A semaphore was used for synchronization, ensuring that multiple threads could safely add points to the convex hull.

# Performance Results:

The performance was measured using various input sizes (n) ranging from 100 to 10 million points.
Speedups were observed for larger datasets, with a maximum speedup of approximately 2x when using 2 or 4 threads, particularly for n > 1,000,000.
Handling the correct order of convex hull points after all threads finished was done sequentially, which contributed to bottlenecks, especially with larger data sets.
Conclusion: The parallel implementation showed significant performance improvement for larger datasets but faced diminishing returns due to sequential handling of the final convex hull ordering. Nevertheless, the achieved speedups indicated the effectiveness of parallelization for large-scale problems.
