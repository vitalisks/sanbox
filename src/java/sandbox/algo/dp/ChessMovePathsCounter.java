
import java.io.*;

public class ChessMovePathsCounter {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        Solver solver = readData(reader);
        writer.write(String.valueOf(solver.solve()));
        reader.close();
        writer.close();
    }

    static Solver readData(BufferedReader reader) throws IOException {
        String[] sizes = reader.readLine().split(" ");
        int height = Integer.parseInt(sizes[0]);
        int width = Integer.parseInt(sizes[1]);
        int[][] matrix = new int[height][width];
        return new Solver(matrix);
    }

    static class Solver {
        private final int[][] moveMatrix;
        private final int width;
        private final int height;
        private final Result[][] maxes;

        Solver(int[][] moveMatrix) {
            this.moveMatrix = moveMatrix;
            this.height = moveMatrix.length;
            this.width = moveMatrix[0].length;
            this.maxes = new Result[height][width];
        }

        int solve() {
            return solveCost(moveMatrix[0][0], 0, 0).routes;
        }

        Result solveCost(int parent, int x, int y) {
            if (y == height - 1 && x == width - 1) {
                return new Result(1);
            } else if (x > width - 1 || y > height - 1) {
                return new Result(0);
            } else if (maxes[y][x] != null) {
                return new Result(maxes[y][x].routes);
            }
            Result xMove = solveCost(parent + moveMatrix[y][x], x + 1, y + 2);
            Result yMove = solveCost(parent + moveMatrix[y][x], x + 2, y + 1);

            return (maxes[y][x] = new Result(xMove.routes + yMove.routes));
        }

    }

    static class Result {
        final int routes;

        Result(int routes) {
            this.routes = routes;
        }
    }
}