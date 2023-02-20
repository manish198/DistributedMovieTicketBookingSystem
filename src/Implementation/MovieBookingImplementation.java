package Implementation;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;

import Interface.AdminInterface;
import Interface.CustomerInterface;
public class MovieBookingImplementation extends UnicastRemoteObject implements AdminInterface,CustomerInterface {
		//Nested Hashmap for movie. <movieName,<movieSlotID,Capacity>>
		protected HashMap<String,HashMap<String,Integer>> movieDataMap=new HashMap<>();		
		//Nested Hashmap for user and movies. <userID<movieName<movieSlotID,numberOfTickets>>>
		protected HashMap<String,HashMap<String,HashMap<String,Integer>>> customerDataMap=new HashMap<>(); 
		
		public String  serverName="";
		public String serverID="";
		
		protected int aPort=4800;
		protected int vPort=4801;
		protected int oPort=4802;
		
		protected final String bookingSuccess="Ticket booked Successfully";
		public MovieBookingImplementation(String serverID) throws Exception {
			super();
			this.serverID=serverID;
		}
		/**
		 * to handle exception by UnicastRemoteObject
		 * @throws Exception
		 */
		public MovieBookingImplementation() throws Exception{
			super();
		}
		
		//Hash Map for log files
		public static HashMap<String,String> file = new HashMap<>();

	    public String log;
	    public String Status;

	    static {
	        file.put("Atwater","Atwater.txt");
	        file.put("Verdun", "Verdun.txt");
	        file.put("Outremont", "Outremont.txt");
	    }
		
