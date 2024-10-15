package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import DAO.AccountDAO;
import Service.AccountService;
import Service.MessageService;
import Model.Account;
import Model.Message;
import java.util.List;
/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
public class SocialMediaController {

    private final AccountDAO accountDAO = new AccountDAO();
    private final AccountService accountService = new AccountService(accountDAO);
    private final MessageService messageService = new MessageService();
    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
    //    app.get("example-endpoint", this::exampleHandler);
    // User Registration
    app.post("/register", this::handleRegister);

    // User Login
    app.post("/login", this::handleLogin);

    // Create a new message
    app.post("/messages", this::handleCreateMessage);

    // Retrieve all messages
    app.get("/messages", this::handleGetAllMessages);

    // Retrieve message by ID
    app.get("/messages/:message_id", this::handleGetMessageById);

    // Delete a message by ID
    app.delete("/messages/:message_id", this::handleDeleteMessage);

    // Update a message by ID
    app.patch("/messages/:message_id", this::handleUpdateMessage);

    // Retrieve messages by user ID
    app.get("/accounts/:account_id/messages", this::handleGetMessagesByUserId);

        return app;
    }

    /**
     * This is an example handler for an example endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     */
    private void exampleHandler(Context context) {
        context.json("sample text");
    }

    private void handleRegister(Context context) throws Exception {
        Account account = context.bodyAsClass(Account.class);

        try {
            Account registeredAccount = accountService.registerAccount(account.getUsername(), account.getPassword());
            context.json(registeredAccount);
        } catch (IllegalArgumentException e) {
            context.status(400).json(e.getMessage());
        }
    }

    // Handler for user login
    private void handleLogin(Context context) throws Exception {
        Account account = context.bodyAsClass(Account.class);

        try {
            Account loggedInAccount = accountService.loginAccount(account.getUsername(), account.getPassword());
            context.json(loggedInAccount);
        } catch (IllegalArgumentException e) {
            context.status(401).json(e.getMessage());
        }
    }

    // Handler for creating a message
    private void handleCreateMessage(Context context) {
        Message message = context.bodyAsClass(Message.class);

        try {
            Message createdMessage = messageService.createMessage(message.getPosted_by(), message.getMessage_text(), message.getTime_posted_epoch());
            context.json(createdMessage);
        } catch (IllegalArgumentException e) {
            context.status(400).json(e.getMessage());
        }
    }

    // Handler for retrieving all messages
    private void handleGetAllMessages(Context context) {
        List<Message> messages = messageService.getAllMessages();
        context.json(messages);
    }

    // Handler for retrieving a message by its ID
    private void handleGetMessageById(Context context) throws Exception {
        int messageId = Integer.parseInt(context.pathParam("message_id"));
        Message message = messageService.getMessageById(messageId);
        
        if (message != null) {
            context.json(message);
        } else {
            context.status(404).json("Message not found");
        }
    }

    // Handler for deleting a message by its ID
    private void handleDeleteMessage(Context context) {
        int messageId = Integer.parseInt(context.pathParam("message_id"));
        boolean deleted = messageService.deleteMessage(messageId);
        
        if (deleted) {
            context.status(200).json("Message deleted");
        } else {
            context.status(404).json("Message not found");
        }
    }

    // Handler for updating a message
    private void handleUpdateMessage(Context context) {
        int messageId = Integer.parseInt(context.pathParam("message_id"));
        String newMessageText = context.bodyAsClass(String.class); // Assuming body contains just the new message text

        try {
            boolean updated = messageService.updateMessage(messageId, newMessageText);
            if (updated) {
                context.json(messageService.getMessageById(messageId)); // Return updated message
            } else {
                context.status(400).json("Message not found or invalid text");
            }
        } catch (IllegalArgumentException e) {
            context.status(400).json(e.getMessage());
        }
    }

    // Handler for retrieving messages by user ID
    private void handleGetMessagesByUserId(Context context) {
        int accountId = Integer.parseInt(context.pathParam("account_id"));
        List<Message> messages = messageService.getMessagesByUserId(accountId);
        context.json(messages);
    }
}