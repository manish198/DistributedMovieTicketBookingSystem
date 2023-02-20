package Static;

public class ServerAddress {
		private static String atwAPI="rmi:/atw";
		private static String outAPI="rmi:/out";
		private static String verAPI="rmi:/ver";
		
		public static String getAtwaterServerAPI() {
			return atwAPI;
		}
		
		public static String getOutremontServerAPI() {
			return outAPI;
		}
		
		public static String getVerdunServerAPI() {
			return verAPI;
		}
		
}


