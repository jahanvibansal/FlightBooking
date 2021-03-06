package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dto.Booking;
import com.exception.DatabaseException;
import com.exception.FileException;
import com.exception.InputException;
import com.util.DatabaseUtil;
import com.util.EnumUtil;

public class BookingDaoImpl implements BookingDao{

	@Override
	public List<Booking> BookingHistoryByPassengerId(int passengerId) throws FileException, DatabaseException {
		List<Booking> bookingList = new ArrayList<>();
		ResultSet set = null;
		String sql = "select booking_id, flight_id, seat_number, baggage, class, status from booking where passenger_id = ?";
		try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
			ps.setInt(1, passengerId);
			set = ps.executeQuery();
			while (set.next()) {
				Booking booking = new Booking(set.getInt("booking_id"), passengerId, 
						set.getInt("flight_id"), set.getInt("seat_number"), set.getInt("baggage"), 
						EnumUtil.stringToFlightClass(set.getString("class")), 
						EnumUtil.stringToBookingStatus(set.getString("status")));
				bookingList.add(booking);
			}
			if (set != null)
				set.close();
		} catch (SQLException e) {
			throw new DatabaseException("Unable to get booking information: " + e.getMessage());
		}
		return bookingList;
	}

	@Override
	public List<Booking> BookingHistory() throws FileException, DatabaseException {
		List<Booking> bookingList = new ArrayList<>();
		ResultSet set = null;
		String sql = "select booking_id, passenger_id, flight_id, seat_number, baggage, class, status from booking";
		try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
			set = ps.executeQuery();
			while (set.next()) {
				Booking booking = new Booking(set.getInt("booking_id"), set.getInt("passenger_id"), 
						set.getInt("flight_id"), set.getInt("seat_number"), set.getInt("baggage"), 
						EnumUtil.stringToFlightClass(set.getString("class")), 
						EnumUtil.stringToBookingStatus(set.getString("status")));
				bookingList.add(booking);
			}
			if (set != null)
				set.close();
		} catch (SQLException e) {
			throw new DatabaseException("Unable to get booking information: " + e.getMessage());
		}
		return bookingList;
	}

	@Override
	public int BookingFlight(Booking booking) throws DatabaseException, FileException {
		int booking_id = 0;
		String insertSql = "insert into booking(booking_id, passenger_id, flight_id, seat_number, baggage, class, status) "
				+ "values (nextval('booking_seq'), ?, ?, ?, ?, ?, ?)";
		String querySql = "select currval('booking_seq') as id ";
		ResultSet set = null;
		try (Connection conn = DatabaseUtil.getConnection(); 
				PreparedStatement insertPS = conn.prepareStatement(insertSql);
				PreparedStatement queryPS = conn.prepareStatement(querySql);) {
			insertPS.setInt(1, booking.getPassangerId());
			insertPS.setInt(2, booking.getFlightId());
			insertPS.setInt(3, booking.getSeatNumber());
			insertPS.setInt(4, booking.getBaggage());
			insertPS.setString(5, booking.getFlightClass().toString());
			insertPS.setString(6, booking.getStatus().toString());
			int row = insertPS.executeUpdate();
			if (row == 0) {
				throw new DatabaseException("Unable to book ticket.");
			}
			set = queryPS.executeQuery();
			if (set.next()) {
				booking_id = set.getInt("id");
			}
			if (booking_id == 0) {
				throw new DatabaseException("Unable to book ticket.");
			}
			if (set != null) {
				set.close();
			}
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Unable to book ticket: " + e.getMessage());
		}
		return booking_id;
	}

	@Override
	public Booking getBookingById(int bookingId) throws SQLException, DatabaseException, FileException {
		
		Booking booking = null;
		ResultSet set = null;
		String sql = "select booking_id, flight_id, seat_number, baggage, class, status from booking where booking_id = ?";
		try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
			ps.setInt(1, bookingId);
			set = ps.executeQuery();
			while (set.next()) {
				booking = new Booking(bookingId, set.getInt("passengerId"), 
						set.getInt("flight_id"), set.getInt("seat_number"), set.getInt("baggage"), 
						EnumUtil.stringToFlightClass(set.getString("class")), 
						EnumUtil.stringToBookingStatus(set.getString("status")));
			}
			if (set != null)
				set.close();			
		} catch (SQLException e) {
			throw new DatabaseException("Unable to get booking information: " + e.getMessage());
		}		
		
		return booking;
	}

	@Override
	public int updateBooking(Booking booking) throws DatabaseException, FileException, InputException {
		int row = 0;
		String sql = "update booking set flight_id = ?, seat_number = ?, baggage = ?, class = ?, status  = ? where booking_id = ?";
		
		try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);) {
			
			if (booking.getBookingId() <=0) {
				throw new InputException("Invalid booking operation.");
			}
			ps.setInt(1, booking.getFlightId());
			ps.setInt(2, booking.getSeatNumber());
			ps.setInt(3, booking.getBaggage());
			ps.setString(4, booking.getFlightClass().toString());
			ps.setString(5, booking.getStatus().toString());
			ps.setInt(6, booking.getBookingId());
			row = ps.executeUpdate();
			if (row == 0) {
				throw new DatabaseException("Unable to update flight seat information.");
			}
			conn.commit();
		} catch (SQLException e) {
			throw new DatabaseException("Unable to update flight seat information: " + e.getMessage());
		}
		return row;
	}

}
