package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.ui.LoginView;
import org.example.ui.MainView;
import org.example.ui.RegisterView;

import java.net.URL;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Currency App");

        showLoginView();   // сначала окно логина
        primaryStage.show();
    }

    // ---------- ОКНО ВХОДА ----------
    public void showLoginView() {
        LoginView view = new LoginView(this);
        Scene scene = new Scene(view.getRoot(), 900, 600);

        URL css = getClass().getResource("/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.out.println("styles.css NOT FOUND");
        }

        primaryStage.setScene(scene);
    }

    public void showLogin() {
        showLoginView();
    }

    // ---------- ОКНО РЕГИСТРАЦИИ ----------
    public void showRegisterView() {
        RegisterView view = new RegisterView(this);
        Scene scene = new Scene(view.getRoot(), 900, 600);

        URL css = getClass().getResource("/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.out.println("styles.css NOT FOUND");
        }

        primaryStage.setScene(scene);
    }

    // ---------- ГЛАВНОЕ ОКНО ----------
    public void showMainView(User user) {
        MainView view = new MainView(this, user);
        Scene scene = new Scene(view.getRoot(), 1200, 700);

        URL css = getClass().getResource("/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.out.println("styles.css NOT FOUND");
        }

        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
