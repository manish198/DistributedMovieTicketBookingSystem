package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Naming;

import Implementation.MovieBookingImplementation;



public class AtwaterServer {
	
	public static void main(String[] args) throws Exception{
		DatagramSocket socket = new DatagramSocket(4800);
		MovieBookingImplementation atwMovieBookingImplementation= new MovieBookingImplementation("ATW");
		atwMovieBookingImplementation.serverName="Atwater";
		Naming.rebind("rmi:/atw",atwMovieBookingImplementation);
		System.out.println("Atwater Server Started");
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		        socket.close();
		    }
		});
		while(true){
			requestHandler(atwMovieBookingImplementation,socket);
		}
//		socket.close();
	}
	
	
	
	public static void requestHandler(MovieBookingImplementation atwMovieBookingImplementation,DatagramSocket socket) throws IOException{
		//Send request to get data.
		try {
			String responseReturn="";
			byte[] b=new byte[1024];
			DatagramPacket dp1=new DatagramPacket(b, b.length);
			socket.receive(dp1);
			
			//Now to send response
			String request=new String(dp1.getData()).trim();
			String [] requestMessageArray=request.split("#");
			
			String userID=requestMessageArray[0];
			String movieName=requestMessageArray[1];
			String movieSlotID=requestMessageArray[2];
			int numberOfTickets=Integer.parseInt(requestMessageArray[3]);
			int port=Integer.parseInt(requestMessageArray[4]);
			String function=requestMessageArray[5];
			String newMovieName=requestMessageArray[6];
			String newMovieID=requestMessageArray[7];
			switch(function) {
				case "listMovieShowsAvailability":{
					String result= atwMovieBookingImplementation.listMovieShowsAvailabilityFromOther(movieName);
					responseReturn=result;
					break;
				}
				case "bookMovieTicket":{
					String result=atwMovieBookingImplementation.bookMovieTicket(userID, movieSlotID, movieName, numberOfTickets);
					responseReturn=result;
					break;
				}
				case "getBookingSchedule":{
					String result= atwMovieBookingImplementation.getBookingScheduleFromOther(userID);
					responseReturn=result;
					break;
				}
				case "cancelMovieTickets":{
					String result=atwMovieBookingImplementation.cancelMovieTickets(userID, movieSlotID, movieName, numberOfTickets);
					responseReturn=result;
					break;
				}
				case "exchangeTickets":{
					String result=atwMovieBookingImplementation.exchangeTickets(userID, movieName, movieSlotID, newMovieID, newMovieName, numberOfTickets);
					responseReturn=result;
					break;
				}
				case "bookingInExchange":{
					String result=atwMovieBookingImplementation.bookMovieTicket(userID, newMovieID, newMovieName, numberOfTickets);
					responseReturn=result;
					break;
				}
				default:{
					break;
				}
			}
			
			
			String message=responseReturn;
			byte[] b2=message.getBytes();
			DatagramPacket dp2=new DatagramPacket(b2, b2.length,dp1.getAddress(),dp1.getPort());
			socket.send(dp2);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return;
	}

}


//public class AtWaterRunnable implements Runnable{
//	MovieBookingImplementation atwMovieBookingImplementation;
//	DatagramSocket socket;
//	public AtWaterRunnable(MovieBookingImplementation atwObject,DatagramSocket atwSocket) {
//		this.atwMovieBookingImplementation=atwObject;
//		socket=atwSocket;
//	}
//	
//	public void run() {
//		try {
//			requestHandler(atwMovieBookingImplementation,socket);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//}
//Thread t= new Thread(new AtWaterRunnable(atwMovieBookingImplementation, socket));
//t.start();