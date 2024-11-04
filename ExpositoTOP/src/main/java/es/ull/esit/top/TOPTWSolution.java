package top;

import java.util.Arrays;
import java.util.logging.Logger;
import es.ull.esit.utilities.ExpositoUtilities;

public class TOPTWSolution {
    public static final int NO_INITIALIZED = -1;
    private static final Logger logger = Logger.getLogger(TOPTWSolution.class.getName());

    private TOPTW problem;
    private int[] predecessors;
    private int[] successors;
    private double[] waitingTime;
    private int[] positionInRoute;
    private int[] routes;
    private int availableVehicles;
    private double objectiveFunctionValue;

    public TOPTWSolution(TOPTW problem) {
        this.problem = problem;
        this.availableVehicles = this.problem.getVehicles();
        this.predecessors = new int[this.problem.getPOIs() + this.problem.getVehicles()];
        this.successors = new int[this.problem.getPOIs() + this.problem.getVehicles()];
        this.waitingTime = new double[this.problem.getPOIs()];
        this.positionInRoute = new int[this.problem.getPOIs()];

        Arrays.fill(this.predecessors, TOPTWSolution.NO_INITIALIZED);
        Arrays.fill(this.successors, TOPTWSolution.NO_INITIALIZED);
        Arrays.fill(this.waitingTime, TOPTWSolution.NO_INITIALIZED);
        Arrays.fill(this.positionInRoute, TOPTWSolution.NO_INITIALIZED);

        this.routes = new int[this.problem.getVehicles()];
        this.objectiveFunctionValue = TOPTWEvaluator.NO_EVALUATED;
    }

    public void initSolution() {
        this.predecessors = new int[this.problem.getPOIs() + this.problem.getVehicles()];
        this.successors = new int[this.problem.getPOIs() + this.problem.getVehicles()];

        Arrays.fill(this.predecessors, TOPTWSolution.NO_INITIALIZED);
        Arrays.fill(this.successors, TOPTWSolution.NO_INITIALIZED);
        Arrays.fill(this.routes, TOPTWSolution.NO_INITIALIZED);

        this.routes[0] = 0;
        this.predecessors[0] = 0;
        this.successors[0] = 0;
        this.availableVehicles = this.problem.getVehicles() - 1;
    }

    public boolean isDepot(int c) {
        for (int route : this.routes) {
            if (c == route) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TOPTWSolution)) return false;
        TOPTWSolution otherSolution = (TOPTWSolution) obj;

