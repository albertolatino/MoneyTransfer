package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private Connection connection;

    public TransactionDAO(Connection connection) {
        this.connection = connection;
    }


    public List<Transaction> findTransactionsByAccount(int accountId) throws SQLException {

        List<Transaction> transactions = new ArrayList<>();

        String query = "SELECT * from transaction where originId = ? OR destinationId = ? ORDER BY date DESC";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, accountId);
            pstatement.setInt(2, accountId);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    Transaction transaction = new Transaction();//Create java Bean
                    transaction.setTransactionId(result.getInt("transactionId"));
                    transaction.setDate(result.getDate("date"));
                    transaction.setAmount((result.getDouble("amount")));
                    transaction.setOriginId(result.getInt("originId"));
                    transaction.setDestinationId(result.getInt("destinationId"));
                    transaction.setDescription(result.getString("description"));
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }


    /**
     * Changes account balance.
     *
     * @param accountId Account to be changed.
     * @param balance   Total balance in account.
     * @throws SQLException
     */
    public void changeAccountBalance(int accountId, double balance) throws SQLException {

        String query = "UPDATE account SET balance = ? WHERE accountId = ? ";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setDouble(1, balance);
            pstatement.setInt(2, accountId);
            pstatement.executeUpdate();
        }
    }


    public void createTransaction(String recipientUsername, int originId, int destinationId, double amount, String description)
            throws SQLException {

        /*
        if (!checkAccountOwner(recipientUsername, originId)) {
            System.out.println("account && username not matching");
            //todo
        }*/

        String query = "INSERT into transaction (transactionId, date, amount, originId, destinationId, description) VALUES(NULL, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setDate(2, new Date(System.currentTimeMillis()));
            pstatement.setDouble(3, amount);
            pstatement.setInt(4, originId);
            pstatement.setInt(5, destinationId);
            pstatement.setString(6, description);

            pstatement.executeUpdate();
        }
    }

    private boolean checkAccountOwner(String username, int accountId) throws SQLException {
        String query = "SELECT * FROM user, account  WHERE user.username = ? AND user.userId = account.userId AND account.accountId = ?";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setString(1, username);
            pstatement.setInt(2, accountId);

            try (ResultSet result = pstatement.executeQuery()) {

                if (!result.isBeforeFirst()) // no results, check failed
                    return false;
                else
                    return true;
            }
        }
    }


}
