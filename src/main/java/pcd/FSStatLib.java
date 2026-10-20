package pcd;

public interface FSStatLib {
    FSReport getFSReport(String directory, long maxFileSize, int numberOfBands);
}
