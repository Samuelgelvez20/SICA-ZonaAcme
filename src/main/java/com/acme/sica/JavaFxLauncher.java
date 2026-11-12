package com.acme.sica;

import com.acme.sica.ui.javafx.SicaApp;
import javafx.application.Application;

/**
 * Launcher wrapper para ejecutar la interfaz JavaFX moderna de SICA
 * sin requerir configuración compleja de module-path.
 */
public class JavaFxLauncher {

    public static void main(String[] args) {
        Application.launch(SicaApp.class, args);
    }
}
