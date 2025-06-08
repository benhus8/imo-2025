package org.example;

import org.example.algorithms.HybridEvolutionaryAlgorithm;
import org.example.algorithms.LocalSearch;
import org.example.interfaces.Neighborhood;
import org.example.swaps.BetweenCycleSwapNeighborhood;
import org.example.swaps.TwoEdgeSwapNeighborhood;
import org.example.utils.Instance;
import org.example.utils.Solution;
import org.example.utils.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.utils.Utils.getAllVertices;

public class LocalOptimaSimilarityAnalysis {

    private final int[][] matrix;
    private final Random rng;
    private final Instance instance;

    public LocalOptimaSimilarityAnalysis(Instance instance, int[][] matrix, Random rng) {
        this.instance = instance;
        this.matrix = matrix;
        this.rng = rng;
    }
    public static void main(String[] args) throws IOException {
        String filename = "kroB200.tsp";
        Instance instance = Utils.loadInstance(filename);
        int[][] matrix = Utils.computeDistanceMatrix(instance);
        Random rng = new Random();

        LocalOptimaSimilarityAnalysis analysis = new LocalOptimaSimilarityAnalysis(instance, matrix, rng);
        analysis.run();
    }

    public void run() {
        int NUM_LOCAL_OPTIMA = 1000;

        System.out.println("👉 Zaczynam generowanie " + NUM_LOCAL_OPTIMA + " losowych lokalnych optimum...");

        List<Solution> localOptima = new ArrayList<>();
        List<Double> costs = new ArrayList<>();

        // 1️⃣ Generujemy lokalne optimum
        for (int i = 0; i < NUM_LOCAL_OPTIMA; i++) {
            Solution randomSolution = generateRandomSolution();
            Solution localOptimum = LocalSearch.steepest(randomSolution, getNeighborhoods(), matrix);
            localOptima.add(localOptimum);

            double cost = localOptimum.totalCost(matrix);
            costs.add(cost);

            System.out.println("✅ Lokalny optimum nr " + (i + 1) + " z kosztem: " + cost);
        }

        // 2️⃣ Najlepsze rozwiązanie
        System.out.println("\n🔎 Uruchamiam HybridEvolutionaryAlgorithm dla najlepszego rozwiązania...");
        Solution bestSolution = new HybridEvolutionaryAlgorithm(173, matrix, rng).run(instance);
        System.out.println("⭐ Najlepsze rozwiązanie kosztuje: " + bestSolution.totalCost(matrix));

        // 3️⃣ Obliczamy podobieństwa do najlepszego
        List<Double> similarityToBestEdges = new ArrayList<>();
        List<Double> similarityToBestPairs = new ArrayList<>();

        for (int i = 0; i < NUM_LOCAL_OPTIMA; i++) {
            Solution s = localOptima.get(i);

            double simEdges = similarityEdges(s, bestSolution);
            similarityToBestEdges.add(simEdges);

            double simPairs = similarityPairs(s, bestSolution);
            similarityToBestPairs.add(simPairs);

            System.out.println("🔹 Rozwiązanie nr " + (i + 1) + " ma " + simEdges + " wspólnych krawędzi i " + simPairs + " wspólnych par wierzchołków z najlepszym.");
        }

        // 4️⃣ Średnie podobieństwo do innych (dla obu metryk)
        List<Double> avgSimilaritiesEdges = new ArrayList<>();
        List<Double> avgSimilaritiesPairs = new ArrayList<>();

        for (int i = 0; i < NUM_LOCAL_OPTIMA; i++) {
            double sumEdges = 0;
            double sumPairs = 0;

            for (int j = 0; j < NUM_LOCAL_OPTIMA; j++) {
                if (i == j) continue;

                sumEdges += similarityEdges(localOptima.get(i), localOptima.get(j));
                sumPairs += similarityPairs(localOptima.get(i), localOptima.get(j));
            }

            double avgEdges = sumEdges / (NUM_LOCAL_OPTIMA - 1);
            double avgPairs = sumPairs / (NUM_LOCAL_OPTIMA - 1);

            avgSimilaritiesEdges.add(avgEdges);
            avgSimilaritiesPairs.add(avgPairs);

            System.out.println("🔸 Rozwiązanie nr " + (i + 1) + " ma średnio " + avgEdges + " wspólnych krawędzi i " + avgPairs + " wspólnych par wierzchołków z innymi.");
        }

        // 5️⃣ Korelacje
        double correlationEdges = pearsonCorrelation(costs, avgSimilaritiesEdges);
        double correlationPairs = pearsonCorrelation(costs, avgSimilaritiesPairs);

        System.out.println("\n📊 Korelacja (krawędzie) między kosztem a średnią liczbą wspólnych krawędzi: " + correlationEdges);
        System.out.println("📊 Korelacja (pary) między kosztem a średnią liczbą wspólnych par wierzchołków: " + correlationPairs);

        // 6️⃣ (opcjonalnie) Zapis do CSV – mogę zrobić 2 pliki, np. "edges.csv" i "pairs.csv"
        double correlationToBestPairs = pearsonCorrelation(costs, similarityToBestPairs);

        System.out.println("\n📊 Korelacja (krawędzie) między kosztem a średnią liczbą wspólnych krawędzi: " + correlationEdges);
        System.out.println("📊 Korelacja (pary) między kosztem a średnią liczbą wspólnych par wierzchołków: " + correlationPairs);
        System.out.println("📊 Korelacja (pary) między kosztem a liczbą wspólnych par z najlepszym: " + correlationToBestPairs);

        // 6️⃣ Zapis do CSV
        saveToCsv("edges_similarity_b.csv", costs, avgSimilaritiesEdges);
        saveToCsv("pairs_similarity_b.csv", costs, avgSimilaritiesPairs);
        saveToCsv("best_pairs_similarity_b.csv", costs, similarityToBestPairs);

    }


