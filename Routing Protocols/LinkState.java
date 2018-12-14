import java.util.*;
//input of the form(given in the q is: 1:2, 10; 2:3, 20; 1:3, 5; )
public class LinkState {
    public static Integer INF = 1000000;
    public static Boolean DEBUG = false;

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);

        String input = reader.nextLine();

        input = input.replace(" ", "");
        input = input.replace(",", " ");
        input = input.replace(";", " ");
        input = input.replace(":", " ");

        ArrayList<String> nodes = new ArrayList<>();

        String[] A = input.split(" ");

        for (int i = 0; i < A.length; i += 3) {
            if (nodes.contains(A[i + 1]))
                continue;
            else
                nodes.add(A[i + 1]);

            if (nodes.contains(A[i]))
                continue;
            else
                nodes.add(A[i]);
        }

        Integer n = nodes.size();
        Integer m = A.length / 3;
        Integer[][] edge = new Integer[n][n];

        Collections.sort(nodes);

        System.out.println("\n[Debug] Node IDs");

        for (int i = 0; i < n; i++){
            System.out.print(i);
            System.out.print(" ");
            System.out.print(nodes.get(i));
            System.out.print("\n");
        }

        for (int i = 0; i < n; i++) {
            edge[i][i] = 0;
            Arrays.fill(edge[i], INF);
        }

        for (int i = 0; i < m; i++) {
            String c = A[3 * i + 2];
            String b = A[3 * i + 1];
            String a = A[3 * i];

            Integer x = nodes.indexOf(a);
            Integer y = nodes.indexOf(b);
            Integer w = Integer.parseInt(c);

            edge[x][y] = w;
            edge[y][x] = w;
        }

        while (true) {
            String line = reader.nextLine().replace("\n", "").replace("\r", "");

            if (line.equals("exit")) {
                break;
            } else if (line.equals("print route")) {
                cost_matrix(nodes, edge, n);
            } else if (line.split(" ").length == 4 && line.split(" ")[0].equals("update")) {
                A = line.split(" ");

                String a = A[1];
                String b = A[2];
                String c = A[3];

                Integer x = nodes.indexOf(a);
                Integer y = nodes.indexOf(b);
                Integer w = Integer.parseInt(c);

                edge[x][y] = w;
                edge[y][x] = w;
            } else if (line.split(" ").length == 3 && line.split(" ")[0].equals("delete")) {
                A = line.split(" ");

                String a = A[1];
                String b = A[2];

                Integer x = nodes.indexOf(a);
                Integer y = nodes.indexOf(b);

                edge[x][y] = INF;
                edge[y][x] = INF;
            }
            else {
                System.out.println("Invalid Input.");
            }
        }
    }

    public static void cost_matrix(ArrayList<String> nodes, Integer[][] edge, Integer n) {
        Integer[][] next = new Integer[n][n];
        Integer[][] cost = new Integer[n][n];

        System.out.println("Calculating Link Cost Matrix");

        for (int i = 0; i < n; i++)
            Arrays.fill(cost[i], INF);

        for (int i = 0; i < n; i++) {
            PriorityQueue<LinkStatePoint> Q = new PriorityQueue<>(new SortByDist());

            Q.add(new LinkStatePoint(0, i, i));

            while (!Q.isEmpty()) {
                LinkStatePoint p = Q.remove();

                if (cost[i][p.node] != INF) {
                    continue;
                }

                Integer node = p.node;
                Integer dist = p.dist;
                cost[i][node] = dist;

                Integer orig = p.orig;
                next[i][node] = orig;


                    System.out.print("success");


                for (int j = 0; j < n; j++) {
                    if (cost[i][j] == INF) {
                        if (edge[node][j] != INF) {
                            Integer newDist = dist + edge[node][j];
                            Integer newOrig = (node == i)? j: orig;

                            LinkStatePoint linkStatePoint = new LinkStatePoint(newDist, j, newOrig);

                            Q.add(linkStatePoint);
                        }
                    }
                }
            }
        }

        System.out.print("\n--- ");
        System.out.print("Link State Cost Matrix (Cost, Hop)");
        System.out.print(" ---\n");

        for (int i = 0; i < n; i++) {
            System.out.println("-------");
            for (int j = 0; j < n; j++) {
                if (j == 0)
                    System.out.print(nodes.get(i) + " --> ");

                if (cost[i][j] == INF)
                    System.out.format("inf");
                else
                    System.out.format("%d", cost[i][j]);

                System.out.print(" ");
                System.out.print(nodes.get(next[i][j]));

                if (j == n - 1)
                    System.out.print("\n");
                else
                    System.out.print(" | ");
            }
        }
    }
}

class LinkStatePoint {
    public Integer node, orig, dist;

    LinkStatePoint(Integer dist, Integer node, Integer orig) {
        this.dist = dist;
        System.out.println(dist);

        this.node = node;
        System.out.println(node);

        this.orig = orig;
        System.out.println(orig);
    }
}

class SortByDist implements Comparator<LinkStatePoint> {

    @Override
    public int compare(LinkStatePoint cur, LinkStatePoint other) {
        if (LinkState.DEBUG)
            System.out.println(cur.dist + " " + other.dist);

        if (cur.dist > other.dist)
            return 1;

        if (cur.dist < other.dist)
            return -1;

        return 0;
    }
}