	    /**
	     * Log Writter method. This method is invoked to write in log file.
	     * @param operation
	     * @param params
	     * @param status
	     * @param responceDetails
	     */
	    public void logWriter(String operation, String params, String status, String responceDetails) {
	        try {
				FileWriter myWriter = new FileWriter("C:\\Users\\manish\\eclipse-workspace\\DistributedMovieTicketSystem\\src\\Logs\\"+file.get(this.serverName),true);
	            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	            String log = dateFormat.format(LocalDateTime.now()) + " : " + operation + " : " + params + " : " + status
	                    + " : " + responceDetails + "\n";
	            myWriter.write(log);
	            myWriter.close();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	    }
		
		/**
		 * This method implements the sends request to the server using UDP
		 * @param userID
		 * @param movieName
		 * @param movieSlotID
		 * @param numberOfTickets
		 * @param port
		 * @param funcationality
		 * @return
		 * @throws IOException
		 */
		public String sendRequestToServer(String userID,String movieName,String movieSlotID,int numberOfTickets,int port, String funcationality,String newMovieName, String newMovieID) throws IOException {
			try {
				//Send request to server
				DatagramSocket ds=new DatagramSocket();
				ds.setSoTimeout(10000);
				String stringToSend=userID+"#"+movieName+"#"+movieSlotID+"#"+numberOfTickets+"#"+port+"#"+funcationality+"#"+newMovieName+"#"+newMovieID;		//message to send through udp
				byte[] bArray=stringToSend.getBytes();
				InetAddress ia=InetAddress.getLocalHost();
				DatagramPacket dp1=new DatagramPacket(bArray, bArray.length,ia,port);
				ds.send(dp1);
				
				//recieve response from the server
				byte [] b2=new byte[1024];
				DatagramPacket dp2=new DatagramPacket(b2, b2.length);
				try {
					ds.receive(dp2);
				}
				catch (SocketTimeoutException e) {
					 e.printStackTrace();
				}
				
				String response=new String(dp2.getData()).trim();
				ds.close();
				return response;
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		}
		
		/**
		 * This method adds movie slots.
		 */
		public String addMovieSlots(String movieID, String movieName, int bookingCapacity){
			//Not empty and contains the movie case
			if(!movieDataMap.isEmpty() && movieDataMap.containsKey(movieName)) {
				//This map will store the content of the movieDataMap on a temporary basis.
				HashMap <String,Integer> map=new HashMap();
				for (var data : movieDataMap.get(movieName).entrySet()) {
					map.put(data.getKey(),data.getValue());
				}
				if (map.containsKey(movieID)) {
					int updatedCapacity=map.get(movieID)+bookingCapacity;
					map.put(movieID,updatedCapacity);
				}
				else {
					map.put(movieID, bookingCapacity);
				}
				movieDataMap.put(movieName, map);
			}
			//Doesnot contain the movie case
			else {
				HashMap<String,Integer> map = new HashMap<>();
				map.put(movieID,bookingCapacity);
				movieDataMap.put(movieName, map);
			}
			log = "Slots added Successfully";
            Status = "Passed";
            logWriter("Add movie slots",movieID+" "+movieName+" "+bookingCapacity,Status,  movieName + " Slots " + movieID+" "+bookingCapacity + " available");
			return "Movie Slot Added Successfuly ";
		}
		/**
		 * This method removes movie slots.
		 */
		public String removeMovieSlots (String movieID, String movieName){
			String result="";
			boolean removed=false;
			if (!movieDataMap.isEmpty() && movieDataMap.containsKey(movieName) && movieDataMap.get(movieName).containsKey(movieID)) {
				movieDataMap.get(movieName).remove(movieID);
				result="Movie slot removed succefully";
				removed=true;
			}
			else {
				result="Movie Slots doesnot exists";
				removed=false;
			}
			if(removed) {
				log = "Slot removed Successfully";
	            Status = "Passed";
	            logWriter("Remove Movie Slot ",movieID+" "+movieName+" ",Status,  movieName + " Slots " + movieID+" removed successfully.");
			}
			else {
				log = "Slots doesnot exits";
	            Status = "Failed";
	            logWriter("Remove Movie Slot ",movieID, Status ," Failed to remove the slot.");
			}
			return result;
		}
		
		/**
		 * This method shows the availability of the movie.
		 */
		public String listMovieShowsAvailability(String movieName) throws IOException{
			String fromMyServer="";								//response from this server
			String responseOtherServerOne="";					//response from other server
			String responseOtherServerTwo="";					//response from other server
			String function="listMovieShowsAvailability";
			String	allResponse="";								//final response
			if(!movieDataMap.isEmpty()) {
				if(movieDataMap.containsKey(movieName)) {
					for (var data:movieDataMap.get(movieName).entrySet()) {
						fromMyServer= fromMyServer+" Movie Slot:"+ data.getKey() + "Availability:"+data.getValue();
					}
				}
			}
			
			if (this.serverID.equals("ATW")) {
				responseOtherServerOne=sendRequestToServer("",movieName,"",0,oPort,function,null,null);
				responseOtherServerTwo=sendRequestToServer("",movieName,"",0,vPort,function,null,null);
			}
			else if (this.serverID.equals("OUT")) {
				responseOtherServerOne=sendRequestToServer("",movieName,"",0,aPort,function,null,null);
				responseOtherServerTwo=sendRequestToServer("",movieName,"",0,vPort,function,null,null);
			
			}
			else if (this.serverID.equals("VER")) {
				responseOtherServerOne=sendRequestToServer("",movieName,"",0,aPort,function,null,null);
				responseOtherServerTwo=sendRequestToServer("",movieName,"",0,oPort,function,null,null);
				
			}
			allResponse=fromMyServer+" "+responseOtherServerOne+" "+responseOtherServerTwo;
			log = "Show Availability";
            Status = "Success";
            logWriter("Show availability for ",movieName+" ", Status ," Show Availibility Displayed");
			return allResponse;
		}
		
		/**
		 *Method for booking tickets. This will be invoked by both admin and customers. 
		 */
		public String bookMovieTicket(String customerID, String movieID,String movieName, int numberOfTickets) throws IOException {
			String toServer=movieID.substring(0,3).toUpperCase().trim();		//Movie slot ID will give the respective server where the message is targeted to.
			String customerFromServer=customerID.substring(0,3).toUpperCase().trim();	//To check where the current logged in customer is from.
			String result="";
			String function="bookMovieTicket";
			
			
			
			//check for the otherserver and number of tickets greater than 3 conditions.
			if (!customerFromServer.equals(toServer) && numberOfTickets>3 ){
				log = "Book Movie Ticket";
	            Status = "Failed";
	            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status ," user cannot book for than 3 tickets in other server.");
				result="You cannot book more than 3 tickets in other server";
			}
			
			//if the server is same server. 
			else if (this.serverID.equals(toServer)) {
				if(movieDataMap.containsKey(movieName)) {
					if (movieDataMap.get(movieName).containsKey(movieID)) {
						int slotsAvailable=movieDataMap.get(movieName).get(movieID); //number of available slots
						if (slotsAvailable>0) {
							//whether slot is less than the number of tickets to be booked.
							if(slotsAvailable>=numberOfTickets) {			
								if (customerDataMap.containsKey(customerID)) {
									if (customerDataMap.get(customerID).containsKey(movieName)) {
										if (customerDataMap.get(customerID).get(movieName).containsKey(movieID)) {
											int previousNumberOfTicket=customerDataMap.get(customerID).get(movieName).get(movieID); //previously booked ticket number
											int updatedNumberOfTickets=previousNumberOfTicket+numberOfTickets;						//number of ticket of a particular user.
											customerDataMap.get(customerID).get(movieName).put(movieID, updatedNumberOfTickets);
											movieDataMap.get(movieName).put(movieID, slotsAvailable-numberOfTickets);
											log = "Book Movie Ticket";
								            Status = "Success";
								            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" " ,numberOfTickets+ " Movie Tickets booked successfully.");
											result=bookingSuccess;
										}
										else {
											HashMap<String,Integer> map=new HashMap<>();
											for(var data : customerDataMap.get(customerID).get(movieName).entrySet()) {
												map.put(data.getKey(), data.getValue());
											}
											map.put(movieID, numberOfTickets);
											customerDataMap.get(customerID).put(movieName, map);
											movieDataMap.get(movieName).put(movieID, slotsAvailable-numberOfTickets);	//slots deduction after booking
											log = "Book Movie Ticket";
								            Status = "Success";
								            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", numberOfTickets+ " Movie Tickets booked successfully.");
											result=bookingSuccess;
										}
									}
									else {
										HashMap<String,Integer> innerMap=new HashMap<>();
										innerMap.put(movieID, numberOfTickets);
										HashMap <String,HashMap<String, Integer>> map=new HashMap<>();
										for(var data: customerDataMap.get(customerID).entrySet()) {
											map.put(data.getKey(),data.getValue());
										}
										map.put(movieName, innerMap);
										customerDataMap.put(customerID, map);
										movieDataMap.get(movieName).put(movieID, slotsAvailable-numberOfTickets);
										log = "Book Movie Ticket";
							            Status = "Success";
							            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", numberOfTickets+ " Movie Tickets booked successfully.");
										result=bookingSuccess;
									}
								}
								else {
									HashMap<String,Integer> innerMap=new HashMap<>();
									innerMap.put(movieID, numberOfTickets);
									HashMap<String,HashMap<String,Integer>> outerMap=new HashMap<>();
									outerMap.put(movieName, innerMap);
									customerDataMap.put(customerID, outerMap);
									movieDataMap.get(movieName).put(movieID,slotsAvailable-numberOfTickets);
									log = "Book Movie Ticket";
						            Status = "Success";
						            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", numberOfTickets+ " Movie Tickets booked successfully.");
									result=bookingSuccess;
								}
							}
							else {
								log = "Book Movie Ticket";
					            Status = "Failed";
					            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Booking failed because user tried to book more than available ");
								result="You cannot book more than available. Only "+slotsAvailable+" seats available";
							}
						}
						else {
							log = "Book Movie Ticket";
				            Status = "Failed";
				            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Show Houseful ");
							result="No Ticket available for this slot";
						}
						
					}
					else {
						log = "Book Movie Ticket";
						Status = "Failed";
			            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Movie Slot Doesnot exists.");
						result="Movie Slot doesnot exists";
					}
				}
				else {
					log = "Book Movie Ticket";
					Status = "Failed";
		            logWriter("Ticket booking for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Movie Doesnot exists.");
					result="Movie doesnot exists";
				}
				
			}
			//If servers is other.
			else if (toServer.equals("ATW")) {
				result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, aPort,function,"","");
			}
			else if(toServer.equals("OUT")){
				result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, oPort,function,"","");
			}
			else if(toServer.equals("VER")) {
				result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, vPort,function,"","");
			}
			return result;
		}
		
		/**
		 * This method displays the booking schedule of the current user.
		 */
		public String getBookingSchedule(String customerID) throws IOException {
			String function="getBookingSchedule";
			String firstServerReply="";
			String secondServerReply="";
			String finalResult="";
			if(!customerDataMap.isEmpty() && customerDataMap.containsKey(customerID)){
					for (var data:customerDataMap.get(customerID).entrySet()) {
						finalResult=finalResult+" Movie Name: "+data.getKey()+"for slots: "+data.getValue()+"\n";
					}
			}
			
			if (this.serverID.equals("ATW")) {
				firstServerReply=sendRequestToServer(customerID,"","",0,oPort,function,null,null);
				secondServerReply=sendRequestToServer(customerID, "", "", 0, vPort,function,null,null);
			}
			else if (this.serverID.equals("OUT")) {
				firstServerReply=sendRequestToServer(customerID,"","",0,aPort,function,null,null);
				secondServerReply=sendRequestToServer(customerID, "", "", 0, vPort,function,null,null);
			}
			else if (this.serverID.equals("VER")) {
				firstServerReply=sendRequestToServer(customerID,"","",0,aPort,function,null,null);
				secondServerReply=sendRequestToServer(customerID, "", "", 0, oPort,function,null,null);
			}
			finalResult=finalResult+" "+firstServerReply+" "+ secondServerReply;
			log = "Booking Schedule";
			Status = "Success";
            logWriter("Ticket Schedule for ","Username: "+customerID +" ", Status+" ", "Booking Schedule successfully displayed");
			return finalResult;
		}
		
		/**
		 * This is a method to cancel booked movie tickets.
		 */
		public String cancelMovieTickets(String customerID,String movieID, String movieName,int numberOfTickets) throws IOException {
			String toServer=movieID.substring(0,3).toUpperCase().trim();
			String result="";
			String function="cancelMovieTickets";
			if (this.serverID.equals(toServer)) {
				if (movieDataMap.containsKey(movieName)) {
					if(movieDataMap.get(movieName).containsKey(movieID)) {
						if(customerDataMap.containsKey(customerID)) {
							if(customerDataMap.get(customerID).containsKey(movieName)) {
								if(customerDataMap.get(customerID).get(movieName).containsKey(movieID)) {
									int previousBooking=customerDataMap.get(customerID).get(movieName).get(movieID);
									int availability=movieDataMap.get(movieName).get(movieID);
									if(numberOfTickets<=previousBooking) {
										int newNumberOfTicket=previousBooking-numberOfTickets;
										if(newNumberOfTicket>0) {
											customerDataMap.get(customerID).get(movieName).put(movieID, newNumberOfTicket);
											movieDataMap.get(movieName).put(movieID, availability+numberOfTickets);
											
											log = "Cancel Movie Ticket";
											Status = "Success";
								            logWriter("Ticket Canceled for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Movie Ticket cancelled successfully ");
											result="Ticket Canceled";
										}
										else {
											customerDataMap.get(customerID).get(movieName).remove(movieID);
											movieDataMap.get(movieName).put(movieID, availability+numberOfTickets);
											log = "Cancel Movie Ticket";
											Status = "Success";
								            logWriter("Ticket Canceled for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Movie Ticket cancelled successfully ");
											result="Ticket Canceled";
										}
									}
									else {
										log = "Cancel Movie Ticket";
										Status = "Failed";
							            logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " You cannot cancelled for than you booked ");
										result="You cannot canceled more than you booked. You booked "+ previousBooking +" previously";
									}
								}
								else {
									log = "Cancel Movie Ticket";
									Status = "Failed";
						            logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " No booking slot available ");
									result="There is no booking for this slot";
								}
							}
							else {
								log = "Cancel Movie Ticket";
								Status = "Failed";
					            logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " No booking for this movie available ");
								result="There is no booking for this movie";
							}
						}
						else {
							log = "Cancel Movie Ticket";
							Status = "Failed";
				            logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Nothing booked for the customer ");
							result="There is no booking for the customer";
						}
					}
					else {
						log = "Cancel Movie Ticket";
						Status = "Failed";
			            logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Slot does not exists ");
						result="Movie slot not found";
					}
				}
				else {
					log = "Cancel Movie Ticket";
					Status = "Failed";
		            logWriter("Ticket Canceled failed for ","Username: "+customerID+" for movie "+movieName +" for slot: "+movieID+" ", Status+" ", " Movie doesnot exists ");
					result="Movie doesnot exists";
				}
				
			}
			else if (toServer.equals("ATW")) {
				result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, aPort,function,"","");
			}
			else if(toServer.equals("OUT")){
				result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, oPort,function,"","");
			}
			else if(toServer.equals("VER")) {
				result=sendRequestToServer(customerID, movieName, movieID, numberOfTickets, vPort,function,"","");
			}
			return result;
		}
		
		/**
		 * This methods list all the availabilty of particular moive from other servers.
		 * @param movieName
		 * @return
		 * @throws IOException
		 */
		public String listMovieShowsAvailabilityFromOther(String movieName) throws IOException{
			String	allResponse="";
			if(!movieDataMap.isEmpty()) {
				if(movieDataMap.containsKey(movieName)) {
					for (var data:movieDataMap.get(movieName).entrySet()) {
						allResponse=allResponse+" Movie Slot:"+ data.getKey() + "Availability:"+data.getValue();
					}
				}
			}
			return allResponse;
		}
		
		
		/**
		 * This method returns booking schedule from other servers.
		 * @param customerID
		 * @return
		 */
		public String getBookingScheduleFromOther(String customerID) {
			String result="";
			if(!customerDataMap.isEmpty() && customerDataMap.containsKey(customerID)){
				for (var data:customerDataMap.get(customerID).entrySet()) {
					result=result+" Movie Name: "+data.getKey()+" for slots: "+data.getValue()+"\n";
				}
			}
			return result;
		}
		
		public String exchangeTickets(String customerID,String movieName, String movieID,String newMovieID,String newMovieName,int numberOfTicketsToCancel) throws IOException{
			String funcationality="exchangeTickets";
			String result="";
			String customerFromServer=customerID.substring(0,3).toUpperCase().trim();
			String movieBookedFromServer=movieID.substring(0,3).toUpperCase().trim();
			String newMovieToBookServer=newMovieID.substring(0,3).toUpperCase().trim();
			
			if(movieBookedFromServer.equals(customerFromServer)) {
				if(!customerDataMap.isEmpty() && customerDataMap.containsKey(customerID)) {
					if(customerDataMap.get(customerID).containsKey(movieName)){
						if(customerDataMap.get(customerID).get(movieName).containsKey(movieID)) {
							int numberOfTicketsBooked=customerDataMap.get(customerID).get(movieName).get(movieID);
							if(numberOfTicketsBooked>=numberOfTicketsToCancel) {
								if(newMovieToBookServer.equals(movieBookedFromServer)) {
									if(movieDataMap.get(newMovieName).get(newMovieID)>=numberOfTicketsToCancel) {
										//Update movieMap
										cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
										bookMovieTicket(customerID, newMovieID, newMovieName, numberOfTicketsToCancel);
										result="Ticket Exchanged Successfully";
									}
									else {
										result="Not Sufficient Seat Available";
									}
								}
								else if(newMovieToBookServer.equals("ATW")){
									funcationality="bookingInExchange";
									String bookMovierequest=sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,aPort,funcationality,newMovieName,newMovieID);
									if(bookMovierequest.equals(bookingSuccess)) {
										//Cancelling after successful booking
										cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
										result= "Ticket Exchanged Successfully";
									}
									else {
										result= bookMovierequest;
									}
								}
								else if(newMovieToBookServer.equals("VER")){
									funcationality="bookingInExchange";
									String bookMovierequest=sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,vPort,funcationality,newMovieName,newMovieID);
									if(bookMovierequest.equals(bookingSuccess)) {
										//Cancelling after successful booking
										cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
										result= bookMovierequest;
									}
									else {
										result= bookMovierequest;
									}
								}
								else if(newMovieToBookServer.equals("OUT")){
									funcationality="bookingInExchange";
									String bookMovierequest=sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,oPort,funcationality,newMovieName,newMovieID);
									if(bookMovierequest.equals(bookingSuccess)) {
										//Cancelling after successful booking
										cancelMovieTickets(customerID, movieID, movieName, numberOfTicketsToCancel);
										result= bookMovierequest;
									}
									else {
										result= bookMovierequest;
									}
								}
							}
							else {
								result=" You cannot cancel more than booked";
							}
						}
						else {
							result=" Customer have not booked ticket for this slot. ";
						}
					}
					else {
						result=" Customer doesnot have booking for this movie";
					}
				}
				else {
					result=" Customer Doesnot exists";
				}
			}
			
			else if(movieBookedFromServer.equals("ATW")) {
				result=sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,aPort,funcationality,newMovieName,newMovieID);
//				(String userID,String movieName,String movieSlotID,int numberOfTickets,int port, String funcationality)
//				String customerID,String movieName, String movieID,String newMovieID,String newMovieName,int numberOfTicketsToCancel
			}
			
			else if(movieBookedFromServer.equals("OUT")) {
				result=sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,oPort,funcationality,newMovieName,newMovieID);
			}
			
			else if(movieBookedFromServer.equals("VER")) {
				result=sendRequestToServer(customerID, movieName, movieID, numberOfTicketsToCancel,vPort,funcationality,newMovieName,newMovieID);
			}
			
			return result;
		}
		
}