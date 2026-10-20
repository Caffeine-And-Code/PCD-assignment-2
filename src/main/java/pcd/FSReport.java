package pcd;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public record FSReport(List<Integer> bandsCount, int fileCount)
{

    public static FSReport Empty(int numberOfBands){
        return new FSReport(IntStream.range(0, numberOfBands+1).map(i -> 0).boxed().toList(), 0);
    }

    public FSReport merge(FSReport other){
        if(this.bandsCount.size() != other.bandsCount.size()){
            throw new IllegalArgumentException("must be same size");
        }

        return new FSReport(IntStream.range(0, this.bandsCount.size())
                .map(i -> this.bandsCount.get(i) + other.bandsCount.get(i)).boxed().toList(), this.fileCount);
    }

    private List<Integer> increment(int i){
        List<Integer> updated = new ArrayList<>(this.bandsCount);
        updated.set(i, updated.get(i)+1);
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

    public FSReport addFile(long size, long maxFileSize, int numberOfBands){
        return new FSReport(
                increment(getBandIndex(size, maxFileSize, numberOfBands)), this.fileCount+1
        );
    }
}