    private Solution generateRandomSolution() {
        List<Integer> vertices = new ArrayList<>(getAllVertices(instance));
        Collections.shuffle(vertices, rng);
        List<Integer> cycle1 = vertices.subList(0, vertices.size() / 2);
        List<Integer> cycle2 = vertices.subList(vertices.size() / 2, vertices.size());
        return new Solution(new ArrayList<>(cycle1), new ArrayList<>(cycle2));
    }

    private List<Neighborhood> getNeighborhoods() {
        return List.of(new TwoEdgeSwapNeighborhood(), new BetweenCycleSwapNeighborhood());
    }

    private double similarityPairs(Solution s1, Solution s2) {
        int count = 0;
        for (int i = 0; i < instance.dimension; i++) {
            for (int j = i + 1; j < instance.dimension; j++) {
                boolean sameCycleS1 = inSameCycle(s1, i, j);
                boolean sameCycleS2 = inSameCycle(s2, i, j);
                if (sameCycleS1 == sameCycleS2 && sameCycleS1) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean inSameCycle(Solution s, int v1, int v2) {
        return (s.cycle1.contains(v1) && s.cycle1.contains(v2)) ||
                (s.cycle2.contains(v1) && s.cycle2.contains(v2));
    }

    private double similarityEdges(Solution s1, Solution s2) {
        Set<String> edges1 = extractEdges(s1);
        Set<String> edges2 = extractEdges(s2);
//        System.out.println("Local edges: " + edges1 + ", Best edges: " + edges2);
        edges1.retainAll(edges2);
        return edges1.size();
    }

    private Set<String> extractEdges(Solution sol) {
        Set<String> edges = new HashSet<>();

        addCycleEdges(edges, sol.cycle1);
        addCycleEdges(edges, sol.cycle2);
        return edges;
    }

    private void addCycleEdges(Set<String> edges, List<Integer> cycle) {
        for (int i = 0; i < cycle.size(); i++) {
            int from = cycle.get(i);
            int to = cycle.get((i + 1) % cycle.size());
            edges.add(edgeKey(from, to));
        }
    }

    private String edgeKey(int a, int b) {
        return Math.min(a, b) + "-" + Math.max(a, b);
    }

    private double pearsonCorrelation(List<Double> xs, List<Double> ys) {
        int n = xs.size();
        double meanX = xs.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double meanY = ys.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double numerator = 0, denominatorX = 0, denominatorY = 0;
        for (int i = 0; i < n; i++) {
            double dx = xs.get(i) - meanX;
            double dy = ys.get(i) - meanY;
            numerator += dx * dy;
            denominatorX += dx * dx;
            denominatorY += dy * dy;
        }
        return numerator / Math.sqrt(denominatorX * denominatorY);
    }


    private void saveToCsv(List<Double> costs, List<Double> similarities) {
        // Tutaj zapis do pliku CSV (np. z nagłówkiem: cost,avg_similarity)
    }
    private void saveToCsv(String filename, List<Double> costs, List<Double> similarities) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("cost,avg_similarity");
            for (int i = 0; i < costs.size(); i++) {
                pw.println(costs.get(i) + "," + similarities.get(i));
            }
            System.out.println("💾 Dane zapisane do pliku: " + filename);
        } catch (IOException e) {
            System.err.println("❌ Błąd przy zapisie do pliku: " + e.getMessage());
        }
    }
}

