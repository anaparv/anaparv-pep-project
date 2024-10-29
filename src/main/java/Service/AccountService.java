package Service;
import DAO.AccountDAO;
import Model.Account;
import java.sql.*;

public class AccountService {
    private AccountDAO accountDAO;

    public AccountService(AccountDAO accountDAO) {
        accountDAO = new AccountDAO();
    }

    public Account registerAccount(Account account) throws SQLException {
        // Validate the username and password
        if (account.getUsername() == null || account.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be blank.");
        }
    
        // Validate the password
        if (account.getPassword() == null || account.getPassword().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long.");
        }
    
        // Check if the username already exists using the DAO method
        if (accountDAO.doesUsernameExist(account.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
    
        // Create the account using AccountDAO
        return accountDAO.createAccount(account);
    }

    public boolean doesUsernameExist(String username) throws SQLException {
        return accountDAO.doesUsernameExist(username);
    }

    public Account login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be blank.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long.");
        }

        // Check login using the AccountDAO
        Account account = accountDAO.login(username, password);
        
        if (account == null) {
            // Throw an exception or handle login failure as needed
            throw new IllegalArgumentException("Invalid username or password.");
        }

        return account;
    }
}
