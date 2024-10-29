package Service;
import DAO.MessageDAO;
import Model.Message;
import java.util.List;
import java.sql.*;


public class MessageService {
    private MessageDAO messageDAO = new MessageDAO();

    public Message createMessage(Message message) throws SQLException {
        // Validate message fields
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()) {
            throw new IllegalArgumentException("Message text cannot be blank.");
        }
        if (message.getMessage_text().length() > 255) {
            throw new IllegalArgumentException("Message text cannot exceed 255 characters.");
        }

        // Validate user existence
        if (!messageDAO.doesUserExist(message.getPosted_by())) {
            throw new IllegalArgumentException("User does not exist.");
        }

        return messageDAO.createMessage(message);
    }

    public List<Message> getAllMessages() throws SQLException {
        return messageDAO.getAllMessages();
    }

    public Message getMessageById(int messageId) throws SQLException {
        return messageDAO.getMessageById(messageId);
    }

    public List<Message> getMessagesByUserId(int accountId) throws SQLException {
        return messageDAO.getMessagesByAccountId(accountId);
    }

    public boolean deleteMessageById(int messageId) throws SQLException {
        return messageDAO.deleteMessageById(messageId);
    }

    public Message updateMessage(Message message) throws SQLException {
        try {
            return messageDAO.updateMessageText(message.getMessage_id(), message.getMessage_text());
        } catch (SQLException e) {
            if (e.getMessage().contains("does not exist")) {
                throw new IllegalArgumentException("Message not found.");
            }
            throw e;
        }
    }
}
