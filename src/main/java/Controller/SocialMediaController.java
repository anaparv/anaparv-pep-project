package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import DAO.AccountDAO;
import Service.AccountService;
import Service.MessageService;
import Model.Account;
import Model.Message;
import java.util.List;
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
        // app.get("example-endpoint", this::exampleHandler);

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
        try {
            Account account = objectMapper.readValue(ctx.body(), Account.class);
    
            // Create the account
            Account createdAccount = accountService.registerAccount(account);
            ctx.status(200).json(createdAccount); 
    
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(""); 
        } catch (Exception e) {
            ctx.status(500).result("Internal server error.");
        }
    }

    private void login(Context ctx) {
        try {
            Account account = objectMapper.readValue(ctx.body(), Account.class);
    
            // Validate username and password
            if (account.getUsername() == null || account.getUsername().trim().isEmpty()) {
                ctx.status(401).result("");
                return;
            }
            if (account.getPassword() == null || account.getPassword().length() < 4) {
                ctx.status(401).result("");
                return;
            }
    
            // Attempt to log in
            Account loggedInAccount = accountService.login(account.getUsername(), account.getPassword());
            ctx.json(loggedInAccount); 
    
        } catch (IllegalArgumentException e) {
            ctx.status(401).result("");
        } catch (SQLException e) {
            ctx.status(500).result("Database error.");
        } catch (Exception e) {
            ctx.status(500).result("Internal server error.");
        }
    }

    private void createMessage(Context ctx) {
        Message message;
        try {
            message = objectMapper.readValue(ctx.body(), Message.class);
        } catch (Exception e) {
            ctx.status(400);
            ctx.result(""); 
            return;
        }
    
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()) {
            ctx.status(400);
            ctx.result("");
            return;
        }
        if (message.getMessage_text().length() > 255) {
            ctx.status(400);
            ctx.result("");
            return;
        }
    
        try {
            if (!accountService.doesUserExist(message.getPosted_by())) {
                ctx.status(400);
                ctx.result("");
                return;
            }
    
            Message createdMessage = messageService.createMessage(message);
    
            ctx.status(200);
            ctx.json(createdMessage);
        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.result(e.getMessage());
        } catch (SQLException e) {
            ctx.status(500);
            ctx.result("Internal Server Error: " + e.getMessage());
        }
    }

    private void getAllMessages(Context ctx) {
        try {
            // Retrieve all messages using the message service
            List<Message> messages = messageService.getAllMessages();

            // Set the response body to the list of messages
            ctx.json(messages); 
        } catch (SQLException e) {
            // Handle any SQL exceptions
            ctx.status(500);
            ctx.result("Internal Server Error: " + e.getMessage());
        }
    }

    private void getMessageById(Context ctx) {
        int messageId;

        try {
            messageId = Integer.parseInt(ctx.pathParam("message_id"));
    
            Message message = messageService.getMessageById(messageId);
    
            if (message == null) {
                ctx.status(200);
                ctx.result("");
                return;
            }
    
            ctx.json(message);
    
        } catch (NumberFormatException e) {
            ctx.status(400); 
            ctx.result("Invalid message ID format.");
        } catch (SQLException e) {
            ctx.status(500); 
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
                ctx.json(message); 
            } else {
                ctx.status(200);
            }
        } catch (NumberFormatException e) {
            // Handle invalid message ID format
            ctx.status(400); 
            ctx.result("Invalid message ID format.");
        } catch (SQLException e) {
            // Handle any SQL exceptions
            ctx.status(500); 
            ctx.result("Internal Server Error: " + e.getMessage());
        }
    }

    private void updateMessageById(Context ctx) {
        try {
            int messageId = Integer.parseInt(ctx.pathParam("message_id"));
            Message updateMessage = ctx.bodyAsClass(Message.class);
    
            // Validate the new message text
            if (updateMessage.getMessage_text() == null || updateMessage.getMessage_text().trim().isEmpty()) {
                ctx.status(400); 
                ctx.result(""); 
                return;
            }
            if (updateMessage.getMessage_text().length() > 255) {
                ctx.status(400); 
                ctx.result(""); 
                return;
            }
    
            // Check if the message exists
            Message existingMessage = messageService.getMessageById(messageId);
            if (existingMessage == null) {
                ctx.status(400); 
                ctx.result(""); 
                return;
            }
    
            // Update the message
            existingMessage.setMessage_text(updateMessage.getMessage_text());
            Message updatedMessage = messageService.updateMessage(existingMessage);
    
            // Return the updated message as JSON
            ctx.json(updatedMessage);
        } catch (NumberFormatException e) {
            ctx.status(400); 
            ctx.result(""); 
        } catch (SQLException e) {
            ctx.status(500); 
            ctx.result("Internal Server Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(400); 
            ctx.result(""); 
        } catch (Exception e) {
            ctx.status(500); 
            ctx.result("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void getMessagesByUserId(Context ctx) {
        try {
            // Retrieve the account ID from the URL path parameter
            int accountId = Integer.parseInt(ctx.pathParam("account_id"));
    
            // Call the service to get messages by user ID
            List<Message> messages = messageService.getMessagesByUserId(accountId);
    
            // Set the response status and return the messages as JSON
            ctx.status(200).json(messages);
        } catch (NumberFormatException e) {
            ctx.status(400); 
            ctx.result("Invalid account ID format.");
        } catch (SQLException e) {
            ctx.status(500); 
            ctx.result("Internal Server Error: " + e.getMessage());
        }
    }
}