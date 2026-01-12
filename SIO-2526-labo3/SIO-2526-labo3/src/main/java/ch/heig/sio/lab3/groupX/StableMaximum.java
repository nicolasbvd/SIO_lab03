package ch.heig.sio.lab3.groupX;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Modélisation et résolution du problème du stable de cardinal maximum avec OR_Tools
 *
 * @author E.rappos, J.-F. Hêche
 */
public final class StableMaximum {
  static {
    // Chargement des bibliothèques natives
    Loader.loadNativeLibraries();
    // Utilisation de points décimaux
    Locale.setDefault(Locale.US);
  }

  private static final double infinity = MPSolver.infinity();

  public static void main(String[] args) {

    int N = 0; // Nombre de sommets
    int E = 0; // Nombre d'arêtes
    boolean[][] adjacencyMatrix = null;
    String[] GRAPHS = {"Hugo", "Horaire"};

    for (String graph : GRAPHS) {
      try (BufferedReader br = new BufferedReader(
          new InputStreamReader(new FileInputStream("./data/" + graph + ".txt"), StandardCharsets.UTF_8))) {

        System.out.printf("Lecture du ficher %s : ", graph + ".txt");

        String line;
        int lineNumber = 0;
        while ((line = br.readLine()) != null) {
          lineNumber++;
          line = line.trim();
          if (line.isEmpty()) continue;

          // Première ligne : nombre de sommets
          if (lineNumber == 1) {
            try {
              N = Integer.parseInt(line);
            } catch (NumberFormatException e) {
              throw new IllegalArgumentException(
                  "Ligne 1: nombre de sommets attendu, obtenu: " + line);
            }
            if (N <= 0) {
              throw new IllegalArgumentException("Le nombre de sommets doit être positif");
            }
            adjacencyMatrix = new boolean[N][N];
            continue;
          }

          // Lignes suivantes : arêtes
          String[] parts = line.split("\\s+");
          if (parts.length != 2) {
            throw new IllegalArgumentException(
                "Ligne " + lineNumber + ": format invalide (deux entiers attendus): " + line);
          }

          int n1, n2;
          try {
            n1 = Integer.parseInt(parts[0]) - 1;
            n2 = Integer.parseInt(parts[1]) - 1;
          } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Ligne " + lineNumber + ": nombres invalides: " + line);
          }

          // Validation des noms des sommets
          if (n1 < 0 || n1 >= N || n2 < 0 || n2 >= N) {
            throw new IllegalArgumentException(
                "Ligne " + lineNumber + ": sommet hors limites (1.." + N + "): " + line);
          }

          adjacencyMatrix[n1][n2] = adjacencyMatrix[n2][n1] = true;
          E++;
        }
        System.out.printf("le graphe contient %d sommets et %d arêtes%n", N, E);

      } catch (IOException e) {
        System.err.println("Erreur lors de la lecture de " + graph + ".txt");
        e.printStackTrace();
      }

      MPSolver solver = MPSolver.createSolver("SCIP");
      if (solver == null) {
        System.out.println("Impossible de créer le solveur SCIP");
        return;
      }

      long t1 = System.nanoTime();

      // Variables

      // Allocation d'un tableau (vecteur) de variables, une par sommet du graphe
      MPVariable[] xvars = new MPVariable[N];
      // Création d'une variable binaire (booléenne) pour chaque sommet, valant 1 si le sommet est sélectionné, 0 sinon
      for (int i = 0; i < N; i++) {
        xvars[i] = solver.makeBoolVar("x" + i);
      }

      // Contraintes

      // Une extrémité retenue pour chaque arête {i,j} : xi + xj <= 1
      for (int i = 0; i < N; i++) {
        for (int j = i + 1; j < N; j++) {
          if (adjacencyMatrix[i][j]) {
            MPConstraint c1 = solver.makeConstraint(-infinity, 1, "c_edge_" + i + "_" + j);
            c1.setCoefficient(xvars[i], 1);
            c1.setCoefficient(xvars[j], 1);
          }
        }
      }

      // Fonction objectif

      // Maximiser le cardinal du stable, c.-à-d. le nombre de sommets sélectionnés
      MPObjective objective = solver.objective();
      for (int i = 0; i < N; i++) {
        objective.setCoefficient(xvars[i], 1);
      }
      objective.setMaximization();

      long t2 = System.nanoTime();

      MPSolver.ResultStatus resultStatus = solver.solve();

      long t3 = System.nanoTime();

      if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
        System.out.println("Solution optimale trouvée :");
        System.out.printf("Cardinal maximal d'un stable = %d%n", (int) objective.value());
//        for (MPVariable var : xvars) {
//          System.out.println(var.name() + "= " + var.solutionValue());
//        }

      } else {
        System.err.println("Le solveur n’a pas réussi à résoudre le modèle (status = " + resultStatus + ").");
      }

      System.out.printf("Temps de création du modèle (sec) = %.4f%n", (t2 - t1) / 1e9);
      System.out.printf("Temps de résolution (sec) = %.4f%n", (t3 - t2) / 1e9);
      System.out.println("*****************************************");
    }

  }
}
