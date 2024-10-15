package Service;
import DAO.MessageDAO;
import Model.Message;
import java.util.List;


public class MessageService {
    private MessageDAO messageDAO = new MessageDAO();

    // Method to handle message creation
    public Message createMessage(int posted_by, String message_text, long time_posted_epoch) {
        if (message_text == null || message_text.trim().isEmpty() || message_text.length() > 255) {
            throw new IllegalArgumentException("Invalid message text");
        }

        Message message = new Message(posted_by, message_text, time_posted_epoch);
        return messageDAO.createMessage(message);
    }

    // Method to retrieve a message by its ID
    public Message getMessageById(int message_id) {
        return messageDAO.getMessageById(message_id);
    }

    // Method to retrieve all messages
    public List<Message> getAllMessages() {
        return messageDAO.getAllMessages();
    }

    // Method to update a message
    public boolean updateMessage(int message_id, String newMessageText) {
        if (newMessageText == null || newMessageText.trim().isEmpty() || newMessageText.length() > 255) {
            throw new IllegalArgumentException("Invalid message text");
        }
        return messageDAO.updateMessage(message_id, newMessageText);
    }

    // Method to delete a message
    public boolean deleteMessage(int message_id) {
        return messageDAO.deleteMessage(message_id);
    }

    // Method to retrieve all messages by a specific user
    public List<Message> getMessagesByUserId(int posted_by) {
        return messageDAO.getMessagesByUserId(posted_by);
    }
}
