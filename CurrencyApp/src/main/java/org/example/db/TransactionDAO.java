package org.example.db;

import org.example.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // --- СОХРАНИТЬ ТРАНЗАКЦИЮ В БД ---
    public void insert(Transaction tx) throws SQLException {
        String sql = "INSERT INTO transactions " +
                "(user_id, from_currency, to_currency, rate_date, amount, rate, result) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tx.getUserId());
            ps.setString(2, tx.getFromCurrency());
            ps.setString(3, tx.getToCurrency());
            ps.setString(4, tx.getRateDate().toString());
            ps.setDouble(5, tx.getAmount());
            ps.setDouble(6, tx.getRate());
            ps.setDouble(7, tx.getResult());

            ps.executeUpdate();

            // Лог, чтобы видеть, что запись реально ушла в БД
            System.out.println("[TX INSERT] user=" + tx.getUserId()
                    + ", " + tx.getFromCurrency() + "->" + tx.getToCurrency()
                    + ", amount=" + tx.getAmount()
                    + ", result=" + tx.getResult());
        }
    }

    // --- ЗАГРУЗИТЬ ИСТОРИЮ ПО ПОЛЬЗОВАТЕЛЮ ---
    public List<Transaction> findByUser(int userId, int limit) throws SQLException {
        String sql = "SELECT id, user_id, from_currency, to_currency, rate_date, amount, rate, result " +
                     "FROM transactions " +
                     "WHERE user_id = ? " +
                     "ORDER BY rate_date DESC, id DESC " +
                     "LIMIT ?";

        List<Transaction> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction();
                    tx.setId(rs.getInt("id"));
                    tx.setUserId(rs.getInt("user_id"));
                    tx.setFromCurrency(rs.getString("from_currency"));
                    tx.setToCurrency(rs.getString("to_currency"));
                    tx.setRateDate(LocalDate.parse(rs.getString("rate_date")));
                    tx.setAmount(rs.getDouble("amount"));
                    tx.setRate(rs.getDouble("rate"));
                    tx.setResult(rs.getDouble("result"));
                    list.add(tx);
                }
            }
        }

        return list;
    }
}
