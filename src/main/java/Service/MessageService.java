package Service;
import DAO.MessageDAO;
import Model.Message;
import java.util.List;
import java.sql.*;


public class MessageService {
    private MessageDAO messageDAO = new MessageDAO();

    public Message createMessage(Message message) throws SQLException {
        // Validate message fields at the service level (you can also keep this in the DAO if necessary)
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()) {
            throw new IllegalArgumentException("Message text cannot be blank.");
        }
        if (message.getMessage_text().length() > 255) {
            throw new IllegalArgumentException("Message text cannot exceed 255 characters.");
        }
        
        // Validate user existence (could be optional if already done in DAO)
        if (!messageDAO.doesUserExist(message.getPosted_by())) {
            throw new IllegalArgumentException("User does not exist.");
        }

        // Set the current time as epoch time for the message
        message.setTime_posted_epoch(System.currentTimeMillis() / 1000L);

        // Call DAO to persist the message to the database
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
        return messageDAO.updateMessageText(message.getMessage_id(), message.getMessage_text());
    }
}
