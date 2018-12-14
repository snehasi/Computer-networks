import java.sql.SQLSyntaxErrorException;
import java.util.*;

import static java.lang.Math.min;

public class DistanceVector {
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

        for (int i = 0; i < n; i++) {
            System.out.print(i);
            System.out.print(" ");
            System.out.print(nodes.get(i));
            System.out.print("\n");
        }

        for (int i = 0; i < n; i++) {
            edge[i][i] = 0;
            Arrays.fill(edge[i], INF);
        }

        Integer[][] origin = new Integer[n][n];
        Integer rand = 0;
        Integer[][][] cost = new Integer[n][n][n];

        for (int i = 0; i < m; i++) {
            String c = A[3 * i + 2];
            String b = A[3 * i + 1];
            String a = A[3 * i];

            Integer x = nodes.indexOf(a);
            Integer y = nodes.indexOf(b);
            Integer w = Integer.parseInt(c);

            edge[x][y] = w;
            edge[y][x] = w;

            if (DEBUG) {
                System.out.println(a + b + c);
            }
        }


        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++)
                Arrays.fill(cost[i][j], INF);

            Arrays.fill(origin[i], -1);
        }

        while (true) {
            String line = reader.nextLine().replace("\n", "").replace("\r", "");

            if (line.equals("exit")) {
                break;
            } else if (line.equals("print route")) {
                cost_matrix(nodes, edge, n, cost, origin);
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
            } else {
                //System.out.println(line.split(" ")[0]);
                System.out.println("Invalid Input.");
            }
        }
    }

    public static void cost_matrix(ArrayList<String> nodes, Integer[][] edge, Integer n, Integer[][][] cost, Integer[][] origin) {
        Integer[][][] pred = new Integer[n][n][n];


            System.out.println("Init pred");


        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                if (DEBUG) {
                    System.out.println(edge[i][j] + " " + cost[i][i][j]);
                }
                if (edge[i][j] < cost[i][i][j]) {

                        //System.out.println(i + " " + j);


                    origin[i][j] = j;

                        //System.out.println(i + " " + j);


                    cost[i][i][j] = edge[i][j];
                }
            }


            //System.out.println("Init done");


        for (int l = 1; l < n; l++) {
            // copy cost to pred
            if (DEBUG) {
                System.out.println("Running round " + l);
            }
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++) {

                    for (int k = 0; k < n; k++)
                    {

                            //System.out.println(cost[i][j][k]);
                      

                        pred[i][j][k] = cost[i][j][k];
                        //System.out.println(pred[i][j][k]);

                    }
                }


                System.out.println("propagate pred to cost");

            for (int x = 0; x < n; x++) {
                for (int y = 0; y < n; y++) {
                    if (DEBUG) {
                        System.out.println(edge[x][y]);
                    }
                    //System.out.println(edge[x][y]);
                    if (edge[x][y] != INF) {
                        for (int i = 0; i < n; i++) {

                            //System.out.println(cost[y][x][i] //+ " " + cost[y][x][i]);


                            if (pred[y][y][i] < cost[x][y][i])
                            {

                                    System.out.println("UPdating cost");
                                cost[x][y][i] = pred[y][y][i];
                            }

                            if (pred[x][x][i] < cost[y][x][i])
                            {

                                    System.out.println("UPdating cost");

                                cost[y][x][i] = pred[x][x][i];
                            }


                                //System.out.println(cost[x][y][i] + " " + cost[y][x][i]);

                        }
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {

                            //System.out.println(cost[i][i][j] + " " + cost[i][j][k]);


                        Integer left = cost[i][i][j] + cost[i][j][k];
                        Boolean check = left < cost[i][i][k];

                        if (check) {

                                //System.out.println(cost[i][i][k] + " " + origin[i][k]);

                            origin[i][k] = origin[i][j];
                            Integer sum = cost[i][i][j] + cost[i][j][k];
                            cost[i][i][k] = sum;


                                //System.out.println(cost[i][i][k] + " " + origin[i][k]);

                        }
                    }
                }
            }
        }

        System.out.println("");
        System.out.print("--- ");
        System.out.print("Distance Vector Cost Matrix (Cost, Hop)");
        System.out.print(" ---\n");

        for (int i = 0; i < n; i++) {
            System.out.println("-------");
            for (int j = 0; j < n; j++) {
                if (j == 0)
                    System.out.print(nodes.get(i) + " --> ");

                if (cost[0][i][j] == INF)
                    System.out.format("inf");
                else
                    System.out.format("%d", cost[0][i][j]);

                System.out.print(" ");
                System.out.print(nodes.get(origin[i][j]) + " ");

                if (j == n - 1)
                    System.out.print("\n");
                else
                    System.out.print(" | ");
            }
        }
    }
}
