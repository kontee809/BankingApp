package bank;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankServer {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);

            // Đăng ký dịch vụ cho MB Bank
            BankInterface mbBankService = new BankImp("mb_bank");
            registry.rebind("MBBankService", mbBankService);

            // Đăng ký dịch vụ cho Agribank
            BankInterface agribankService = new BankImp("agribank");
            registry.rebind("AgribankService", agribankService);

            while (true) {
                System.out.println("Bank Server is running...");
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
