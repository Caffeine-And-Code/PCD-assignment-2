package pcd.reactiveprogramming;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public record FSReport(List<Integer> bandsCount) {
    public FSReport {
        bandsCount = List.copyOf(bandsCount);
    }

    public static FSReport empty(int numberOfBands) {
        return new FSReport(
                IntStream.rangeClosed(0, numberOfBands)
                        .map(index -> 0)
                        .boxed()
                        .toList()
        );
    }

    public int totalFiles() {
        return bandsCount.stream().mapToInt(Integer::intValue).sum();
    }

    public FSReport merge(FSReport other) {
        if (bandsCount.size() != other.bandsCount.size()) {
            throw new IllegalArgumentException("Both reports must have same size.");
        }

        return new FSReport(
                IntStream.range(0, bandsCount.size())
                        .map(index -> bandsCount.get(index) + other.bandsCount.get(index))
                        .boxed()
                        .toList()
        );
    }

    private List<Integer> increment(int index) {
        List<Integer> updated = new ArrayList<>(bandsCount);
        updated.set(index, updated.get(index) + 1);
        return updated;
    }

    private int getBandIndex(long size, long maxFileSize, int numberOfBands) {
        if (size < 0 || maxFileSize <= 0 || numberOfBands <= 0
                || numberOfBands > Integer.MAX_VALUE - 1) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        if (size > maxFileSize) {
            return numberOfBands;
        }

        if (size == 0) {
            return 0;
        }

        double bandWidth = (double) maxFileSize / numberOfBands;
        return (int) Math.ceil(size / bandWidth) - 1;
    }

    public FSReport addSize(long size, long maxFileSize, int numberOfBands) {
        int bandIndex = getBandIndex(size, maxFileSize, numberOfBands);
        return new FSReport(increment(bandIndex));
    }
}
