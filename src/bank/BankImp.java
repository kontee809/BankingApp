package bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BankImp extends UnicastRemoteObject implements BankInterface {

    private Connection connection;

        public BankImp(String database) throws RemoteException {
        try {
            // Kết nối tới cơ sở dữ liệu dựa trên tham số truyền vào
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + database, "root", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getBalance(String username) throws RemoteException {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT balance FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // -1 nếu không tìm thấy
    }

    @Override
    public boolean register(String username, String password) throws RemoteException {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO users(username, password, balance) VALUES (?, ?, 100000)");
            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public double login(String username, String password) throws RemoteException {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT balance FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // -1 nếu không thành công
    }

    @Override
    public double checkBalance(String username) throws RemoteException {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT balance FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // -1 nếu không tìm thấy
    }

    @Override
    public boolean transfer(String fromAccount, String toAccount, double amount) throws RemoteException {
        // Khai báo kết nối và PreparedStatement
        Connection mbConnection = null;
        Connection agribankConnection = null;

        try {
            mbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mb_bank", "root", "");
            agribankConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/agribank", "root", "");

            mbConnection.setAutoCommit(false); // Bắt đầu giao dịch MB Bank
            agribankConnection.setAutoCommit(false); // Bắt đầu giao dịch Agribank

            // Kiểm tra số dư tài khoản gửi trong MB Bank
            double balance = checkBalance(fromAccount);
            if (balance >= amount) {
                // Cập nhật số dư tài khoản gửi trong MB Bank
                PreparedStatement stmt1 = mbConnection.prepareStatement("UPDATE users SET balance = balance - ? WHERE username = ?");
                stmt1.setDouble(1, amount);
                stmt1.setString(2, fromAccount);
                stmt1.executeUpdate();

                // Cập nhật số dư tài khoản nhận trong MB Bank
                PreparedStatement stmt2 = mbConnection.prepareStatement("UPDATE users SET balance = balance + ? WHERE username = ?");
                stmt2.setDouble(1, amount);
                stmt2.setString(2, toAccount);
                stmt2.executeUpdate();

                // Lưu thông tin giao dịch vào bảng transactions trong MB Bank
                PreparedStatement stmt3 = mbConnection.prepareStatement("INSERT INTO transactions(from_account, to_account, amount) VALUES (?, ?, ?)");
                stmt3.setString(1, fromAccount);
                stmt3.setString(2, toAccount);
                stmt3.setDouble(3, amount);
                stmt3.executeUpdate();

                // Cập nhật số dư tài khoản nhận trong Agribank
                PreparedStatement stmtAgribankReceive = agribankConnection.prepareStatement("UPDATE users SET balance = balance + ? WHERE username = ?");
                stmtAgribankReceive.setDouble(1, amount);
                stmtAgribankReceive.setString(2, toAccount);
                stmtAgribankReceive.executeUpdate();

                // Cập nhật số dư tài khoản gửi trong Agribank
                PreparedStatement stmtAgribankSend = agribankConnection.prepareStatement("UPDATE users SET balance = balance - ? WHERE username = ?");
                stmtAgribankSend.setDouble(1, amount);
                stmtAgribankSend.setString(2, fromAccount);
                stmtAgribankSend.executeUpdate();

                // Lưu thông tin giao dịch vào bảng transactions trong Agribank
                PreparedStatement stmtAgribankTransaction = agribankConnection.prepareStatement("INSERT INTO transactions(from_account, to_account, amount) VALUES (?, ?, ?)");
                stmtAgribankTransaction.setString(1, fromAccount);
                stmtAgribankTransaction.setString(2, toAccount);
                stmtAgribankTransaction.setDouble(3, amount);
                stmtAgribankTransaction.executeUpdate();

                // Hoàn tất giao dịch
                mbConnection.commit();
                agribankConnection.commit();
                return true; // Chuyển tiền thành công
            } else {
                return false; // Không đủ tiền
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (mbConnection != null) {
                    mbConnection.rollback(); // Hoàn tác MB Bank
                }
                if (agribankConnection != null) {
                    agribankConnection.rollback(); // Hoàn tác Agribank
                }
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (mbConnection != null) {
                    mbConnection.close();
                }
                if (agribankConnection != null) {
                    agribankConnection.close();
                }
            } catch (Exception closeEx) {
                closeEx.printStackTrace();
            }
        }
        return false; // Nếu có lỗi xảy ra
    }
}
