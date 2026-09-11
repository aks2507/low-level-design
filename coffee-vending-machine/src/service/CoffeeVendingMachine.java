package service;

import models.Beverage;
import models.DispenseResult;
import models.IngredientInventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CoffeeVendingMachine {
    private static final Set<Integer> SUPPORTED_COINS = Set.of(25, 50, 100, 200);

    private final Map<String, Beverage> menuByCode = new HashMap<>();
    private final IngredientInventory inventory;
    private Beverage selected;
    private int balance;


    public CoffeeVendingMachine(Collection<Beverage> menu, IngredientInventory inventory) {
        for (Beverage beverage : menu) {
            menuByCode.put(beverage.code(), beverage);
        }
        this.inventory = inventory;
    }

    public void select(String code) {
        if (selected != null) {
            throw new IllegalStateException("Cancel or dispense currect selection first");
        }

        Beverage beverage = menuByCode.get(code);
        if (beverage == null) {
            throw new IllegalArgumentException("Beverage code unknown: " + code);
        }

        if (!inventory.canPrepare(beverage.recipe())) {
            throw new IllegalStateException("Beverage unavailable: " + beverage.name());
        }
        selected = beverage;
    }

    public void insertCoin(int cents) {
        if (selected == null) {
            throw new IllegalStateException("Select beverage first");
        }

        if (!SUPPORTED_COINS.contains(cents)) {
            throw new IllegalArgumentException("Unsupported denomination: " + cents);
        }
        balance += cents;
    }

    public DispenseResult dispense() {
        if (selected == null) {
            throw new IllegalStateException("No beverage selected");
        }

        if (balance < selected.price()) {
            throw new IllegalStateException("Insufficient payment");
        }

        inventory.consume(selected.recipe());
        DispenseResult result = new DispenseResult(selected, selected.price(), balance - selected.price());

        clearSession();
        return result;
    }

    public int cancel() {
        int refund = balance;
        clearSession();
        return refund;
    }

    public void clearSession() {
        selected = null;
        balance = 0;
    }
}
