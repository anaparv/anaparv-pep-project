package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import DAO.AccountDAO;
import Service.AccountService;
import Service.MessageService;
import Model.Account;
import Model.Message;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
public class SocialMediaController {

    private final AccountDAO accountDAO = new AccountDAO();
    private final AccountService accountService = new AccountService(accountDAO);
    private final MessageService messageService = new MessageService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        //    app.get("example-endpoint", this::exampleHandler);
        // User Registration
        app.post("/register", this::registerUser);

        // User Login
        app.post("/login", this::login);

        // Create a new message
        app.post("/messages", this::createMessage);

        // Retrieve all messages
        app.get("/messages", this::getAllMessages);

        // Retrieve message by ID
        app.get("/messages/{message_id}", this::getMessageById);

        // Delete a message by ID
        app.delete("/messages/{message_id}", this::deleteMessageById);

        // Update a message by ID
        app.patch("/messages/{message_id}", this::updateMessageById);

        // Retrieve messages by user ID
        app.get("/accounts/{account_id}/messages", this::getMessagesByUserId);

        return app;
    }

    /**
     * This is an example handler for an example endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     */
    private void exampleHandler(Context context) {
        context.json("sample text");
    }

    private void registerUser(Context ctx) {
        // Check if the request body is empty
    if (ctx.body().isEmpty()) {
        ctx.status(200).result("No account information provided."); // or any appropriate response
        return;
    }

    try {
        // Deserialize the request body into an Account object
        Account account = objectMapper.readValue(ctx.body(), Account.class);

        // Check if the username is blank before checking existence
        if (account.getUsername() == null || account.getUsername().trim().isEmpty()) {
            ctx.status(400).result("Username cannot be blank.");
            return;
        }

        // Check if username already exists
        if (accountService.doesUsernameExist(account.getUsername())) {
            ctx.status(400).result("Username already exists.");
            return;
        }

        // Validate password length
        if (account.getPassword() == null || account.getPassword().length() < 4) {
            ctx.status(400).result("Password must be at least 4 characters long.");
            return;
        }

        // Create the account
        Account createdAccount = accountService.registerAccount(account);
        ctx.status(200).json(createdAccount); // Return created account with status 200

    } catch (IllegalArgumentException e) {
        // Handle validation exceptions separately
        ctx.status(400).result(e.getMessage());
    } catch (JsonProcessingException e) {
        // Handle JSON parsing exceptions
        ctx.status(400).result("Invalid JSON format.");
    } catch (SQLException e) {
        // Handle SQL exceptions related to account registration
        ctx.status(500).result("Failed to register account due to database error.");
    } catch (Exception e) {
        // Handle any other unexpected exceptions
        ctx.status(500).result("Internal server error.");
    }
    }

    private void login(Context ctx) {
        // Parse the JSON body to Account object
        Account loginAccount;
        try {
            loginAccount = objectMapper.readValue(ctx.body(), Account.class);
        } catch (Exception e) {
            ctx.status(400);
            ctx.result("Invalid JSON format.");
            return;
        }

        try {
            // Call the login method from the account service
            Account account = accountService.login(loginAccount.getUsername(), loginAccount.getPassword());

            // Set the response status and body
            ctx.status(200);
            ctx.json(account); // Send the account back as JSON
        } catch (IllegalArgumentException e) {
            // If login fails, respond with 401 Unauthorized
            ctx.status(401);
            ctx.result(e.getMessage());
        } catch (SQLException e) {
            // Handle any SQL exceptions
            ctx.status(500);
            ctx.result("Internal Server Error: " + e.getMessage());
        }
    }

    private void createMessage(Context ctx) {
        // Parse the JSON body to Message object
        Message message;
        try {
            message = objectMapper.readValue(ctx.body(), Message.class);
        } catch (Exception e) {
            ctx.status(400);
            ctx.result("Invalid JSON format.");
            return;
        }

        try {
            // Call the createMessage method from the message service
            Message createdMessage = messageService.createMessage(message);

            // Set the response status and body
            ctx.status(200);
            ctx.json(createdMessage); // Send the created message back as JSON
        } catch (IllegalArgumentException e) {
            // If validation fails, respond with 400 Bad Request
            ctx.status(400);
            ctx.result(e.getMessage());
        } catch (SQLException e) {
            // Handle any SQL exceptions
            ctx.status(500);
            ctx.result("Internal Server Error: " + e.getMessage());
        }
    }

    private void getAllMessages(Context ctx) {
        try {
            // Retrieve all messages using the message service
            List<Message> messages = messageService.getAllMessages();

            // Set the response body to the list of messages
            ctx.json(messages); // Automatically returns an empty list if there are no messages
        } catch (SQLException e) {
            // Handle any SQL exceptions
            ctx.status(500); // Internal Server Error
            ctx.result("Internal Server Error: " + e.getMessage());
        }
    }

    private void getMessageById(Context ctx) {
        int messageId;

        try {
            messageId = Integer.parseInt(ctx.pathParam("message_id")); // Parse the message ID
    
            Message message = messageService.getMessageById(messageId); // Get message by ID
    
            if (message == null) {
                ctx.status(200); // Change this to 200 if the message is not found
                ctx.result(""); // Return an empty body
                return;
            }
    
            ctx.json(message); // Return the message as JSON if found
    
        } catch (NumberFormatException e) {
            ctx.status(400); // Bad Request for invalid message ID format
            ctx.result("Invalid message ID format.");
        } catch (SQLException e) {
            ctx.status(500); // Internal Server Error for other exceptions
            ctx.result("Database error.");
        }
    }

    private void deleteMessageById(Context ctx) {
        try {
            // Retrieve the message ID from the path parameters
            int messageId = Integer.parseInt(ctx.pathParam("message_id"));
            
            // Retrieve the message before deletion to return it in the response
            Message message = messageService.getMessageById(messageId);
            
            // Delete the message using the message service
            boolean deleted = messageService.deleteMessageById(messageId);
            
            if (deleted) {
                ctx.json(message); // Return the deleted message
            } else {
                ctx.status(200); // Return 200 OK with empty body
            }
        } catch (NumberFormatException e) {
            // Handle invalid message ID format
            ctx.status(400); // Bad Request
            ctx.result("Invalid message ID format.");
        } catch (SQLException e) {
            // Handle any SQL exceptions
            ctx.status(500); // Internal Server Error
            ctx.result("Internal Server Error: " + e.getMessage());
        }
    }

    private void updateMessageById(Context ctx) {
        try {
            // Retrieve the message ID from the path parameters
            int messageId = Integer.parseInt(ctx.pathParam("message_id"));
            
            // Parse the request body to get the new message text
            Message updateMessage = ctx.bodyAsClass(Message.class); // Assuming Message has a setter for message_text
            
            // Validate the new message text
            if (updateMessage.getMessage_text() == null || updateMessage.getMessage_text().trim().isEmpty()) {
                ctx.status(400); // Bad Request
                ctx.result("Message text cannot be blank.");
                return;
            }
            if (updateMessage.getMessage_text().length() > 255) {
                ctx.status(400); // Bad Request
                ctx.result("Message text cannot exceed 255 characters.");
                return;
            }

            // Fetch the existing message to ensure it exists
            Message existingMessage = messageService.getMessageById(messageId);
            if (existingMessage == null) {
                ctx.status(400); // Bad Request if the message does not exist
                ctx.result("Message ID not found.");
                return;
            }

            // Update the message text
            existingMessage.setMessage_text(updateMessage.getMessage_text());

            // Call the DAO to persist the updated message
            Message updatedMessage = messageService.updateMessage(existingMessage);

            // Return the updated message as JSON
            ctx.json(updatedMessage);
        } catch (NumberFormatException e) {
            // Handle invalid message ID format
            ctx.status(400); // Bad Request
            ctx.result("Invalid message ID format.");
        } catch (SQLException e) {
            // Handle any SQL exceptions
            ctx.status(500); // Internal Server Error
            ctx.result("Internal Server Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle invalid message content
            ctx.status(400); // Bad Request
            ctx.result(e.getMessage());
        }
    }

    private void getMessagesByUserId(Context ctx) {
        try {
            // Retrieve the account ID from the URL path parameter
            int accountId = Integer.parseInt(ctx.pathParam("account_id"));
    
            // Call the service to get messages by user ID
            List<Message> messages = messageService.getMessagesByUserId(accountId);
    
            // Set the response status and return the messages as JSON
            ctx.status(200).json(messages); // 200 is the default status
        } catch (NumberFormatException e) {
            ctx.status(400); // Bad Request
            ctx.result("Invalid account ID format.");
        } catch (SQLException e) {
            ctx.status(500); // Internal Server Error
            ctx.result("Internal Server Error: " + e.getMessage());
        }
    }
}