package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            try (ResultSet result = pstatement.executeQuery();) {
                while (result.next()) {
                    Transaction transaction = new Transaction();//Create java Bean
                    transaction.setTransactionId(result.getInt("transactionId"));
                    transaction.setDate(result.getDate("date"));
                    transaction.setAmount((result.getDouble("amount")));
                    transaction.setOriginId(result.getInt("originId"));
                    transaction.setDestinationId(result.getInt("destinationId"));
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

	/*
	public void createMission(Date startDate, int days, String destination, String description, int reporterId)
			throws SQLException {

		String query = "INSERT into mission (date, destination, state, description, days, reporter) VALUES(?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setDate(1, new java.sql.Date(startDate.getTime()));
			pstatement.setString(2, destination);
			pstatement.setInt(3, MissionStatus.OPEN.getValue());
			pstatement.setString(4, description);
			pstatement.setInt(5, days);
			pstatement.setInt(6, reporterId);
			pstatement.executeUpdate();
		}
	}*/


}
