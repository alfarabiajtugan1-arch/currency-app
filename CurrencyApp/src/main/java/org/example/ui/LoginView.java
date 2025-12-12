package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.MainApp;
import org.example.model.User;

public class LoginView {

    private final MainApp app;
    private final VBox root;

    public LoginView(MainApp app) {
        this.app = app;

        TextField loginField = new TextField();
        loginField.setPromptText("Логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        Button loginButton = new Button("Войти");
        Button registerButton = new Button("Регистрация");
        Label messageLabel = new Label();

        // ВОЙТИ
        loginButton.setOnAction(e -> {
            String login = loginField.getText();
            String pass = passwordField.getText();

            if (login == null || login.isBlank()) {
                messageLabel.setText("Введите логин.");
                return;
            }

            // ВРЕМЕННО: фейковый пользователь без БД
            // У тебя User(int id, String name, String email, String password)
            User user = new User(
                    1,
                    login,
                    login + "@test.local",
                    pass
            );

            messageLabel.setText("");
            app.showMainView(user);
        });

        // РЕГИСТРАЦИЯ
        registerButton.setOnAction(e -> app.showRegisterView());

        root = new VBox(10, loginField, passwordField, loginButton, registerButton, messageLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
    }

    public Parent getRoot() {
        return root;
    }
}
