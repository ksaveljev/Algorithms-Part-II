import java.util.Iterator;

public class SAP {
    private final Digraph digraph;
    private final BFSCache vcache, wcache;

    private class BFSCache implements Iterable<Integer> {
        private final boolean[] visited;
        private final int[] distanceTo;
        private final Queue<Integer> modified = new Queue<Integer>();
        private final Queue<Integer> queue = new Queue<Integer>();

        public BFSCache(int size) {
            visited = new boolean[size];
            distanceTo = new int[size];

            for (int i = 0; i < size; i++) {
                visited[i] = false;
                distanceTo[i] = -1;
            }
        }

        public Iterator<Integer> iterator() {
            return modified.iterator();
        }

        public void clear() {
            while (!modified.isEmpty()) {
                int v = modified.dequeue();
                visited[v] = false;
                distanceTo[v] = -1;
            }
        }

        public void bfs(int v) {
            visited[v] = true;
            distanceTo[v] = 0;

            modified.enqueue(v);
            queue.enqueue(v);

            while (!queue.isEmpty()) {
                int w = queue.dequeue();

                for (int next : digraph.adj(w)) {
                    if (!visited[next]) {
                        visited[next] = true;
                        distanceTo[next] = distanceTo[w] + 1;
                        modified.enqueue(next);
                        queue.enqueue(next);
                    }
                }
            }
        }

        public void bfs(Iterable<Integer> v) {
            for (int w : v) {
                visited[w] = true;
                distanceTo[w] = 0;

                modified.enqueue(w);
                queue.enqueue(w);
            }

            while (!queue.isEmpty()) {
                int w = queue.dequeue();

                for (int next : digraph.adj(w)) {
                    if (!visited[next]) {
                        visited[next] = true;
                        distanceTo[next] = distanceTo[w] + 1;
                        modified.enqueue(next);
                        queue.enqueue(next);
                    }
                }
            }
        }

        public boolean canReach(int v) {
            return visited[v];
        }

        public int distanceTo(int v) {
            return distanceTo[v];
        }
    }

    public SAP(Digraph G) {
        this.digraph = new Digraph(G);
        this.vcache = new BFSCache(G.V());
        this.wcache = new BFSCache(G.V());
    }

    public int length(int v, int w) {
        precalc(v, w);

        return findDistance();
    }

    public int ancestor(int v, int w) {
        precalc(v, w);

        return findAncestor();
    }

    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        precalc(v, w);

        return findDistance();
    }

    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        precalc(v, w);

        return findAncestor();
    }

    private int findDistance() {
        int result = -1;
        BFSCache[] caches = { vcache, wcache };

        for (BFSCache cache : caches) {
            for (int v : cache) {
                if (vcache.canReach(v) && wcache.canReach(v)) {
                    int distance = vcache.distanceTo(v) + wcache.distanceTo(v);

                    if (result == -1 || distance < result) {
                        result = distance;
                    }
                }
            }
        }

        return result;
    }

    private int findAncestor() {
        int minDistance = -1;
        int ancestor = -1;
        BFSCache[] caches = { vcache, wcache };

        for (BFSCache cache : caches) {
            for (int v : cache) {
                if (vcache.canReach(v) && wcache.canReach(v)) {
                    int distance = vcache.distanceTo(v) + wcache.distanceTo(v);

                    if (minDistance < 0 || distance < minDistance) {
                        minDistance = distance;
                        ancestor = v;
                    }
                }
            }
        }

        return ancestor;
    }

    private void precalc(int v, int w) {
        verifyInput(v);
        verifyInput(w);

        vcache.clear();
        wcache.clear();

        vcache.bfs(v);
        wcache.bfs(w);
    }

    private void precalc(Iterable<Integer> v, Iterable<Integer> w) {
        verifyInput(v);
        verifyInput(w);

        vcache.clear();
        wcache.clear();

        vcache.bfs(v);
        wcache.bfs(w);
    }

    private void verifyInput(int v) {
        if (v < 0 || v >= digraph.V())
            throw new java.lang.IndexOutOfBoundsException();
    }

    private void verifyInput(Iterable<Integer> v) {
        for (int w : v) {
            verifyInput(w);
        }
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);

        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
