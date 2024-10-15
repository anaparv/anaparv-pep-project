package Service;
import DAO.AccountDAO;
import Model.Account;

public class AccountService {
    private AccountDAO accountDAO;

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    // Register a new account
    public Account registerAccount(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("Username cannot be empty");
        }

        if (password == null || password.length() < 4) {
            throw new Exception("Password must be at least 4 characters long");
        }

        // Check if the username already exists
        Account existingAccount = accountDAO.getAccountByUsername(username);
        if (existingAccount != null) {
            throw new Exception("Username is already taken");
        }

        // Create the account if validation passes
        Account newAccount = new Account(username, password);
        accountDAO.createAccount(newAccount.getUsername(), newAccount.getPassword());
        return accountDAO.getAccountByUsername(username); // Return the created account (including id)
    }

    // Login with an existing account
    public Account loginAccount(String username, String password) throws Exception {
        Account account = accountDAO.getAccountByUsername(username);
        if (account == null || !account.getPassword().equals(password)) {
            throw new Exception("Invalid username or password");
        }
        return account; // Successful login, return the account
    }
}
