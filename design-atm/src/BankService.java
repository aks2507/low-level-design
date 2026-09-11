public interface BankService {
    boolean authenticate(String cardNumber, int pin);

    int getBalance(String cardNumber);

    boolean debit(String cardNumber, int amount);
}
