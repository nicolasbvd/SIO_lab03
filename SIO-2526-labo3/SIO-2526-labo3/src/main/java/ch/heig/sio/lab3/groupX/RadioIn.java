package ch.heig.sio.lab3.groupX;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.Locale;


/**
 * Modélisation et résolution du problème "RadioIn""
 *
 * @author J.-F. Hêche
 */

public class RadioIn {
  static {
    // Chargement des bibliothèques natives
    Loader.loadNativeLibraries();
    // Utilisation de points décimaux
    Locale.setDefault(Locale.US);
  }

  private static final double infinity = MPSolver.infinity();

  public static void main(String[] args) {
    // Instantiation du solveur linéaire GLOP
    MPSolver solver = MPSolver.createSolver("GLOP");
    if (solver == null) {
      System.out.println("Impossible de créer le solveur GLOP.");
      return;
    }

    // Variables
    MPVariable x1 = solver.makeNumVar(0.0, infinity, "Nb. de radios de type 1 produites");
    MPVariable x2 = solver.makeNumVar(0.0, infinity, "Nb. de radios de type 2 produites");

    // Fonction objectif
    MPObjective chiffresAffaires = solver.objective();
    chiffresAffaires.setCoefficient(x1, 75.0);
    chiffresAffaires.setCoefficient(x2, 50.);
    chiffresAffaires.setMaximization();

    // Contraintes
    MPConstraint c1 = solver.makeConstraint(-infinity, 26, "Tps travail de Pierre");
    c1.setCoefficient(x1, 1);
    c1.setCoefficient(x2, 2);

    MPConstraint c2 = solver.makeConstraint(-infinity, 34, "Tps travail de Paul");
    c2.setCoefficient(x1, 2);
    c2.setCoefficient(x2, 1);

    MPConstraint c3 = solver.makeConstraint(-infinity, 42, "Tps travail de Jacques");
    c3.setCoefficient(x1, 2);
    c3.setCoefficient(x2, 3);

    // Résolution
    MPSolver.ResultStatus status = solver.solve();
    switch (status) {
      case OPTIMAL:
        System.out.println("Solution optimale trouvée :");
        System.out.printf("%s = %.3f%n", x1.name(), x1.solutionValue());
        System.out.printf("%s = %.3f%n", x2.name(), x2.solutionValue());
        System.out.printf("Chiffre d'affaires hebdo. maximal = %.3f%n", chiffresAffaires.value());
        break;

      case UNBOUNDED:
        System.out.println("Problème non borné.");
        break;

      case INFEASIBLE:
        System.out.println("Problème sans solutions admissibles.");
        break;

      default: // FEASIBLE, ABNORMAL, NOT_SOLVED, MODEL_INVALID
        System.out.println("Le solveur n’a pas réussi à résoudre le modèle (status = " + status + ").");
        break;
    }
  }
}

