package ch.heig.sio.lab3.groupX;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

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
    TreeMap<String, Integer> prefs_Student = new TreeMap<>(); // studentid_projectid -> pref

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
        prefs_Student.put(key, mot3);
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


    // Partie 2


    // Partie 3

  }

}
