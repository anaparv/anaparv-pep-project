package DAO;
import java.sql.*;
import Model.Account;
import Util.ConnectionUtil;


public class AccountDAO {
    Connection connection = ConnectionUtil.getConnection();

    public Account createAccount(Account account) throws SQLException {
        String query = "INSERT INTO account (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating account failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setAccount_id(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating account failed, no ID obtained.");
                }
            }
        }
        return account;
    }

    public boolean doesUsernameExist(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM account WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            
            try (ResultSet resultSet = stmt.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    public boolean doesAccountIdExist(int accountId) throws SQLException {
        String query = "SELECT COUNT(*) FROM account WHERE account_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, accountId);
            
            try (ResultSet resultSet = stmt.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    public Account login(String username, String password) throws SQLException {
        String query = "SELECT * FROM account WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // Create an Account object and set its properties
                    Account account = new Account();
                    account.setAccount_id(resultSet.getInt("account_id"));
                    account.setUsername(resultSet.getString("username"));
                    account.setPassword(resultSet.getString("password"));
                    return account; // Successful login
                } else {
                    return null; // Login failed (wrong username/password)
                }
            }
        }
    }
}
