package org.example.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.example.MainApp;
import org.example.data.ExchangeRateService;
import org.example.db.TransactionDAO;
import org.example.db.DemoAccountDAO;
import org.example.model.Transaction;
import org.example.model.User;
import org.example.model.DemoAccount;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainView {

    private final BorderPane root;
    private final MainApp app;
    private final User user;

    // Конвертер
    private final ComboBox<String> fromCurrencyBox;
    private final ComboBox<String> toCurrencyBox;
    private final DatePicker datePicker;
    private final TextField amountField;
    private final Label resultLabel;

    // DEMO-СЧЁТ
    private final Label demoBalanceLabel;
    private final DemoAccountDAO demoAccountDAO = new DemoAccountDAO();
    private DemoAccount demoAccount;

    // Графики
    private final DatePicker fromDatePicker;
    private final DatePicker toDatePicker;
    private final Button showChartButton;
    private final LineChart<String, Number> rateChart;
    private final CandleStickChart candleChart;

    // История
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final TableView<Transaction> historyTable = new TableView<>();
    private final ObservableList<Transaction> historyData = FXCollections.observableArrayList();
    private final ComboBox<String> historyFilter = new ComboBox<>();
    private List<Transaction> allHistory = new ArrayList<>();

    private final ExchangeRateService rateService = new ExchangeRateService();
    private final DateTimeFormatter dfShort = DateTimeFormatter.ofPattern("dd.MM");

    // последние данные конвертации для кнопки "Обменять"
    private String lastFrom;
    private String lastTo;
    private LocalDate lastDate;
    private double lastAmount;
    private double lastRate;
    private double lastResult;
    private boolean hasLastConversion = false;

    // -------------------------------------------------------------
    //                        КОНСТРУКТОР
    // -------------------------------------------------------------
    public MainView(MainApp app, User user) {
        this.app = app;
        this.user = user;

        root = new BorderPane();
        root.setPadding(new Insets(0));

        // ---------- ШАПКА ----------
        Label titleLabel = new Label("Currency App");
        titleLabel.getStyleClass().add("header-title");

        Label userLabel = new Label("Пользователь: " + user.getName());
        userLabel.getStyleClass().add("header-user");

        Button logoutButton = new Button("Выйти");
        logoutButton.getStyleClass().add("primary-button");
        logoutButton.setOnAction(e -> onLogout());

        VBox headerText = new VBox(2, titleLabel, userLabel);

        HBox headerBar = new HBox(headerText, logoutButton);
        headerBar.setPadding(new Insets(10, 24, 10, 24));
        headerBar.setSpacing(20);
        headerBar.setAlignment(Pos.CENTER_RIGHT);
        headerBar.getStyleClass().add("header-bar");

        root.setTop(headerBar);

        // ---------- СПИСОК ВАЛЮТ ----------
        var currencies = FXCollections.observableArrayList(
                "EUR", "USD", "GBP", "JPY",
                "AUD", "BGN", "BRL", "CAD", "CHF",
                "CNY", "CZK", "DKK", "HKD", "HUF",
                "IDR", "ILS", "INR", "ISK", "KRW",
                "MXN", "MYR", "NOK", "NZD", "PHP",
                "PLN", "RON", "SEK", "SGD", "THB",
                "TRY", "ZAR"
        );

        // ---------- КОНВЕРТЕР ----------
        fromCurrencyBox = new ComboBox<>(currencies);
        fromCurrencyBox.setValue("EUR");

        toCurrencyBox = new ComboBox<>(currencies);
        toCurrencyBox.setValue("USD");

        datePicker = new DatePicker(LocalDate.now());
        amountField = new TextField("100");
        amountField.setPrefWidth(120);

        Button convertButton = new Button("Конвертировать");
        convertButton.getStyleClass().add("primary-button");

        Button exchangeButton = new Button("Обменять (демо-счёт)");
        exchangeButton.getStyleClass().add("primary-button");

        resultLabel = new Label("Выберите валюты, дату и нажмите «Конвертировать».");
        resultLabel.getStyleClass().add("result-label");

        GridPane convertForm = new GridPane();
        convertForm.setHgap(12);
        convertForm.setVgap(10);
        convertForm.add(new Label("Из валюты:"), 0, 0);
        convertForm.add(fromCurrencyBox, 1, 0);
        convertForm.add(new Label("В валюту:"), 0, 1);
        convertForm.add(toCurrencyBox, 1, 1);
        convertForm.add(new Label("Дата курса:"), 0, 2);
        convertForm.add(datePicker, 1, 2);
        convertForm.add(new Label("Сумма:"), 0, 3);
        convertForm.add(amountField, 1, 3);

        HBox convertButtons = new HBox(10, convertButton, exchangeButton);
        convertButtons.setAlignment(Pos.CENTER_LEFT);
        convertForm.add(convertButtons, 1, 4);

        Label convertTitle = new Label("Конвертер валют");
        convertTitle.getStyleClass().add("card-title");

        VBox converterCard = new VBox(10, convertTitle, convertForm, resultLabel);
        converterCard.getStyleClass().add("card");

        // ---------- DEMO-СЧЁТ ----------
        demoBalanceLabel = new Label();
        demoBalanceLabel.getStyleClass().add("result-label");

        Button demoPlusButton = new Button("+1000 USD");
        demoPlusButton.getStyleClass().add("primary-button");
        Button demoResetButton = new Button("Сброс демо");

        demoPlusButton.setOnAction(e -> {
            demoAccount.addBalance("USD", 1000.0);
            demoAccountDAO.save(demoAccount);
            updateDemoBalanceLabel();
        });

        demoResetButton.setOnAction(e -> {
            demoAccount.resetToDefault();
            demoAccountDAO.save(demoAccount);
            updateDemoBalanceLabel();
        });

        HBox demoButtons = new HBox(10, demoPlusButton, demoResetButton);
        demoButtons.setAlignment(Pos.CENTER_LEFT);

        Label demoTitle = new Label("Демо-счёт");
        demoTitle.getStyleClass().add("card-title");

        VBox demoCard = new VBox(8, demoTitle, demoBalanceLabel, demoButtons);
        demoCard.getStyleClass().add("card");

        // ---------- ИСТОРИЯ ----------
        historyTable.setItems(historyData);
        historyTable.setPrefHeight(200);

        TableColumn<Transaction, String> colDate = new TableColumn<>("Дата");
        colDate.setCellValueFactory(new PropertyValueFactory<>("rateDate"));

        TableColumn<Transaction, String> colPair = new TableColumn<>("Пара");
        colPair.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getFromCurrency() + "/" + cd.getValue().getToCurrency()
                ));

        TableColumn<Transaction, String> colAmount = new TableColumn<>("Сумма");
        colAmount.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        String.format(Locale.US, "%.2f", cd.getValue().getAmount())
                ));

        TableColumn<Transaction, String> colResult = new TableColumn<>("Результат");
        colResult.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        String.format(Locale.US, "%.2f", cd.getValue().getResult())
                ));

        historyTable.getColumns().addAll(colDate, colPair, colAmount, colResult);

        historyFilter.getItems().addAll("Все", "За неделю", "За месяц");
        historyFilter.setValue("Все");

        Label historyTitle = new Label("История транзакций");
        historyTitle.getStyleClass().add("card-title");
        Label periodLabel = new Label("Период:");

        HBox historyHeader = new HBox(10, historyTitle, periodLabel, historyFilter);
        historyHeader.setAlignment(Pos.CENTER_LEFT);

        VBox historyCard = new VBox(10, historyHeader, historyTable);
        historyCard.getStyleClass().add("card");

        historyFilter.setOnAction(e -> applyHistoryFilter());

        // ---------- ГРАФИКИ ----------
        fromDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        toDatePicker = new DatePicker(LocalDate.now());
        showChartButton = new Button("Показать график");
        showChartButton.getStyleClass().add("primary-button");

        GridPane chartForm = new GridPane();
        chartForm.setHgap(10);
        chartForm.setVgap(10);
        chartForm.add(new Label("Период от:"), 0, 0);
        chartForm.add(fromDatePicker, 1, 0);
        chartForm.add(new Label("до:"), 2, 0);
        chartForm.add(toDatePicker, 3, 0);
        chartForm.add(showChartButton, 1, 1, 2, 1);

        CategoryAxis xAxisLine = new CategoryAxis();
        xAxisLine.setLabel("Дата");
        xAxisLine.setTickLabelRotation(-60);

        NumberAxis yAxisLine = new NumberAxis();
        yAxisLine.setLabel("Курс");
        yAxisLine.setForceZeroInRange(false);

        rateChart = new LineChart<>(xAxisLine, yAxisLine);
        rateChart.setTitle("Линейный график");
        rateChart.setAnimated(false);
        rateChart.setLegendVisible(false);
        rateChart.setPrefHeight(220);

        NumberAxis xAxisCandle = new NumberAxis();
        NumberAxis yAxisCandle = new NumberAxis();
        xAxisCandle.setLabel("Дата (индекс)");
        yAxisCandle.setLabel("Курс");
        yAxisCandle.setForceZeroInRange(false);

        candleChart = new CandleStickChart(xAxisCandle, yAxisCandle);
        candleChart.setTitle("Японские свечи");
        candleChart.setPrefHeight(220);

        Label chartTitle = new Label("История курса");
        chartTitle.getStyleClass().add("card-title");

        VBox chartsBox = new VBox(10, chartTitle, chartForm, rateChart, candleChart);
        chartsBox.getStyleClass().add("card");
        VBox.setVgrow(rateChart, Priority.ALWAYS);
        VBox.setVgrow(candleChart, Priority.ALWAYS);

        // ---------- ЛЕВАЯ КОЛОНКА ----------
        VBox leftColumn = new VBox(15, demoCard, converterCard, historyCard);

        // ---------- ЦЕНТР ----------
        HBox centerBox = new HBox(20, leftColumn, chartsBox);
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.TOP_CENTER);

        root.setCenter(centerBox);

        // обработчики
        convertButton.setOnAction(e -> onConvert());
        exchangeButton.setOnAction(e -> onExchange());
        showChartButton.setOnAction(e -> onShowChart());

        // загрузка данных
        loadHistoryFromDb();
        loadDemoAccount();
    }

    public BorderPane getRoot() {
        return root;
    }

    // ---------- DEMO-СЧЁТ ----------
    private void loadDemoAccount() {
        demoAccount = demoAccountDAO.findByUser(user.getId());
        if (demoAccount == null) {
            demoAccount = new DemoAccount(user.getId());
            demoAccount.resetToDefault(); // например, USD 10000
            demoAccountDAO.save(demoAccount);
        }
        updateDemoBalanceLabel();
    }

    private void updateDemoBalanceLabel() {
        StringBuilder sb = new StringBuilder("Баланс демо: ");
        boolean first = true;
        for (var entry : demoAccount.getBalances().entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(entry.getKey()).append(" ")
              .append(String.format(Locale.US, "%.2f", entry.getValue()));
        }
        demoBalanceLabel.setText(sb.toString());
    }

    // ---------- ИСТОРИЯ ИЗ БД + ФИЛЬТР ----------
    private void loadHistoryFromDb() {
        try {
            allHistory = transactionDAO.findByUser(user.getId(), 200);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки истории транзакций: " + e.getMessage());
            allHistory = new ArrayList<>();
        }
        applyHistoryFilter();
    }

    private void applyHistoryFilter() {
        historyData.clear();
        if (allHistory == null) return;

        String mode = historyFilter.getValue();
        LocalDate now = LocalDate.now();
        LocalDate fromDate = null;

        if ("За неделю".equals(mode)) {
            fromDate = now.minusDays(7);
        } else if ("За месяц".equals(mode)) {
            fromDate = now.minusMonths(1);
        }

        for (Transaction tx : allHistory) {
            if (fromDate != null) {
                if (tx.getRateDate() == null) continue;
                if (tx.getRateDate().isBefore(fromDate)) continue;
            }
            historyData.add(tx);
        }
    }

    // ---------- ВЫХОД ----------
    private void onLogout() {
        Platform.exit(); // просто закрыть приложение
    }

    // ---------- КОНВЕРТАЦИЯ (с проверкой суммы + try/catch) ----------
    private void onConvert() {
        try {
            String from = fromCurrencyBox.getValue();
            String to = toCurrencyBox.getValue();
            LocalDate date = datePicker.getValue();

            if (from == null || to == null) {
                resultLabel.setText("Выберите обе валюты.");
                return;
            }

            if (date != null && date.isAfter(LocalDate.now())) {
                date = LocalDate.now();
            }

            // Проверка суммы
            String textAmount = amountField.getText().trim();
            if (textAmount.isEmpty()) {
                resultLabel.setText("Введите сумму.");
                return;
            }

            double amount = Double.parseDouble(textAmount.replace(",", "."));
            if (amount <= 0) {
                resultLabel.setText("Сумма должна быть больше 0.");
                return;
            }

            resultLabel.setText("Загружаю курс...");

            double finalAmount = amount;
            LocalDate finalDate = date;

            new Thread(() -> {
                try {
                    double rate = rateService.getRate(from, to, finalDate);
                    double result = finalAmount * rate;

                    // сохраняем данные последней конвертации
                    lastFrom = from;
                    lastTo = to;
                    lastAmount = finalAmount;
                    lastRate = rate;
                    lastResult = result;
                    lastDate = finalDate != null ? finalDate : LocalDate.now();
                    hasLastConversion = true;

                    String text = String.format(
                            Locale.US,
                            "%.2f %s = %.2f %s (курс %.4f, дата %s)",
                            finalAmount, from, result, to, rate,
                            (finalDate != null ? finalDate : LocalDate.now())
                    );

                    Platform.runLater(() -> resultLabel.setText(text));

                } catch (Exception apiEx) {
                    apiEx.printStackTrace();
                    Platform.runLater(() ->
                            resultLabel.setText("Ошибка при загрузке курса: " + apiEx.getMessage())
                    );
                }
            }).start();

        } catch (NumberFormatException ex) {
            resultLabel.setText("Введите корректное число.");
        } catch (Exception ex) {
            resultLabel.setText("Произошла ошибка: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ---------- ОБМЕН ПО ДЕМО-СЧЁТУ (с проверками + try/catch) ----------
    private void onExchange() {
        try {
            if (!hasLastConversion) {
                resultLabel.setText("Сначала нажмите «Конвертировать».");
                return;
            }

            if (lastFrom == null || lastTo == null) {
                resultLabel.setText("Нет данных для обмена.");
                return;
            }

            if (lastAmount <= 0) {
                resultLabel.setText("Сумма должна быть больше 0.");
                return;
            }

            if (lastFrom.equals(lastTo)) {
                resultLabel.setText("Выбраны одинаковые валюты, обмен не требуется.");
                return;
            }

            // проверяем, хватает ли средств
            boolean ok = demoAccount.subtractIfEnough(lastFrom, lastAmount);
            if (!ok) {
                resultLabel.setText("Недостаточно средств на демо-счёте в " + lastFrom);
                return;
            }

            // зачисляем валюту назначения
            demoAccount.addBalance(lastTo, lastResult);
            demoAccountDAO.save(demoAccount);
            updateDemoBalanceLabel();

            // записываем в историю
            Transaction tx = new Transaction();
            tx.setUserId(user.getId());
            tx.setFromCurrency(lastFrom);
            tx.setToCurrency(lastTo);
            tx.setRateDate(lastDate);
            tx.setAmount(lastAmount);
            tx.setRate(lastRate);
            tx.setResult(lastResult);

            try {
                transactionDAO.insert(tx);
                loadHistoryFromDb();
            } catch (Exception dbEx) {
                System.err.println("Не удалось сохранить транзакцию: " + dbEx.getMessage());
            }

            resultLabel.setText("Обмен выполнен: " +
                    String.format(Locale.US,
                            "%.2f %s → %.2f %s (курс %.4f)",
                            lastAmount, lastFrom, lastResult, lastTo, lastRate));

        } catch (Exception ex) {
            resultLabel.setText("Ошибка при обмене: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ---------- ГРАФИКИ (ДЕМО, БЕЗ API) ----------
    private void onShowChart() {
        try {
            String from = fromCurrencyBox.getValue();
            String to = toCurrencyBox.getValue();
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();

            if (from == null || to == null) {
                resultLabel.setText("Выберите валюты для графика.");
                return;
            }

            if (fromDate == null || toDate == null) {
                resultLabel.setText("Выберите период для графика.");
                return;
            }

            if (toDate.isBefore(fromDate)) {
                resultLabel.setText("Дата 'до' не может быть раньше даты 'от'.");
                return;
            }

            if (fromDate.isAfter(LocalDate.now())) {
                fromDate = LocalDate.now().minusDays(7);
            }
            if (toDate.isAfter(LocalDate.now())) {
                toDate = LocalDate.now();
            }

            ObservableList<XYChart.Series<String, Number>> rateData = rateChart.getData();
            if (rateData == null) {
                rateData = FXCollections.observableArrayList();
                rateChart.setData(rateData);
            } else {
                rateData.clear();
            }

            ObservableList<XYChart.Series<Number, Number>> candleData = candleChart.getData();
            if (candleData == null) {
                candleData = FXCollections.observableArrayList();
                candleChart.setData(candleData);
            } else {
                candleData.clear();
            }

            Map<LocalDate, Double> history = new LinkedHashMap<>();
            Random random = new Random();

            double rate = 0.8 + random.nextDouble() * 0.4;
            double trend = (random.nextDouble() - 0.5) / 200.0;

            for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
                double dailyNoise = (random.nextDouble() - 0.5) * 0.04;
                double change = 1.0 + trend + dailyNoise;

                rate *= change;

                if (rate < 0.3) rate = 0.3;
                if (rate > 5.0) rate = 5.0;

                history.put(d, rate);
            }

            if (history.isEmpty()) {
                resultLabel.setText("Нет данных за выбранный период (демо).");
                return;
            }

            List<LocalDate> dates = new ArrayList<>(history.keySet());
            dates.sort(Comparator.naturalOrder());

            XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
            lineSeries.setName(from + "/" + to + " (demo)");

            int size = dates.size();
            int step = 1;
            if (size > 40) step = 3;
            else if (size > 25) step = 2;

            for (int i = 0; i < size; i++) {
                LocalDate d = dates.get(i);
                double r = history.get(d);
                String label = (i % step == 0) ? d.format(dfShort) : "";
                lineSeries.getData().add(new XYChart.Data<>(label, r));
            }

            rateData.add(lineSeries);

            XYChart.Series<Number, Number> candleSeries = buildCandleSeries(dates, history);
            candleData.add(candleSeries);

            resultLabel.setText("Показаны демо-графики для периода " +
                    fromDate + " .. " + toDate + " (без API).");

        } catch (Exception ex) {
            ex.printStackTrace();
            resultLabel.setText("Ошибка при построении графика: " + ex.getMessage());
        }
    }

    // ---------- ПОСТРОЕНИЕ СВЕЧЕЙ ----------
    private XYChart.Series<Number, Number> buildCandleSeries(
            List<LocalDate> dates,
            Map<LocalDate, Double> history
    ) {
        NumberAxis xAxis = (NumberAxis) candleChart.getXAxis();
        int n = dates.size();

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(Math.max(0, n - 1));
        xAxis.setTickUnit(1);
        xAxis.setMinorTickCount(0);

        List<String> labels = new ArrayList<>();
        for (LocalDate d : dates) {
            labels.add(d.format(dfShort));
        }

        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                int i = value.intValue();
                if (i >= 0 && i < labels.size()) return labels.get(i);
                return "";
            }

            @Override
            public Number fromString(String s) {
                return 0;
            }
        });

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Свечной график");

        Double prevClose = null;
        Random r = new Random();

        for (int i = 0; i < n; i++) {
            LocalDate d = dates.get(i);

            double close;
            if (prevClose == null) {
                close = history.get(d);
            } else {
                close = prevClose * (1.0 + (r.nextDouble() - 0.5) * 0.04);
            }

            double open = (prevClose == null) ? close : prevClose;

            double max = Math.max(open, close);
            double min = Math.min(open, close);

            double high = max * (1.0 + r.nextDouble() * 0.01);
            double low  = min * (1.0 - r.nextDouble() * 0.01);

            CandleStickChart.OHLC ohlc =
                    new CandleStickChart.OHLC(open, close, high, low);

            XYChart.Data<Number, Number> data =
                    new XYChart.Data<>(i, close, ohlc);

            series.getData().add(data);
            prevClose = close;
        }

        return series;
    }
}
