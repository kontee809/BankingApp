package bank;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankClient {
    private static BankInterface mbBankService;
    private static BankInterface agribankService;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            mbBankService = (BankInterface) registry.lookup("MBBankService");
            agribankService = (BankInterface) registry.lookup("AgribankService");

            JFrame frame = new JFrame("Bank Client");
            frame.setSize(400, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(null);

            // Components for Registration
            JLabel registerLabel = new JLabel("Register");
            registerLabel.setBounds(150, 10, 100, 30);
            frame.add(registerLabel);

            JLabel usernameLabel = new JLabel("Username:");
            usernameLabel.setBounds(30, 50, 100, 30);
            frame.add(usernameLabel);

            JTextField usernameField = new JTextField();
            usernameField.setBounds(150, 50, 200, 30);
            frame.add(usernameField);

            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setBounds(30, 90, 100, 30);
            frame.add(passwordLabel);

            JPasswordField passwordField = new JPasswordField();
            passwordField.setBounds(150, 90, 200, 30);
            frame.add(passwordField);

            JButton registerButton = new JButton("Register");
            registerButton.setBounds(150, 130, 100, 30);
            frame.add(registerButton);

            // Components for Login
            JLabel loginLabel = new JLabel("Login");
            loginLabel.setBounds(150, 170, 100, 30);
            frame.add(loginLabel);

            JButton loginButton = new JButton("Login");
            loginButton.setBounds(150, 210, 100, 30);
            frame.add(loginButton);

            registerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String username = usernameField.getText();
                        String password = new String(passwordField.getPassword());
                        
                        // Đồng bộ đăng ký cả 2 dịch vụ
                        boolean mbSuccess = mbBankService.register(username, password);
                        boolean agribankSuccess = agribankService.register(username, password);
                        
                        if (mbSuccess && agribankSuccess) {
                            JOptionPane.showMessageDialog(frame, "Đăng ký thành công trên cả hai ngân hàng!");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Đăng ký không thành công trên ít nhất một ngân hàng!");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String username = usernameField.getText();
                        String password = new String(passwordField.getPassword());
                        
                        // Chỉ đăng nhập vào ngân hàng MB
                        double balance = mbBankService.login(username, password);
                        if (balance >= 0) {
                            JOptionPane.showMessageDialog(frame, "Đăng nhập thành công! Số dư: " + balance);
                            // Mở giao diện chuyển tiền
                            showTransferInterface(frame, username, balance);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Đăng nhập không thành công!");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showTransferInterface(JFrame parentFrame, String username, double initialBalance) {
        JFrame transferFrame = new JFrame("Transfer Money");
        transferFrame.setSize(400, 300);
        transferFrame.setLayout(null);

        // Hiển thị tên tài khoản
        JLabel accountLabel = new JLabel("Tài khoản: " + username);
        accountLabel.setBounds(30, 10, 200, 30);
        transferFrame.add(accountLabel);

        // Hiển thị số dư
        JLabel balanceLabel = new JLabel("Số dư: " + initialBalance);
        balanceLabel.setBounds(30, 40, 200, 30);
        transferFrame.add(balanceLabel);

        JLabel toLabel = new JLabel("Đến tài khoản:");
        toLabel.setBounds(30, 80, 100, 30);
        transferFrame.add(toLabel);

        JTextField toField = new JTextField();
        toField.setBounds(150, 80, 200, 30);
        transferFrame.add(toField);

        JLabel amountLabel = new JLabel("Số tiền:");
        amountLabel.setBounds(30, 120, 100, 30);
        transferFrame.add(amountLabel);

        JTextField amountField = new JTextField();
        amountField.setBounds(150, 120, 200, 30);
        transferFrame.add(amountField);

        JButton transferButton = new JButton("Chuyển tiền");
        transferButton.setBounds(150, 160, 100, 30);
        transferFrame.add(transferButton);

        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String toAccount = toField.getText();
                    double amount = Double.parseDouble(amountField.getText());

                    // Chuyển tiền từ ngân hàng MB
                    if (mbBankService.transfer(username, toAccount, amount)) {
                        JOptionPane.showMessageDialog(transferFrame, "Chuyển tiền thành công từ MB Bank!");

                        // Cập nhật số dư sau khi chuyển tiền
                        double newBalance = mbBankService.getBalance(username); // Gọi phương thức lấy số dư
                        balanceLabel.setText("Số dư: " + newBalance); // Cập nhật nhãn số dư
                    } else {
                        JOptionPane.showMessageDialog(transferFrame, "Chuyển tiền không thành công từ MB Bank!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        transferFrame.setVisible(true);
    }
}
