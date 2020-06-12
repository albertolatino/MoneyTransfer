package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class AccountDAO {
    private Connection connection;

    public AccountDAO(Connection connection) {
        this.connection = connection;
    }


    public List<Account> findAccountsByUser(int userId) throws SQLException {

        List<Account> accounts = new ArrayList<>();

        String query = "SELECT * from account where userId = ? ORDER BY balance DESC";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, userId);
            try (ResultSet result = pstatement.executeQuery();) {
                while (result.next()) {
                    Account account = new Account();//Create java Bean
                    account.setUserId(result.getInt("userId"));
                    account.setAccountId(result.getInt("accountId"));
                    account.setBalance((result.getDouble("balance")));
                    accounts.add(account);
                }
            }
        }
        return accounts;
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
