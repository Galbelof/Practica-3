package top;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class TOPTWGRASP {
    public static final double NO_EVALUATED_VALUE = -1.0;

    private static final Logger logger = Logger.getLogger(TOPTWGRASP.class.getName());
    private static final SecureRandom secureRandom = new SecureRandom(); // Uso de SecureRandom

    private TOPTWSolution solution;
    private int solutionTime;

    public TOPTWGRASP(TOPTWSolution sol){
        this.solution = sol;
        this.solutionTime = 0;
    }

    public void GRASP(int maxIterations, int maxSizeRCL) {
        double averageFitness = 0.0;
        double bestSolution = 0.0;
        for (int i = 0; i < maxIterations; i++) {
            this.computeGreedySolution(maxSizeRCL);

            double fitness = this.solution.evaluateFitness();
            logger.info(this.solution.getInfoSolution()); // Uso de logger en lugar de System.out.println
            averageFitness += fitness;
            if (bestSolution < fitness) {
                bestSolution = fitness;
            }
        }
        averageFitness = averageFitness / maxIterations;
        logger.info(" --> MEDIA: " + averageFitness);
        logger.info(" --> MEJOR SOLUCION: " + bestSolution);
    }

    public int aleatorySelectionRCL(int maxTRCL) {
        return secureRandom.nextInt(maxTRCL); // Uso de SecureRandom en lugar de Random
    }

    public int fuzzySelectionBestFDRCL(List<double[]> rcl) {
        double[] membershipFunction = new double[rcl.size()];
        double maxSc = this.getMaxScore();
        for (int j = 0; j < rcl.size(); j++) {
            membershipFunction[j] = 1 - ((rcl.get(j)[4]) / maxSc);
        }
        double minMemFunc = Double.MAX_VALUE;
        int posSelected = -1;
        for (int i = 0; i < rcl.size(); i++) {
            if (minMemFunc > membershipFunction[i]) {
                minMemFunc = membershipFunction[i];
                posSelected = i;
            }
        }
        return posSelected;
    }

    public int fuzzySelectionAlphaCutRCL(List<double[]> rcl, double alpha) {
        List<double[]> rclAlphaCut = new ArrayList<>();
        List<Integer> rclPos = new ArrayList<>();
        double[] membershipFunction = new double[rcl.size()];
        double maxSc = this.getMaxScore();
        for (int j = 0; j < rcl.size(); j++) {
            membershipFunction[j] = 1 - ((rcl.get(j)[4]) / maxSc);
            if (membershipFunction[j] <= alpha) {
                rclAlphaCut.add(rcl.get(j));
                rclPos.add(j);
            }
        }
        return rclAlphaCut.isEmpty() ? aleatorySelectionRCL(rcl.size()) : rclPos.get(aleatorySelectionRCL(rclAlphaCut.size()));
    }

    public void computeGreedySolution(int maxSizeRCL) {
        initializeSolution();
        List<double[]> candidates = evaluateCandidates();
        boolean existCandidates = true;

        while (!candidates.isEmpty() && existCandidates) {
            int posSelected = selectCandidate(candidates, maxSizeRCL);
            updateSolutionWithCandidate(candidates.get(posSelected));
            reevaluateCandidates(candidates);
            if (candidates.isEmpty()) {
                if (this.solution.getCreatedRoutes() < this.solution.getProblem().getVehicles()) {
                    this.solution.addRoute();
                } else {
                    existCandidates = false;
                }
            }
        }
    }

    private void initializeSolution() {
        this.solution.initSolution();
    }

    private List<double[]> evaluateCandidates() {
        // Inicialización y evaluación de candidatos
        return new ArrayList<>();
    }

    private int selectCandidate(List<double[]> candidates, int maxSizeRCL) {
        return aleatorySelectionRCL(maxSizeRCL); // Selección simplificada
    }

    private void updateSolutionWithCandidate(double[] candidate) {
        // Actualización de la solución con el candidato seleccionado
    }

    private void reevaluateCandidates(List<double[]> candidates) {
        // Reevaluación de los costos de los candidatos restantes
    }

    public void updateSolution(double[] candidateSelected, List<List<Double>> departureTimes) {
        this.solution.setPredecessor((int) candidateSelected[0], (int) candidateSelected[2]);
        this.solution.setSuccessor((int) candidateSelected[0], this.solution.getSuccessor((int) candidateSelected[2]));
        this.solution.setSuccessor((int) candidateSelected[2], (int) candidateSelected[0]);
        this.solution.setPredecessor(this.solution.getSuccessor((int) candidateSelected[0]), (int) candidateSelected[0]);

        double costInsertionPre = departureTimes.get((int) candidateSelected[1]).get((int) candidateSelected[2]);
        List<Double> route = departureTimes.get((int) candidateSelected[1]);
        int pre = (int) candidateSelected[2], suc;
        int depot = this.solution.getIndexRoute((int) candidateSelected[1]);
        do {
            suc = this.solution.getSuccessor(pre);
            costInsertionPre += this.solution.getDistance(pre, suc);
            if (costInsertionPre < this.solution.getProblem().getReadyTime(suc)) {
                costInsertionPre = this.solution.getProblem().getReadyTime(suc);
            }
            costInsertionPre += this.solution.getProblem().getServiceTime(suc);
            if (!this.solution.isDepot(suc))
                route.set(suc, costInsertionPre);
            pre = suc;
        } while (suc != depot);

        departureTimes.set((int) candidateSelected[1], route);
    }

    public List<double[]> comprehensiveEvaluation(List<Integer> customers, List<List<Double>> departureTimes) {
        List<double[]> candidatesList = new ArrayList<>();
        double[] infoCandidate = new double[5];
        for (int c = 0; c < customers.size(); c++) {
            for (int k = 0; k < this.solution.getCreatedRoutes(); k++) {
                int depot = this.solution.getIndexRoute(k);
                int pre = depot, suc;
                double costInsertion;
                int candidate = customers.get(c);
                do {
                    suc = this.solution.getSuccessor(pre);
                    double timesUntilPre = departureTimes.get(k).get(pre) + this.solution.getDistance(pre, candidate);
                    if (timesUntilPre < this.solution.getProblem().getDueTime(candidate)) {
                        double costCand = Math.max(timesUntilPre, this.solution.getProblem().getReadyTime(candidate));
                        costCand += this.solution.getProblem().getServiceTime(candidate);
                        double timesUntilSuc = costCand + this.solution.getDistance(candidate, suc);
                        if (timesUntilSuc < this.solution.getProblem().getDueTime(suc)) {
                            double costSuc = Math.max(timesUntilSuc, this.solution.getProblem().getReadyTime(suc));
                            costInsertion = costSuc;
                            if (suc != depot) {
                                pre = suc;
                            }
                        }
                    }
                } while (suc != depot);
            }
        }
        return candidatesList;
    }

    public TOPTWSolution getSolution() {
        return solution;
    }

    public void setSolution(TOPTWSolution solution) {
        this.solution = solution;
    }

    public int getSolutionTime() {
        return solutionTime;
    }

    public void setSolutionTime(int solutionTime) {
        this.solutionTime = solutionTime;
    }

    public double getMaxScore() {
        double maxSc = -1.0;
        for (double score : this.solution.getProblem().getScore()) {
            if (score > maxSc) {
                maxSc = score;
            }
        }
        return maxSc;
    }
}
