package org.example.db;

import org.example.model.DemoAccount;

import java.util.HashMap;
import java.util.Map;

public class DemoAccountDAO {

    // userId -> DemoAccount
    private static final Map<Integer, DemoAccount> storage = new HashMap<>();

    public DemoAccount findByUser(int userId) {
        return storage.get(userId);
    }

    public void save(DemoAccount account) {
        storage.put(account.getUserId(), account);
    }
}
