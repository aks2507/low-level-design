import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

public class ATMDesignDemo {


    static class InMemoryBankService implements BankService {
        private static class BankAccount {
            private final int pin;
            private int balance;

            private BankAccount(int pin, int balance) {
                this.pin = pin;
                this.balance = balance;
            }
        }

        private final Map<String, BankAccount> accounts = new HashMap<>();

        public void addAccount(String cardNumber, int pin, int openingBalance) {
            if (openingBalance < 0) {
                throw new IllegalArgumentException("Opening balance cannot be negative");
            }
            accounts.put(cardNumber, new BankAccount(pin, openingBalance));
        }

        @Override
        public boolean authenticate(String cardNumber, int pin) {
            BankAccount account = accounts.get(cardNumber);
            return account != null && account.pin == pin;
        }

        @Override
        public int getBalance(String cardNumber) {
            return getAccount(cardNumber).balance;
        }

        @Override
        public boolean debit(String cardNumber, int amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }

            BankAccount account = getAccount(cardNumber);
            if (account.balance < amount) {
                return false;
            }

            account.balance -= amount;
            return true;
        }

        private BankAccount getAccount(String cardNumber) {
            BankAccount account = accounts.get(cardNumber);
            if (account == null) {
                throw new ATMException("Unknown card");
            }
            return account;
        }
    }

    static class CashDispenser {
        private final NavigableMap<Integer, Integer> inventory =
                new TreeMap<>(Comparator.reverseOrder());

        CashDispenser(Map<Integer, Integer> initialInventory) {
            for (Map.Entry<Integer, Integer> entry : initialInventory.entrySet()) {
                int denomination = entry.getKey();
                int count = entry.getValue();
                if (denomination <= 0 || count < 0) {
                    throw new IllegalArgumentException("Invalid cash inventory");
                }
                inventory.put(denomination, count);
            }
        }

        public Optional<Map<Integer, Integer>> planDispense(int amount) {
            if (amount <= 0) {
                return Optional.empty();
            }

            List<Integer> denominations = new ArrayList<>(inventory.keySet());
            Map<Integer, Integer> plan = new LinkedHashMap<>();

            if (!findPlan(denominations, 0, amount, plan)) {
                return Optional.empty();
            }
            return Optional.of(new LinkedHashMap<>(plan));
        }

        private boolean findPlan(
                List<Integer> denominations,
                int index,
                int remaining,
                Map<Integer, Integer> plan) {

            if (remaining == 0) {
                return true;
            }
            if (index == denominations.size()) {
                return false;
            }

            int denomination = denominations.get(index);
            int available = inventory.get(denomination);
            int maximumUsable = Math.min(available, remaining / denomination);

            // Try more high-value notes first, but backtrack if they block
            // an otherwise valid combination.
            for (int count = maximumUsable; count >= 0; count--) {
                if (count == 0) {
                    plan.remove(denomination);
                } else {
                    plan.put(denomination, count);
                }

                int newRemaining = remaining - count * denomination;
                if (findPlan(denominations, index + 1, newRemaining, plan)) {
                    return true;
                }
            }

            plan.remove(denomination);
            return false;
        }

        public void dispense(Map<Integer, Integer> plan) {
            // Validate the entire plan before mutating inventory.
            for (Map.Entry<Integer, Integer> entry : plan.entrySet()) {
                int available = inventory.getOrDefault(entry.getKey(), 0);
                if (entry.getValue() <= 0 || available < entry.getValue()) {
                    throw new ATMException("Cash inventory changed before dispensing");
                }
            }

            for (Map.Entry<Integer, Integer> entry : plan.entrySet()) {
                inventory.put(entry.getKey(), inventory.get(entry.getKey()) - entry.getValue());
            }
        }

        public int getAvailableCash() {
            int total = 0;
            for (Map.Entry<Integer, Integer> entry : inventory.entrySet()) {
                total += entry.getKey() * entry.getValue();
            }
            return total;
        }

        public Map<Integer, Integer> getInventory() {
            return new LinkedHashMap<>(inventory);
        }
    }

    static class ATM {
        private final BankService bankService;
        private final CashDispenser cashDispenser;

        private ATMState state = ATMState.IDLE;
        private String currentCardNumber;

        ATM(BankService bankService, CashDispenser cashDispenser) {
            this.bankService = bankService;
            this.cashDispenser = cashDispenser;
        }

        public void insertCard(String cardNumber) {
            requireState(ATMState.IDLE);
            if (cardNumber == null || cardNumber.isBlank()) {
                throw new IllegalArgumentException("Card number is required");
            }

            currentCardNumber = cardNumber;
            state = ATMState.CARD_INSERTED;
        }

        public boolean enterPin(int pin) {
            requireState(ATMState.CARD_INSERTED);

            if (!bankService.authenticate(currentCardNumber, pin)) {
                return false;
            }

            state = ATMState.AUTHENTICATED;
            return true;
        }

        public int checkBalance() {
            requireState(ATMState.AUTHENTICATED);
            return bankService.getBalance(currentCardNumber);
        }

        public Map<Integer, Integer> withdraw(int amount) {
            requireState(ATMState.AUTHENTICATED);
            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be positive");
            }

            Map<Integer, Integer> plan = cashDispenser.planDispense(amount)
                    .orElseThrow(() -> new ATMException(
                            "ATM cannot dispense the requested amount with available notes"));

            // Check the physical-cash constraint before changing bank balance.
            if (!bankService.debit(currentCardNumber, amount)) {
                throw new ATMException("Insufficient account balance");
            }

            // Under the single-session assumption, the prevalidated plan cannot
            // be changed by another withdrawal between planning and dispensing.
            cashDispenser.dispense(plan);
            return plan;
        }

        public void ejectCard() {
            if (state == ATMState.IDLE) {
                throw new ATMException("No card to eject");
            }

            currentCardNumber = null;
            state = ATMState.IDLE;
        }

        public ATMState getState() {
            return state;
        }

        private void requireState(ATMState expected) {
            if (state != expected) {
                throw new ATMException(
                        "Operation requires state " + expected + ", but ATM is " + state);
            }
        }
    }

    public static void main(String[] args) {
        InMemoryBankService bankService = new InMemoryBankService();
        bankService.addAccount("CARD-123", 4321, 1_000);
        bankService.addAccount("CARD-LOW", 1234, 50);

        CashDispenser cashDispenser = new CashDispenser(Map.of(
                100, 3,
                50, 2,
                20, 5,
                10, 10));

        ATM atm = new ATM(bankService, cashDispenser);

        expectATMException(atm::checkBalance);

        atm.insertCard("CARD-123");
        assertEquals(ATMState.CARD_INSERTED, atm.getState(), "card insertion state");
        assertFalse(atm.enterPin(1111), "wrong PIN should fail");
        assertTrue(atm.enterPin(4321), "correct PIN should authenticate");
        assertEquals(1_000, atm.checkBalance(), "opening balance");

        Map<Integer, Integer> dispensed = atm.withdraw(380);
        assertEquals(Map.of(100, 3, 50, 1, 20, 1, 10, 1), dispensed,
                "dispense plan");
        assertEquals(620, atm.checkBalance(), "balance after withdrawal");
        assertEquals(220, cashDispenser.getAvailableCash(), "ATM cash after withdrawal");

        atm.ejectCard();
        assertEquals(ATMState.IDLE, atm.getState(), "state after eject");

        int cashBeforeRejectedWithdrawal = cashDispenser.getAvailableCash();
        atm.insertCard("CARD-LOW");
        assertTrue(atm.enterPin(1234), "second card should authenticate");
        expectATMException(() -> atm.withdraw(100));
        assertEquals(50, atm.checkBalance(), "rejected withdrawal must not change balance");
        assertEquals(cashBeforeRejectedWithdrawal, cashDispenser.getAvailableCash(),
                "rejected withdrawal must not change ATM cash");
        atm.ejectCard();

        // Greedy-only dispensing would try 50 + 10 and fail. The planner
        // correctly backtracks to 20 + 20 + 20.
        CashDispenser trickyInventory = new CashDispenser(Map.of(
                50, 1,
                20, 3,
                10, 0));
        Map<Integer, Integer> backtrackedPlan = trickyInventory.planDispense(60)
                .orElseThrow(() -> new AssertionError("Expected a valid cash plan"));
        assertEquals(Map.of(20, 3), backtrackedPlan, "backtracking cash plan");

        CashDispenser impossibleInventory = new CashDispenser(Map.of(50, 1, 20, 1));
        assertTrue(impossibleInventory.planDispense(60).isEmpty(),
                "an amount that cannot be formed should be rejected");

        System.out.println("All tests passed.");
        System.out.println("Dispensed notes: " + dispensed);
        System.out.println("Remaining account balance: " + bankService.getBalance("CARD-123"));
        System.out.println("ATM cash remaining: " + cashDispenser.getAvailableCash());
    }

    private static void expectATMException(Runnable operation) {
        try {
            operation.run();
            throw new AssertionError("Expected ATMException");
        } catch (ATMException expected) {
            // Expected.
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean value, String message) {
        assertTrue(!value, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}