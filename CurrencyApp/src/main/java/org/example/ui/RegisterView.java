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

public class RegisterView {

    private final MainApp app;
    private final VBox root;

    public RegisterView(MainApp app) {
        this.app = app;

        TextField nameField = new TextField();
        nameField.setPromptText("Имя");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Пароль");

        PasswordField pass2Field = new PasswordField();
        pass2Field.setPromptText("Повторите пароль");

        Button registerButton = new Button("Зарегистрироваться");
        Button backButton = new Button("Назад");
        Label messageLabel = new Label();

        registerButton.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String pass = passField.getText();
            String pass2 = pass2Field.getText();

            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                messageLabel.setText("Заполните все поля.");
                return;
            }
            if (!pass.equals(pass2)) {
                messageLabel.setText("Пароли не совпадают.");
                return;
            }

            // TODO: здесь можно добавить сохранение в БД
            messageLabel.setText("Регистрация успешна. Теперь войдите.");
            app.showLoginView();
        });

        backButton.setOnAction(e -> app.showLoginView());

        root = new VBox(10,
                nameField, emailField, passField, pass2Field,
                registerButton, backButton, messageLabel
        );
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
    }

    public Parent getRoot() {
        return root;
    }
}
