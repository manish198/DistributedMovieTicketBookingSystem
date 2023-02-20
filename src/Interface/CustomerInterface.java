package Interface;
import java.io.IOException;
import java.rmi.*;

public interface CustomerInterface extends Remote {
	
	/**
	 * 
	 * @param customerID
	 * @param movieID
	 * @param movieName
	 * @param numberOfTickets
	 * @return
	 * @throws RemoteException
	 */
	public String bookMovieTicket(String customerID, String movieID,String movieName, int numberOfTickets) throws IOException;

	/**
	 * 
	 * @param customerID
	 * @return
	 * @throws RemoteException
	 */
	public String getBookingSchedule(String customerID) throws IOException;

	/**
	 * 
	 * @param customerID
	 * @param movieID
	 * @param movieName
	 * @param numberOfTicekts
	 * @return
	 * @throws RemoteException
	 */
	public String cancelMovieTickets(String customerID,String movieID, String movieName,int numberOfTicekts) throws IOException;

	/**
	 * 
	 * @param customerID
	 * @param movieID
	 * @param newMovieID
	 * @param newMovieName
	 * @param numberOfTickets
	 * @return
	 * @throws IOException
	 */
	public String exchangeTickets(String customerID,String movieName, String movieID,String newMovieID,String newMovieName,int numberOfTicketsToCancel) throws IOException;
	
}
