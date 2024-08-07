
import java.io.*;

public class CavernPathFinder {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        writer.write(String.valueOf(Solver.start(reader).solve()));

        reader.close();
        writer.close();
    }

    static class Solver {
        private final int startVertex;
        private final int[][] map;
        private final boolean[] exits;

        Solver(int startVertex, int[][] map, boolean[] exits) {
            this.startVertex = startVertex;
            this.map = map;
            this.exits = exits;
        }

        static Solver start(BufferedReader reader) throws IOException {
            int layerCount = Integer.parseInt(reader.readLine());
            int levelArea = layerCount * layerCount;

            int[][] graph = new int[levelArea * layerCount][];
            boolean[] exits = new boolean[levelArea];
            AreaWalker areaWalker = new AreaWalker(reader, layerCount);

            int startVertex = initGraph(areaWalker, graph, exits, levelArea, layerCount);

            return new Solver(startVertex, graph, exits);
        }

        int solve() {
            return findPath(map, startVertex, exits);
        }

        static int initGraph(AreaWalker areaWalker, int[][] graph, boolean[] exits, int levelArea, int layerCount) {
            int startPos = -1;
            while (areaWalker.hasNext()) {
                AreaPoint p = areaWalker.next();
                int currentVertexId = p.z * levelArea + p.y * layerCount + p.x;
                int upperVertexId = currentVertexId - levelArea;
                int leftVertex = currentVertexId - 1;
                int upVertex = currentVertexId - layerCount;
                char currentPosition = p.value;
                switch (currentPosition) {
                    case '#':
                        break;
                    case 'S':
                        startPos = currentVertexId;
                    case '.':
                        graph[currentVertexId] = new int[]{0, 0};
                        if (p.z == 0) {
                            exits[currentVertexId] = true;
                        }
                        if (p.z > 0 && graph[upperVertexId] != null) {
                            connectVertices(graph, currentVertexId, upperVertexId);
                        }
                        if (p.y > 0 && graph[upVertex] != null) {
                            connectVertices(graph, currentVertexId, upVertex);
                        }
                        if (p.x > 0 && graph[leftVertex] != null) {
                            connectVertices(graph, currentVertexId, leftVertex);
                        }
                        break;
                    default:
                        throw new RuntimeException("invalid path");
                }
            }
            return startPos;
        }

        static void connectVertices(int[][] graph, int start, int end) {
            graph[start] = append(graph[start], end);
            graph[end] = append(graph[end], start);
        }

        static int findPath(int[][] graph, int start, boolean[] exits) {
            int exitsAt = exits.length - 1;
            int depth = 0;
            int[] stack = new int[2];
            stack[0] = 1;
            stack[1] = start;
            while (stack[0] > 0) {
                depth++;
                boolean[] appended = new boolean[graph.length];
                int[] vertices = new int[stack[0]];
                System.arraycopy(stack, 1, vertices, 0, stack[0]);
                stack[0] = 0;
                for (int current : vertices) {
                    for (int i = 1; i <= graph[current][0]; i++) {
                        if (graph[current][i] <= exitsAt) {
                            return depth;
                        } else if (!appended[graph[current][i]]) {
                            stack = append(stack, graph[current][i]);
                            appended[graph[current][i]] = true;
                        }
                    }
                }
            }
            return -1;
        }
    }

    static class AreaWalker {
        private final BufferedReader reader;
        private final int size;
        //z,y,x
        private static final int xPos = 2;
        private static final int yPos = 1;
        private static final int zPos = 0;
        private final int[] position = new int[]{0, 0, 0};
        private int absolutePos = 0;
        private String currentLine;

        AreaWalker(BufferedReader reader, int size) {
            this.reader = reader;
            this.size = size;
        }

        boolean hasNext() {
            for (int pos : position) {
                if (pos != size - 1) return true;
            }
            return false;
        }

        void advancePosition() {
            absolutePos = ++absolutePos % ((int) Math.pow(size, 3));
            int baseDecrement = absolutePos;
            for (int i = 0; i < position.length; i++) {
                int base = (int) Math.pow(size, (position.length - 1 - i));
                position[i] = baseDecrement / base;
                baseDecrement -= position[i] * base;
            }
        }

        AreaPoint next() {
            if (currentLine != null) {
                advancePosition();
            }
            if (position[xPos] == 0) {
                nextLine();
            }
            char point = currentLine.charAt(position[xPos]);
            return new AreaPoint(point, position[xPos], position[yPos], position[zPos]);
        }

        void nextLine() {
            while (true) {
                try {
                    if ((currentLine = reader.readLine()) != null && !currentLine.isBlank()) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    static class AreaPoint {
        final char value;
        final int x;
        final int y;
        final int z;

        AreaPoint(char value, int x, int y, int z) {
            this.value = value;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return "c=" + value + ", x=" + x + ", y=" + y + ", z=" + z;
        }
    }

    static int[] append(int[] values, int value) {
        int[] newarr;
        if (values == null) {
            int[] ints = new int[10];
            ints[0] = 1;
            ints[1] = value;
            newarr = ints;
        } else {
            if (values[0] + 1 > values.length - 1) {
                newarr = new int[values.length * 2];
                System.arraycopy(values, 0, newarr, 0, values.length);
            } else {
                newarr = values;
            }
            newarr[++newarr[0]] = value;
        }
        return newarr;
    }
}