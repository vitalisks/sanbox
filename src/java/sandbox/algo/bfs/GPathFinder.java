import java.io.*;

class GPathFinder {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        writer.write(String.valueOf(Solver.start(reader).solve()));
        reader.close();
        writer.close();
    }
    // uses BFS to find sum of paths for set of from vertices to target vertex
    // precomputes semi optimal graph with possible directions
    static class Solver {
        private static final int NARROW_PATH_THRESHOLD = 8;
        private static final int NOT_SET = 0;
        // possible move directions
        private static final int[][] moveMatrix = new int[][]{{-1, -2}, {1, -2}, {2, -1}, {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}};

        private final int width;
        private final int height;
        private final int[][] precomputedGraph;
        private final int[] distanceCache;
        private final ResetAbleInt appendTracker;
        private final ResetAbleBool visited;

        private final SimpleVertexStack simpleVertexStack = new SimpleVertexStack();
        private final int endX;
        private final int endY;
        private final int[] fromVertices;

        public Solver(
                int width,
                int height,
                int targetX,
                int targetY,
                int[] fromVertices
        ) {
            this.width = width;
            this.height = height;
            this.endX = targetX;
            this.endY = targetY;
            this.fromVertices = fromVertices;

            int vertexCount = width * height;

            precomputedGraph = new int[vertexCount][];
            distanceCache = new int[vertexCount];
            appendTracker = new ResetAbleInt(new int[vertexCount]);
            visited = new ResetAbleBool(new boolean[vertexCount]);
        }

        int solve() {
            int endVertex = width * endY + endX;

            initializeGraph();

            int total = 0;
            for (int i = 0; i < fromVertices.length; i++) {
                int fromVertex = fromVertices[i];
                if (fromVertex == endVertex) {
                } else {
                    int result = findPath(fromVertex, endVertex);
                    if (result == -1) {
                        total = -1;
                        break;
                    } else {
                        total += result;
                    }
                }
            }
            return total;
        }

        static Solver start(BufferedReader reader) throws IOException {
            String[] params = reader.readLine().split(" ");
            int width = Integer.parseInt(params[0]);
            int height = Integer.parseInt(params[1]);
            int endX = Integer.parseInt(params[2]) - 1;
            int endY = Integer.parseInt(params[3]) - 1;
            int unitCount = Integer.parseInt(params[4]);
            int[] fromVertices = new int[unitCount];

            for (int i = 0; i < unitCount; i++) {
                String[] coords = reader.readLine().split(" ");
                int fromX = Integer.parseInt(coords[0]) - 1;
                int fromY = Integer.parseInt(coords[1]) - 1;
                int fromVertex = width * fromY + fromX;
                fromVertices[i] = fromVertex;
            }
            return new Solver(
                    width,
                    height,
                    endX,
                    endY,
                    fromVertices
            );
        }

        void initializeGraph() {
            for (int i = 0; i < precomputedGraph.length; i++) {
                int vY = getVertexY(i);
                int vX = getVertexX(i);
                int dX = Math.abs(vX - endX);
                int dY = Math.abs(vY - endY);
                distanceCache[i] = dX + dY;
                precomputedGraph[i] = getVertices(i).toArray();
            }
        }

        int getVertexY(int i) {
            return i / width;
        }

        int getVertexX(int i) {
            return i - getVertexY(i) * width;
        }

        IntStack getVertices(int i) {
            IntStack moveDirections = new IntStack();
            int vY = getVertexY(i);
            int vX = getVertexX(i);

            int minDistance = distanceCache[i];
            int maxArea = 0;
            int maxDistance = minDistance;

            for (int[] moveOffset : moveMatrix) {
                int nX = vX + moveOffset[0];
                int nY = vY + moveOffset[1];
                if (nY >= 0 && nY < height && nX >= 0 && nX < width) {
                    int dX = Math.abs(Math.abs(endX) - Math.abs(nX));
                    int dY = Math.abs(Math.abs(endY) - Math.abs(nY));
                    int curDistance = dX + dY;
                    int rV = nY * width + nX;

                    if (maxDistance > NARROW_PATH_THRESHOLD) {
                        if (curDistance < minDistance) {
                            moveDirections.reset().append(rV);
                            minDistance = curDistance;
                            maxArea = dX * dY;
                        } else if (curDistance == minDistance && dX * dY > maxArea) {
                            moveDirections.append(rV);
                            maxArea = dX * dY;
                        }
                    } else {
                        moveDirections.append(rV);
                    }
                }
            }
            return moveDirections;
        }

        int findPath(int start, int end) {
            int depth = 0;
            int stackStart = 0;
            int stackEnd;
            simpleVertexStack.reset();
            simpleVertexStack.append(-1, start);
            boolean newVerticesFound = true;
            visited.reset();
            while (newVerticesFound) {
                depth++;
                stackEnd = simpleVertexStack.size() - 1;
                int tmpStackStart = stackStart;
                stackStart = stackEnd;
                IntStack foundVertexStack = new IntStack();
                appendTracker.reset();

                for (int i = tmpStackStart; i <= stackEnd; i++) {
                    int current = simpleVertexStack.vertex(i);
                    if (visited.get(current)) continue;
                    if (checkVertex(foundVertexStack, precomputedGraph[current], appendTracker, i, end)) {
                        return depth;
                    }
                    visited.set(current, true);
                }
                newVerticesFound = appendToQueue(foundVertexStack.toArray(), simpleVertexStack);
            }
            return -1;
        }

        static boolean checkVertex(IntStack toAppend, int[] vertices, ResetAbleInt appended, int parentVertex, int end) {
            for (int vertex : vertices) {
                if (vertex == end) {
                    return true;
                } else if (appended.get(vertex) <= NOT_SET) {
                    toAppend.append(vertex);
                    appended.set(vertex, parentVertex);
                }
            }
            return false;
        }

        boolean appendToQueue(int[] toAppend, SimpleVertexStack simpleVertexStack) {
            int minDistance = width + height;
            int minDistanceVertex = -1;
            int vCnt = -1;

            for (int v = 0; v < toAppend.length; v++) {
                int curDistance = distanceCache[toAppend[v]];
                if (curDistance <= minDistance && curDistance > NARROW_PATH_THRESHOLD) {
                    minDistanceVertex = toAppend[v];
                    minDistance = curDistance;
                } else if (curDistance <= NARROW_PATH_THRESHOLD) {
                    toAppend[++vCnt] = toAppend[v];
                }
            }
            if (minDistanceVertex >= 0) {
                toAppend[++vCnt] = minDistanceVertex;
            }
            for (int v = 0; v <= vCnt; v++) {
                int vertex = toAppend[v];
                simpleVertexStack.append(appendTracker.values[vertex], vertex);
            }
            return vCnt != -1;
        }
    }

    static class ResetAbleInt {
        private final int[] values;
        private final IntStack set = new IntStack();

        ResetAbleInt(int[] values) {
            this.values = values;
        }

        void set(int idx, int value) {
            values[idx] = value;
            set.append(idx);
        }

        void reset() {
            for (int i = 0; i < set.size(); i++) {
                values[set.get(i)] = 0;
            }
            set.reset();
        }

        int get(int i) {
            return values[i];
        }
    }

    static class ResetAbleBool {
        private final boolean[] values;
        private final IntStack set = new IntStack();

        ResetAbleBool(boolean[] values) {
            this.values = values;
        }

        void set(int idx, boolean value) {
            values[idx] = value;
            set.append(idx);
        }

        void reset() {
            for (int i = 0; i < set.size(); i++) {
                values[set.get(i)] = false;
            }
            set.reset();
        }

        boolean get(int i) {
            return values[i];
        }
    }

    static class SimpleVertexStack {
        private final IntStack parents = new IntStack();
        private final IntStack vertices = new IntStack();

        void append(int parent, int vertex) {
            parents.append(parent);
            vertices.append(vertex);
        }

        int size() {
            return vertices.size();
        }

        int vertex(int i) {
            return vertices.get(i);
        }

        void reset() {
            parents.reset();
            vertices.reset();
        }
    }

    static class IntStack {
        private int[] stack = new int[10];
        private int pointer = -1;

        void append(int value) {
            if (pointer + 1 > stack.length - 1) {
                int[] newStack = new int[stack.length * 2];
                System.arraycopy(stack, 0, newStack, 0, stack.length);
                stack = newStack;
            }
            stack[++pointer] = value;
        }

        int[] toArray() {
            int[] arr = new int[pointer + 1];
            System.arraycopy(stack, 0, arr, 0, arr.length);
            return arr;
        }

        int size() {
            return pointer + 1;
        }

        int get(int i) {
            return stack[i];
        }

        IntStack reset() {
            pointer = -1;
            return this;
        }
    }

}