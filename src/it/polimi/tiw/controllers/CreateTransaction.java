package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.Account;
import it.polimi.tiw.dao.TransactionDAO;
import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;


@WebServlet("/CreateTransaction")
public class CreateTransaction extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Connection connection = null;
    private TemplateEngine templateEngine;

    public CreateTransaction() {
        super();
    }

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // If the user is not logged in (not present in session) redirect to the login
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {
            String loginpath = getServletContext().getContextPath() + "/index.html";
            response.sendRedirect(loginpath);
            return;
        }

        // Get and parse all parameters from request
        boolean isBadRequest = false;

        String recipientUsername = null;
        Integer recipientAccountId = null;
        Double amount = null;
        String description = null;

        try {
            amount = Double.parseDouble(request.getParameter("amount"));
            recipientUsername = StringEscapeUtils.escapeJava(request.getParameter("recipient-username"));
            recipientAccountId = Integer.parseInt(request.getParameter("recipient-accountid"));
            description = StringEscapeUtils.escapeJava(request.getParameter("description"));

            isBadRequest = amount <= 0 || recipientUsername.isEmpty() || description.isEmpty();
        } catch (NumberFormatException | NullPointerException e) {
            isBadRequest = true;
            e.printStackTrace();
        }
        if (isBadRequest) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }

        // Create transaction in DB
        Integer accountId = (Integer) request.getAttribute("accountId");
        TransactionDAO transactionDAO = new TransactionDAO(connection);
        try {
            transactionDAO.createTransaction(recipientUsername, accountId, recipientAccountId, amount, description);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create transaction");
            return;
        }


        /*
        account id origin (mio) + balance
        account id destination + balance
         */
        AccountDAO accountDAO = new AccountDAO(connection);
        Account origin = null;
        Account destination = null;
        try {
            origin = accountDAO.findAccountsById(accountId);
            destination = accountDAO.findAccountsById(recipientAccountId);
        } catch (SQLException throwables) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to retrieve accounts data");
            return;
        }

        // return the user to the right view
        //String contextPath = getServletContext().getContextPath();
        //String path = contextPath + "/Confirmation";

        //response.sendRedirect(path);
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("origin", origin);
        ctx.setVariable("destination", destination);
        String path = "WEB-INF/confirmation.html";
        templateEngine.process(path, ctx, response.getWriter());

    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
