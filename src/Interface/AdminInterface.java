package Interface;
import java.io.IOException;
import java.rmi.*;

public interface AdminInterface extends Remote{
	/**
	 * 
	 * @param movieID
	 * @param movieName
	 * @param bookingCapacity
	 * @return
	 * @throws RemoteException
	 */
	public String addMovieSlots(String movieID, String movieName, int bookingCapacity) throws RemoteException;
	
	/**
	 * 
	 * @param movieID
	 * @param movieName
	 * @return
	 */
	public String removeMovieSlots (String movieID, String movieName) throws RemoteException;
	
	/**
	 * 
	 * @param movieName
	 */
	public String listMovieShowsAvailability(String movieName) throws IOException;
	
	/**
	 * 
	 * @param customerID
	 * @param movieID
	 * @param movieName
	 * @param numberOfTickets
	 * @return
	 * @throws IOException
	 */
	public String bookMovieTicket(String customerID, String movieID,String movieName, int numberOfTickets) throws IOException;

	/**
	 * 
	 * @param customerID
	 * @return
	 * @throws IOException
	 */
	public String getBookingSchedule(String customerID) throws IOException;
	
	/**
	 * 
	 * @param customerID
	 * @param movieID
	 * @param movieName
	 * @param numberOfTicekts
	 * @return
	 * @throws IOException
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