        return Arrays.equals(this.predecessors, otherSolution.predecessors);
    }

    public int getAvailableVehicles() {
        return this.availableVehicles;
    }

    public int getCreatedRoutes() {
        return this.problem.getVehicles() - this.availableVehicles;
    }

    public double getDistance(int x, int y) {
        return this.problem.getDistance(x, y);
    }

    public void setAvailableVehicles(int availableVehicles) {
        this.availableVehicles = availableVehicles;
    }

    public int getPredecessor(int customer) {
        return this.predecessors[customer];
    }

    public int[] getPredecessors() {
        return this.predecessors;
    }

    public TOPTW getProblem() {
        return this.problem;
    }

    public double getObjectiveFunctionValue() {
        return this.objectiveFunctionValue;
    }

    public int getPositionInRoute(int customer) {
        return this.positionInRoute[customer];
    }

    public int getSuccessor(int customer) {
        return this.successors[customer];
    }

    public int[] getSuccessors() {
        return this.successors;
    }

    public int getIndexRoute(int index) {
        return this.routes[index];
    }

    public double getWaitingTime(int customer) {
        return this.waitingTime[customer];
    }

    public void setObjectiveFunctionValue(double objectiveFunctionValue) {
        this.objectiveFunctionValue = objectiveFunctionValue;
    }

    public void setPositionInRoute(int customer, int position) {
        this.positionInRoute[customer] = position;
    }

    public void setPredecessor(int customer, int predecessor) {
        this.predecessors[customer] = predecessor;
    }

    public void setSuccessor(int customer, int successor) {
        this.successors[customer] = successor;
    }

    public void setWaitingTime(int customer, int waitingTime) {
        this.waitingTime[customer] = waitingTime;
    }

    public String getInfoSolution() {
        final int COLUMN_WIDTH = 15;
        StringBuilder text = new StringBuilder("\n" + "NODES: " + this.problem.getPOIs() + "\n" +
                "MAX TIME PER ROUTE: " + this.problem.getMaxTimePerRoute() + "\n" +
                "MAX NUMBER OF ROUTES: " + this.problem.getMaxRoutes() + "\n");
        StringBuilder textSolution = new StringBuilder("\n" + "SOLUTION: " + "\n");

        double costTimeSolution = 0.0;
        double fitnessScore = 0.0;
        boolean validSolution = true;

        for (int k = 0; k < this.getCreatedRoutes(); k++) {
            String[] strings = {"\n" + "ROUTE " + k};
            int[] width = new int[strings.length];
            Arrays.fill(width, COLUMN_WIDTH);
            text.append(ExpositoUtilities.getFormat(strings, width)).append("\n");

            strings = new String[]{"CUST NO.", "X COORD.", "Y. COORD.", "READY TIME", "DUE DATE", "ARRIVE TIME", "LEAVE TIME", "SERVICE TIME"};
            Arrays.fill(width, COLUMN_WIDTH);
            text.append(ExpositoUtilities.getFormat(strings, width)).append("\n");

            int depot = this.getIndexRoute(k);
            int pre = depot;
            double costTimeRoute = 0.0;
            double fitnessScoreRoute = 0.0;

            do {
                int suc = this.getSuccessor(pre);
                textSolution.append(pre).append(" - ");

                int index = 0;
                strings[index++] = "" + suc;
                strings[index++] = "" + this.getProblem().getX(suc);
                strings[index++] = "" + this.getProblem().getY(suc);
                strings[index++] = "" + this.getProblem().getReadyTime(suc);
                strings[index++] = "" + this.getProblem().getDueTime(suc);

                costTimeRoute += this.getDistance(pre, suc);
                if (costTimeRoute < this.getProblem().getDueTime(suc)) {
                    if (costTimeRoute < this.getProblem().getReadyTime(suc)) {
                        costTimeRoute = this.getProblem().getReadyTime(suc);
                    }
                    strings[index++] = "" + costTimeRoute;
                    costTimeRoute += this.getProblem().getServiceTime(suc);
                    strings[index++] = "" + costTimeRoute;
                    strings[index++] = "" + this.getProblem().getServiceTime(pre);

                    if (costTimeRoute > this.getProblem().getMaxTimePerRoute()) {
                        validSolution = false;
                    }
                    fitnessScoreRoute += this.problem.getScore(suc);
                } else {
                    validSolution = false;
                }
                pre = suc;
                text.append(ExpositoUtilities.getFormat(strings, width)).append("\n");
            } while (pre != depot);

            textSolution.append(depot).append("\n");
            costTimeSolution += costTimeRoute;
            fitnessScore += fitnessScoreRoute;
        }

        textSolution.append("FEASIBLE SOLUTION: ").append(validSolution).append("\n")
                .append("SCORE: ").append(fitnessScore).append("\n")
                .append("TIME COST: ").append(costTimeSolution).append("\n");

        return textSolution.append(text).toString();
    }

    public double evaluateFitness() {
        double objectiveFunction = 0.0;

        for (int k = 0; k < this.getCreatedRoutes(); k++) {
            int depot = this.getIndexRoute(k);
            int pre = depot;

            double objectiveFunctionPerRoute = 0.0;
            do {
                int suc = this.getSuccessor(pre);
                objectiveFunctionPerRoute += this.problem.getScore(suc);
                pre = suc;
            } while (pre != depot);

            objectiveFunction += objectiveFunctionPerRoute;
        }
        return objectiveFunction;
    }

    public int addRoute() {
        int depot = this.problem.getPOIs() + 1;
        int routePos = 1;

        for (int i = 0; i < this.routes.length; i++) {
            if (this.routes[i] != -1 && this.routes[i] != 0) {
                depot = this.routes[i] + 1;
                routePos = i + 1;
            }
        }
        this.routes[routePos] = depot;
        this.availableVehicles--;
        this.predecessors[depot] = depot;
        this.successors[depot] = depot;
        this.problem.addNodeDepot();
        return depot;
    }

    public double printSolution() {
        for (int k = 0; k < this.getCreatedRoutes(); k++) {
            int depot = this.getIndexRoute(k);
            int pre = depot;

            StringBuilder route = new StringBuilder();
            do {
                int suc = this.getSuccessor(pre);
                route.append(pre).append(" - ");
                pre = suc;
            } while (pre != depot);
            route.append(depot);
            logger.info(route.toString());
        }

        double fitness = this.evaluateFitness();
        logger.info("SC=" + fitness);
        return fitness;
    }
}
