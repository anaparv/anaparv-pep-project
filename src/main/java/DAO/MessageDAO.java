package DAO;
import java.sql.*;
import Model.Message;
import Util.ConnectionUtil;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    Connection connection = ConnectionUtil.getConnection();

    public Message createMessage(Message message) throws SQLException {
        // Validate message fields
        validateMessage(message);

        // Check if the user exists
        if (!doesUserExist(message.getPosted_by())) {
            throw new IllegalArgumentException("User does not exist.");
        }

        // Insert the message into the database
        String query = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, message.getPosted_by());
            stmt.setString(2, message.getMessage_text());
            stmt.setLong(3, message.getTime_posted_epoch());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating message failed, no rows affected.");
            }

            // Retrieve the generated message_id
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    message.setMessage_id(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating message failed, no ID obtained.");
                }
            }
        }
        return message;
    }

    // Validates the message fields.
    private void validateMessage(Message message) {
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()) {
            throw new IllegalArgumentException("Message text cannot be blank.");
        }
        if (message.getMessage_text().length() > 255) {
            throw new IllegalArgumentException("Message text cannot exceed 255 characters.");
        }
    }

    // Checks if a user exists in the account table.
    public boolean doesUserExist(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM account WHERE account_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public Message getMessageById(int messageId) throws SQLException {
        String query = "SELECT * FROM message WHERE message_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, messageId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // Create and return the Message object
                    return new Message(
                            resultSet.getInt("message_id"),
                            resultSet.getInt("posted_by"),
                            resultSet.getString("message_text"),
                            resultSet.getLong("time_posted_epoch")
                    );
                } else {
                    // No message found with the given message_id
                    return null;
                }
            }
        }
    }

    public List<Message> getAllMessages() throws SQLException {
        List<Message> messages = new ArrayList<>();

        String query = "SELECT * FROM message";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                // Create and add a Message object to the list
                Message message = new Message(
                        resultSet.getInt("message_id"),
                        resultSet.getInt("posted_by"),
                        resultSet.getString("message_text"),
                        resultSet.getLong("time_posted_epoch")
                );
                messages.add(message);
            }
        }

        return messages;
    }

    public List<Message> getMessagesByAccountId(int accountId) throws SQLException {
        List<Message> messages = new ArrayList<>();

        String query = "SELECT * FROM message WHERE posted_by = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, accountId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    // Create and add a Message object to the list
                    Message message = new Message(
                            resultSet.getInt("message_id"),
                            resultSet.getInt("posted_by"),
                            resultSet.getString("message_text"),
                            resultSet.getLong("time_posted_epoch")
                    );
                    messages.add(message);
                }
            }
        }

        return messages;
    }

    public Message updateMessageText(int messageId, String newText) throws SQLException {
        // Ensure the new text is not blank and not over 255 characters
    if (newText == null || newText.trim().isEmpty() || newText.length() > 255) {
        throw new IllegalArgumentException("Message text must be non-blank and under 255 characters.");
    }

    // Initialize variables to hold the existing message details
    int postedBy = 0;
    long timePostedEpoch = 0;

    // Check if the message exists before attempting the update
    String selectQuery = "SELECT * FROM message WHERE message_id = ?";
    try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
        selectStmt.setInt(1, messageId);
        try (ResultSet resultSet = selectStmt.executeQuery()) {
            if (!resultSet.next()) {
                // Message does not exist, throw an exception
                throw new SQLException("Message with the provided ID does not exist.");
            }
            // Retrieve existing details from the ResultSet
            postedBy = resultSet.getInt("posted_by");
            timePostedEpoch = resultSet.getLong("time_posted_epoch");
        }
    }

    // Proceed with the update if the message exists
    String updateQuery = "UPDATE message SET message_text = ? WHERE message_id = ?";
    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
        updateStmt.setString(1, newText);
        updateStmt.setInt(2, messageId);

        int rowsUpdated = updateStmt.executeUpdate();
        if (rowsUpdated > 0) {
            // Return the updated message using the existing posted_by and time_posted_epoch
            return new Message(messageId, postedBy, newText, timePostedEpoch);
        } else {
            throw new SQLException("Failed to update the message.");
        }
    }
    }

    public boolean deleteMessageById(int messageId) throws SQLException {
        String deleteQuery = "DELETE FROM message WHERE message_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setInt(1, messageId);
            
            int rowsAffected = stmt.executeUpdate();
            
            // If rowsAffected is greater than 0, the message was deleted
            return rowsAffected > 0;
        }
    }
}
