package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Account;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.dao.TransactionDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


@WebServlet("/CreateTransaction")
public class CreateTransaction extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Connection connection = null;
    private TemplateEngine templateEngine;

    public CreateTransaction() {
        super();
    }

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");

        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // If the user is not logged in (not present in session) redirect to the login
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (session.isNew() || user == null) {
            String loginpath = getServletContext().getContextPath() + "/index.html";
            response.sendRedirect(loginpath);
            return;
        }

        // Get and parse all parameters from request
        boolean isBadRequest = false;

        Integer originAccountId = null;
        String destinationUsername = null;
        Integer destinationAccountId = null;
        Double amount = null;
        String description = null;

        try {
            amount = Double.parseDouble(request.getParameter("amount"));
            destinationUsername = StringEscapeUtils.escapeJava(request.getParameter("recipient-username"));
            originAccountId = Integer.parseInt(request.getParameter("accountid"));
            destinationAccountId = Integer.parseInt(request.getParameter("recipient-accountid"));
            description = StringEscapeUtils.escapeJava(request.getParameter("description"));

            isBadRequest = amount <= 0 || destinationUsername.isEmpty() || description.isEmpty();
        } catch (NumberFormatException | NullPointerException e) {
            isBadRequest = true;
            e.printStackTrace();
        }
        if (isBadRequest) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }


        //account id origin (mine) + balance
        //account id destination + balance
        AccountDAO accountDAO = new AccountDAO(connection);
        TransactionDAO transactionDAO = new TransactionDAO(connection);
        Account origin;
        Account destination;
        boolean originUserOwnsAccount, destinationUserOwnsAccount;
        try {
            originUserOwnsAccount = accountDAO.userOwnsAccount(user.getUsername(), originAccountId);
            destinationUserOwnsAccount = accountDAO.userOwnsAccount(destinationUsername, destinationAccountId);

            origin = accountDAO.findAccountById(originAccountId);
            //destination returns null if recipient account doesn't exist
            destination = accountDAO.findAccountById(destinationAccountId);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to retrieve accounts data");
            return;
        }

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());


     /*
        transaction checks:
        existence destination (account)
        origin != destination (account)
        amount <= origin.balance (account)
        username owns destination account (user, account)
     */


        String errorMsg = "";

        if (destination == null) {
            errorMsg = "Destination account doesn't exist";
        } else if (!originUserOwnsAccount) {
            errorMsg = "You don't own the origin account";
        } else if (!destinationUserOwnsAccount) {
            errorMsg = "Username doesn't match the selected account";
        } else if (origin.getAccountId() == destination.getAccountId()) {
            errorMsg = "Origin and destination account must be different";
        } else if (amount > origin.getBalance()) {
            errorMsg = "Insufficient funds for this transaction";
        }

        String path;

        //no errors in transaction -> create transaction
        if (errorMsg.equals("")) {

            // Create transaction in DB
            try {
                transactionDAO.createTransaction(originAccountId, destinationAccountId, amount, description);
                origin = accountDAO.findAccountById(originAccountId);
                destination = accountDAO.findAccountById(destinationAccountId);
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create transaction");
                return;
            }
            path = "WEB-INF/confirmation.html";
            ctx.setVariable("origin", origin);
            ctx.setVariable("destination", destination);


        } else {
            //error page specifying error type
            ctx.setVariable("errorMsg", errorMsg);
            path = "WEB-INF/transactionError.html";
        }

        templateEngine.process(path, ctx, response.getWriter());

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doPost(request, response);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
