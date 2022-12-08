package laserlads;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CountData {

    public static boolean isAdmin(String email) {
        String SQL_QUERY = "select COUNT(email) from admins where email = ?";

        try (Connection con = DBConn.getConnection()) {
            try (PreparedStatement pst = con.prepareStatement(SQL_QUERY)) {
                pst.setString(1, email);
                try (ResultSet rs = pst.executeQuery()) {
                    rs.next();
                    if (rs.getInt("COUNT(email)") == 1) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static int fetchCount(String id) throws SQLException {
        String SQL_QUERY = "select * from lots where name = ?";
        int lotTotal = 0, takenSpots = 1;
        int lotCount = -1;

        try (Connection con = DBConn.getConnection()) {
            try (PreparedStatement pst = con.prepareStatement(SQL_QUERY)) {
                pst.setString(1, id);
                try (ResultSet rs = pst.executeQuery()) {
                    rs.next();
                    lotTotal = rs.getInt("total");
                    takenSpots = rs.getInt("taken");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (takenSpots > -1 && takenSpots <= lotTotal) {
            lotCount = lotTotal - takenSpots;
        }
        return lotCount;
    }

    public static String updateTaken(String id, int taken) throws SQLException {
        String SQL_QUERY = "update lots set taken = ? where name = ?";

        try (Connection con = DBConn.getConnection()){
            try (PreparedStatement pst = con.prepareStatement(SQL_QUERY)) {
                if (taken > -1) {
                    pst.setInt(1, taken);
                } else {
                    pst.setInt(1, 0);
                }
                pst.setString(2, id);
                pst.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Update Failed";
        }
        return "Update Successful";
    }

    public static String updateLot(String id, int taken, int total) throws SQLException {
        String SQL_QUERY = "update lots set taken = ?, total = ? where name = ?";

        try (Connection con = DBConn.getConnection()){
            try (PreparedStatement pst = con.prepareStatement(SQL_QUERY)) {
                if (taken > -1) {
                    pst.setInt(1, taken);
                } else {
                    return "Update Failed: Bad integer for spots taken";
                }

                if (total > -1) {
                    pst.setInt(2, total);
                } else {
                    return "Update Failed: Bad integer for new lot total";
                }

                pst.setString(3, id);
                pst.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Update Failed";
        }
        return "Update Successful";
    }

    public static String addNewLot(String id, int taken, int total) throws SQLException {
        String SQL_QUERY = "insert into lots values(?,?,?)";

        try (Connection con = DBConn.getConnection()){
            try (PreparedStatement pst = con.prepareStatement(SQL_QUERY)) {
                pst.setString(1, id);

                if (taken > -1) {
                    pst.setInt(2, taken);
                } else {
                    return "Update Failed: Bad integer for spots taken";
                }

                if (total > -1) {
                    pst.setInt(3, total);
                } else {
                    return "Update Failed: Bad integer for new lot total";
                }
                pst.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Update Failed";
        }
        return "Update Successful";
    }

    public static String dropLot(String id) throws SQLException {
        String SQL_QUERY = "delete from lots where name = ?";

        try (Connection con = DBConn.getConnection()){
            try (PreparedStatement pst = con.prepareStatement(SQL_QUERY)) {
                pst.setString(1, id);
                pst.executeUpdate();
                con.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Update Failed";
        }
        return "Update Successful";
    }

    public static boolean checkForUser(String email) throws SQLException {
        String SQL_QUERY = "select COUNT(*) from logins where email = ?";

        try (Connection con = DBConn.getConnection()) {
            try (PreparedStatement pst = con.prepareStatement(SQL_QUERY)) {
                pst.setString(1, email);
                try (ResultSet rs = pst.executeQuery()) {
                    rs.next();
                    int numUsers = rs.getInt("COUNT(*)");
                    if (numUsers == 0) {
                        return false;
                    }
                    else {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean passwordMatch(String email, String plain) throws SQLException {
        String SQL_QUERY = "select password from logins WHERE email = ?";

        try (Connection con = DBConn.getConnection()) {
            try (PreparedStatement pst = con.prepareStatement(SQL_QUERY)) {
                pst.setString(1, email);
                try (ResultSet rs = pst.executeQuery()) {
                    rs.next();
                    String hash = rs.getString("password");
                    if (Laser_Bcrypt.password_match(plain, hash))
                        return true;
                    else
                        return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addNewUser(String email, String hash) throws SQLException {
        String SQL_QUERY = "insert into logins (email, password) values(?,?)";

        try (Connection con = DBConn.getConnection()){
            try (PreparedStatement pst = con.prepareStatement(SQL_QUERY)) {
                pst.setString(1, email);
                pst.setString(2, hash);
                pst.executeUpdate();
                return true;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
