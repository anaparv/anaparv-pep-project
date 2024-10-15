package DAO;
import java.sql.*;
import Model.Account;
import Util.ConnectionUtil;
import java.util.ArrayList;
import java.util.List;


public class AccountDAO {
    // Create a new account
    public void createAccount(String username, String password) throws SQLException {
        String query = "INSERT INTO account (username, password) VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
        }
    }

    // Get account by id
    public Account getAccountById(int accountId) throws SQLException {
        String query = "SELECT * FROM account WHERE account_id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password"));
            }
        }
        return null; // Account not found
    }

    // Get all accounts
    public List<Account> getAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String query = "SELECT * FROM account";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                accounts.add(new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password")));
            }
        }
        return accounts;
    }

    // Update account
    public void updateAccount(int accountId, String newUsername, String newPassword) throws SQLException {
        String query = "UPDATE account SET username = ?, password = ? WHERE account_id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newUsername);
            stmt.setString(2, newPassword);
            stmt.setInt(3, accountId);
            stmt.executeUpdate();
        }
    }

    // Delete account
    public void deleteAccount(int accountId) throws SQLException {
        String query = "DELETE FROM account WHERE account_id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, accountId);
            stmt.executeUpdate();
        }
    }

    // Retrieve an account by username
    public Account getAccountByUsername(String username) {
        Account account = null;
        String sql = "SELECT * FROM account WHERE username = ?";

        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int accountId = rs.getInt("account_id");
                String retrievedUsername = rs.getString("username");
                String password = rs.getString("password");

                account = new Account(accountId, retrievedUsername, password);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return account;
    }
}
