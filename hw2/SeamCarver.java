import java.awt.Color;

public class SeamCarver {
    private Picture picture;
    private double[] energy;
    private double[] distTo;
    private int[] edgeTo;

    public SeamCarver(Picture picture) {
        this.picture = new Picture(picture);
    }

    public Picture picture() {
        return new Picture(this.picture);
    }

    public int width() {
        return picture.width();
    }

    public int height() {
        return picture.height();
    }

    // energy of pixel at COLUMN x and ROW y
    public double energy(int x, int y) {
        if (x < 0 || y < 0 || x >= width() || y >= height()) {
            throw new java.lang.IndexOutOfBoundsException();
        }

        if (x == 0 || x == width() - 1 || y == 0 || y == height() - 1) {
            return 255 * 255 + 255 * 255 + 255 * 255;
        }

        return squareGradient(picture.get(x-1, y), picture.get(x+1, y))
             + squareGradient(picture.get(x, y-1), picture.get(x, y+1));
    }

    private int squareGradient(Color one, Color two) {
        int r = Math.abs(one.getRed() - two.getRed());
        int g = Math.abs(one.getGreen() - two.getGreen());
        int b = Math.abs(one.getBlue() - two.getBlue());

        return r * r + g * g + b * b;
    }

    public int[] findHorizontalSeam() {
        int size = width() * height();

        energy = new double[size];
        distTo = new double[size];
        edgeTo = new int[size];
        int p;

        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                p = position(col, row);

                if (col == 0) {
                    distTo[p] = 0;
                } else {
                    distTo[p] = Double.POSITIVE_INFINITY;
                }

                energy[p] = energy(col, row);
                edgeTo[p] = -1;
            }
        }

        for (int col = 0; col < width() - 1; col++) {
            for (int row = 0; row < height(); row++) {
                p = position(col, row);

                if (row - 1 >= 0) {
                    relax(p, position(col+1, row-1));
                }

                relax(p, position(col+1, row));

                if (row + 1 < height()) {
                    relax(p, position(col+1, row+1));
                }
            }
        }


        double min = Double.POSITIVE_INFINITY;
        int end = 0;

        for (int row = 0; row < height(); row++) {
            if (distTo[position(width() - 1, row)] < min) {
                min = distTo[position(width() - 1, row)];
                end = position(width() - 1, row);
            }
        }
        
        return horizontalSeam(end);
    }

    public int[] findVerticalSeam() {
        int size = width() * height();

        energy = new double[size];
        distTo = new double[size];
        edgeTo = new int[size];
        int p;

        for (int col = 0; col < width(); col++) {
            for (int row = 0; row < height(); row++) {
                p = position(col, row);

                if (row == 0) {
                    distTo[p] = 0;
                } else {
                    distTo[p] = Double.POSITIVE_INFINITY;
                }

                energy[p] = energy(col, row);
                edgeTo[p] = -1;
            }
        }

        for (int row = 0; row < height() - 1; row++) {
            for (int col = 0; col < width(); col++) {
                p = position(col, row);

                if (col - 1 >= 0) {
                    relax(p, position(col-1, row+1));
                }

                relax(p, position(col, row+1));

                if (col + 1 < width()) {
                    relax(p, position(col+1, row+1));
                }
            }
        }

        double min = Double.POSITIVE_INFINITY;
        int end = 0;

        for (int col = 0; col < width(); col++) {
            if (distTo[position(col, height()-1)] < min) {
                min = distTo[position(col, height()-1)];
                end = position(col, height() - 1);
            }
        }
        
        return verticalSeam(end);
    }

    private int position(int col, int row) {
        return width() * row + col;
    }
    
    private int positionRow(int position) {
        return position / width();
    }

    private int positionColumn(int position) {
        return position % width();
    }

    private void relax(int from, int to) {
        if (distTo[to] > distTo[from] + energy[to]) {
            distTo[to] = distTo[from] + energy[to];
            edgeTo[to] = from;
        }
    }

    private int[] horizontalSeam(int end) {
        int[] result = new int[width()];
        int tmp = end;

        while (tmp >= 0) {
            result[positionColumn(tmp)] = positionRow(tmp);
            tmp = edgeTo[tmp];
        }

        return result;
    }

    private int[] verticalSeam(int end) {
        int[] result = new int[height()];
        int tmp = end;

        while (tmp >= 0) {
            result[positionRow(tmp)] = positionColumn(tmp);
            tmp = edgeTo[tmp];
        }

        return result;
    }

    public void removeHorizontalSeam(int[] a) {
        if (height() <= 1) {
            throw new java.lang.IllegalArgumentException();
        }

        if (a.length != width()) {
            throw new java.lang.IllegalArgumentException();
        }

        Picture result = new Picture(width(), height() - 1);
        int prev = a[0];

        for (int col = 0; col < width(); col++) {
            if (a[col] < 0 || a[col] >= height()) {
                throw new java.lang.IndexOutOfBoundsException();
            }

            if (a[col] < prev - 1 || a[col] > prev + 1) {
                throw new java.lang.IllegalArgumentException();
            }

            prev = a[col];

            for (int row = 0; row < height() - 1; row++) {
                if (row < prev) {
                    result.set(col, row, picture.get(col, row));
                } else {
                    result.set(col, row, picture.get(col, row+1));
                }
            }
        }

        picture = result;

        distTo = null;
        edgeTo = null;
        energy = null;
    }

    public void removeVerticalSeam(int[] a) {
        if (width() <= 1) {
            throw new java.lang.IllegalArgumentException();
        }

        if (a.length != height()) {
            throw new java.lang.IllegalArgumentException();
        }

        Picture result = new Picture(width() - 1, height());
        int prev = a[0];

        for (int row = 0; row < height(); row++) {
            if (a[row] < 0 || a[row] >= width()) {
                throw new java.lang.IndexOutOfBoundsException();
            }

            if (a[row] < prev - 1 || a[row] > prev + 1) {
                throw new java.lang.IllegalArgumentException();
            }

            prev = a[row];

            for (int col = 0; col < width() - 1; col++) {
                if (col < prev) {
                    result.set(col, row, picture.get(col, row));
                } else {
                    result.set(col, row, picture.get(col+1, row));
                }
            }
        }

        picture = result;

        distTo = null;
        edgeTo = null;
        energy = null;
    }
}
