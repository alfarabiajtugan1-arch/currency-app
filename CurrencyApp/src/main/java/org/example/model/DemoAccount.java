package org.example.model;

import java.util.HashMap;
import java.util.Map;

public class DemoAccount {

    private int userId;
    // мультивалютный баланс: "USD" -> 10000, "EUR" -> 0 и т.д.
    private Map<String, Double> balances = new HashMap<>();

    public DemoAccount(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public Map<String, Double> getBalances() {
        return balances;
    }

    public double getBalance(String currency) {
        return balances.getOrDefault(currency, 0.0);
    }

    public void setBalance(String currency, double amount) {
        balances.put(currency, amount);
    }

    public void addBalance(String currency, double delta) {
        double current = getBalance(currency);
        balances.put(currency, current + delta);
    }

    public boolean subtractIfEnough(String currency, double amount) {
        double current = getBalance(currency);
        if (current + 1e-9 < amount) {
            return false;
        }
        balances.put(currency, current - amount);
        return true;
    }

    public void resetToDefault() {
        balances.clear();
        balances.put("USD", 10_000.0); // стартовый демо-баланс
    }
}
