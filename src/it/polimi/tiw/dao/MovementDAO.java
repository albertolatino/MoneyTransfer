package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Movement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovementDAO {
    private Connection connection;

    public MovementDAO(Connection connection) {
        this.connection = connection;
    }


    public List<Movement> findTransfersByAccount(int accountId) throws SQLException {

        List<Movement> movements = new ArrayList<>();

        String query = "SELECT * from transfer where accountId = ? ORDER BY date DESC";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, accountId);
            try (ResultSet result = pstatement.executeQuery();) {
                while (result.next()) {
                    Movement movement = new Movement();//Create java Bean
                    movement.setTransferId(result.getInt("transferId"));
                    movement.setAmount((result.getDouble("amount")));
                    movement.setOriginId(result.getInt("originId"));
                    movement.setDestinationId(result.getInt("destinationId"));
                    movement.setDate(result.getDate("date"));
                    movements.add(movement);
                }
            }
        }
        return movements;
    }


    /**
     * Changes account balance.
     * @param accountId Account to be changed.
     * @param balance Total balance in account.
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
