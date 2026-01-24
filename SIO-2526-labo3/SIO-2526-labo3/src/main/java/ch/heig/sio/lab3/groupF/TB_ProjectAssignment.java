package ch.heig.sio.lab3.groupF;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

/**
 * SIO - 2025-2026, travail pratique n° 3
 * Modélisation et résolution d'un problème d'affectation de travaux de bachelor.'
 *
 * @author
 * @author
 */
public class TB_ProjectAssignment {
  static {
    // Chargement des bibliothèques natives
    Loader.loadNativeLibraries();
  }

  private static final double infinity = MPSolver.infinity();

  public static void main(String[] args) {

    int N_PROFS = 0; // nombre de profs
    int N_STUDENTS = 0; // nombre d'étudiants
    int N_PROJECTS = 0; // nombre de projets

    int MAX_ENCADREMENT = 3;  // valuers possibles : 4, 3, 2

    int[] prof_project = null;  // projectid -> profid
    TreeMap<String, Integer> prefs_student = new TreeMap<>(); // studentid_projectid -> pref

    String FILENAME_PROJECTS = "./data/projects.txt";
    String FILENAME_PREFERENCES = "./data/preferences.txt";

    // lecture de projects.txt
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FILENAME_PROJECTS), StandardCharsets.UTF_8))) {

      String line;
      int count = 0;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;
        String[] s = line.split("\\s+");
        if (count == 0) {
          // première ligne : N_PROJETS N_PROFS
          N_PROJECTS = Integer.parseInt(s[0]);
          N_PROFS = Integer.parseInt(s[1]);
          prof_project = new int[N_PROJECTS];
          count++;
          continue;
        }

        // stockage du couple (projet,prof) : prof_project[project_id] = prof_id
        prof_project[Integer.parseInt(s[0])] = Integer.parseInt(s[1]);
        count++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // lecture de preferences.txt
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FILENAME_PREFERENCES), StandardCharsets.UTF_8))) {

      String line;
      int count = 0;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;
        String[] s = line.split("\\s+");
        if (count == 0) {
          // première ligne : N_STUDENTS
          N_STUDENTS = Integer.parseInt(s[0]);
          count++;
          continue;
        }
        int mot1 = Integer.parseInt(s[0]); // studentid
        int mot2 = Integer.parseInt(s[1]); // projectid
        int mot3 = Integer.parseInt(s[2]); // preference
        String key = mot1 + "_" + mot2; // studentid_projectid
        // stockage de la clé studentid_projectid avec sa valeur preference
        prefs_student.put(key, mot3);
        count++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("Nombre de projets  : " + N_PROJECTS);
    System.out.println("Nombre de profs    : " + N_PROFS);
    System.out.println("Nombre d'étudiants : " + N_STUDENTS);

    // Création d'un solveurMI
    MPSolver solver = MPSolver.createSolver("SCIP");
    if (solver == null) {
      System.out.println("Impossible de créer le solveur SCIP");
    }

    // Partie 1
    MPObjective objective = solver.objective();

      // Variables + Fonctions Objectifs
    // Allocation d'un tableau de variables, une variable par projet par élève
      TreeMap<String, MPVariable> assigned = new TreeMap<String, MPVariable>();
    // Création d'une variable binaire (booléenne) pour chaque sommet, valant 1 si le projet est assigné, 0 sinon
    for (int i = 0; i < N_STUDENTS; i++) {
        for (int j = 0; j < N_PROJECTS; j++) {
            String key = i + "_" + j;
            if(prefs_student.containsKey(key)){
                assigned.put(key,solver.makeBoolVar("x" + i + "_" + j));

                objective.setCoefficient(assigned.get(key), prefs_student.get(key));
            }
        }
    }
      objective.setMaximization();

    // Contraintes

    // au plus 1 étudiant par projet
    for(int i = 0; i < N_PROJECTS; i++) {
      MPConstraint oneStudentPerProject = solver.makeConstraint(0,1,"project_"+ i);
      for(int j = 0; j < N_STUDENTS; j++) {
        String key = j + "_" + i;
        if(assigned.containsKey(key)){
          oneStudentPerProject.setCoefficient(assigned.get(key), 1);
        }
      }
    }
    // au moins 1 projet par étudiant
    for (int i = 0; i < N_STUDENTS; i++) {
      MPConstraint oneProjectPerStudent = solver.makeConstraint(1, 1, "student_" + i);
      for (int j = 0; j < N_PROJECTS; j++) {
        String key = i + "_" + j;
        if (assigned.containsKey(key)) {
          oneProjectPerStudent.setCoefficient(assigned.get(key), 1);
        }
      }
    }
    // un professeur ne peux pas avoir trop de projets
    for (int p = 0; p < N_PROFS; p++) {
      MPConstraint maxProjectsPerProf = solver.makeConstraint(0, MAX_ENCADREMENT, "prof_" + p);
    
      // Parcourir tous les projets de ce professeur
      for (int j = 0; j < N_PROJECTS; j++) {
        if (prof_project[j] == p) {
          // Pour tous les étudiants qui pourraient avoir ce projet
          for (int i = 0; i < N_STUDENTS; i++) {
            String key = i + "_" + j;
            if (assigned.containsKey(key)) {
              maxProjectsPerProf.setCoefficient(assigned.get(key), 1);
            }
          }
        }
      }
    }

    // Résolution
    MPSolver.ResultStatus resultStatus = solver.solve();
    TreeMap<String, Boolean> initialSolution = new TreeMap<>();


    if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
      System.out.println("\n=== PARTIE 1 - MAX_ENCADREMENT = " + MAX_ENCADREMENT + " ===");
      System.out.println("Somme maximale des préférences : " + (int)objective.value());
    
      try (BufferedWriter writer = new BufferedWriter(
        new FileWriter("solution_part1_max" + MAX_ENCADREMENT + ".txt"))) {
          for (int i = 0; i < N_STUDENTS; i++) {
            for (int j = 0; j < N_PROJECTS; j++) {
              String key = i + "_" + j;
              if (assigned.containsKey(key) && assigned.get(key).solutionValue() > 0.5) {
                writer.write(i + " " + j + "\n");
                // Partie 2 : Stocker solution initiale
                initialSolution.put(key, true);

                break;
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
} else {
    System.out.println("Pas de solution optimale trouvée. Status: " + resultStatus);
}


    // Partie 2
    System.out.println("\n=== PARTIE 2 ===");

    // Contrainte:interdire certains projet d'avoir affectation
    int[] unavailableProjects = {68, 123, 151};

    for (int unavailableProject : unavailableProjects) {
      MPConstraint unavailable = solver.makeConstraint(0, 0, "unavailable_" + unavailableProject);
    
      for (int i = 0; i < N_STUDENTS; i++) {
        String key = i + "_" + unavailableProject;
        if (assigned.containsKey(key)) {
          unavailable.setCoefficient(assigned.get(key), 1);
        }
    }
}

    // Objectif: Maximiser nb d'affectation conservée par rapport à la partie 1

    // Effacer ancien objectif
    objective.clear();

    for (String key : initialSolution.keySet()) {
      if (assigned.containsKey(key)) {
        // Coefficient = 1 pour chaque affectation présente dans la solution initiale
        objective.setCoefficient(assigned.get(key), 1);
      }
    }

    objective.setMaximization();

    // Résolution
    MPSolver.ResultStatus resultStatus2 = solver.solve();
    int nbConserved = (int)objective.value();

    if (resultStatus2 == MPSolver.ResultStatus.OPTIMAL) {
      System.out.println("Nombre d'affectations conservées : " + nbConserved);
      System.out.println("Nombre d'affectations modifiées : " + (N_STUDENTS - nbConserved));
    
      try (BufferedWriter writer = new BufferedWriter(
        new FileWriter("solution_part2_max" + MAX_ENCADREMENT + ".txt"))) {
          for (int i = 0; i < N_STUDENTS; i++) {
            for (int j = 0; j < N_PROJECTS; j++) {
              String key = i + "_" + j;
              if (assigned.containsKey(key) && assigned.get(key).solutionValue() > 0.5) {
                writer.write(i + " " + j + "\n");
                break;
              }
            }
          }
      } catch (IOException e) {
          e.printStackTrace();
    }
  } else {
      System.out.println("Pas de solution pour la Partie 2. Status: " + resultStatus2);
    }

    // Partie 3
    System.out.println("\n=== PARTIE 3 ===");
    
    // Effacer ancien objectif et rétablir l'objectif de la partie 1
    objective.clear();

    for (int i = 0; i < N_STUDENTS; i++) {
      for (int j = 0; j < N_PROJECTS; j++) {
        String key = i + "_" + j;
        if (assigned.containsKey(key)) {
          objective.setCoefficient(assigned.get(key), prefs_student.get(key));
        }
      }
    }

    objective.setMaximization();

    // Contrainte : maintenir exactement nbConserved affectations de la solution initiale
    MPConstraint keepConserved = solver.makeConstraint(nbConserved, nbConserved, "keepConserved");

    for (String key : initialSolution.keySet()) {
      if (assigned.containsKey(key)) {
        keepConserved.setCoefficient(assigned.get(key), 1);
      }
    }

    // Résolution
    MPSolver.ResultStatus resultStatus3 = solver.solve();

    if (resultStatus3 == MPSolver.ResultStatus.OPTIMAL) {
      System.out.println("Nombre d'affectations conservées : " + nbConserved);
      System.out.println("Somme maximale des préférences : " + (int)objective.value());
    
      // Sauvegarder la solution finale
      try (BufferedWriter writer = new BufferedWriter(
        new FileWriter("solution_part3_max" + MAX_ENCADREMENT + ".txt"))) {
        
        for (int i = 0; i < N_STUDENTS; i++) {
          for (int j = 0; j < N_PROJECTS; j++) {
            String key = i + "_" + j;
            if (assigned.containsKey(key) && assigned.get(key).solutionValue() > 0.5) {
              writer.write(i + " " + j + "\n");
              break;
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("Pas de solution pour la Partie 3. Status: " + resultStatus3);
    }

  }

}
