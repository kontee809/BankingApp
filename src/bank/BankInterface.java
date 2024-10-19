package bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankInterface extends Remote {
    boolean register(String username, String password) throws RemoteException;
    double login(String username, String password) throws RemoteException;
    double checkBalance(String username) throws RemoteException;
    boolean transfer(String fromAccount, String toAccount, double amount) throws RemoteException;
    double getBalance(String username) throws RemoteException;
}
