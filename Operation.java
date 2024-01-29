import java.sql.Connection;
import java.util.* ;

import org.sqlite.SQLiteException;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Operation {
  
	public static void viewAllAccountDetails() {
		String URL = "jdbc:sqlite:src/yoober_project.db";
	       try (Connection conn = DriverManager.getConnection(URL);
	             Statement st = conn.createStatement()) {

	            String query = "SELECT a.FIRST_NAME, a.LAST_NAME, adr.STREET, adr.CITY, adr.PROVINCE, adr.POSTAL_CODE, " +
	                    "a.PHONE_NUMBER, a.EMAIL, " +
	                    "CASE " +
	                    "WHEN d.ID IS NOT NULL AND p.ID IS NOT NULL THEN 'Both Passenger and Driver' " +
	                    "WHEN d.ID IS NOT NULL THEN 'Driver' " +
	                    "WHEN p.ID IS NOT NULL THEN 'Passenger' " +
	                    "ELSE 'Unknown' " +
	                    "END AS ACCOUNT_TYPE " +
	                    "FROM accounts a " +
	                    "JOIN addresses adr ON a.ADDRESS_ID = adr.ID " +
	                    "LEFT JOIN passengers p ON a.ID = p.ID " +
	                    "LEFT JOIN drivers d ON a.ID = d.ID ";

	            ResultSet set = st.executeQuery(query);

	            while (set.next()) {
	                String firstName = set.getString("FIRST_NAME");
	                String lastName = set.getString("LAST_NAME");
	                String street = set.getString("STREET");
	                String city = set.getString("CITY");
	                String province = set.getString("PROVINCE");
	                String postalCode = set.getString("POSTAL_CODE");
	                String phoneNumber = set.getString("PHONE_NUMBER");
	                String email = set.getString("EMAIL");
	                String accountType = set.getString("ACCOUNT_TYPE");

	                System.out.println("First Name: " + firstName);
	                System.out.println("Last Name: " + lastName);
	                System.out.println("Full Address: " + street + ", " + city + ", " + province + ", " + postalCode);
	                System.out.println("Phone Number: " + phoneNumber);
	                System.out.println("Email: " + email);
	                System.out.println("Account Type: " + accountType);
	                System.out.println("\n__________________________________________\n");
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
		
	public static void calculateAverageRating() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the email address of the driver: ");
        String driverEmail = scanner.nextLine();
        String JDBC_URL = "jdbc:sqlite:src/yoober_project.db";
        try (Connection connection = DriverManager.getConnection(JDBC_URL);) {
        	String query = "SELECT AVG(rides.RATING_FROM_PASSENGER) AS AVERAGE_RATING " +
                    "FROM rides " +
                    "JOIN drivers ON rides.DRIVER_ID = drivers.ID " +
                    "JOIN accounts  ON drivers.ID = accounts.ID " +
                    "WHERE accounts.EMAIL = ?" ;
        	PreparedStatement statement = connection.prepareStatement(query) ;
            statement.setString(1, driverEmail);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double averageRating = resultSet.getDouble("AVERAGE_RATING");
                System.out.println("Average Rating for Driver with email " + driverEmail + ": " + averageRating);
            } else {
                System.out.println("Driver with email " + driverEmail + " not found or has no ratings.");
            }
            System.out.println("\n__________________________________________\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public static void calculateTotalMoneySpent() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the email address of the passenger: ");
        String passengerEmail = scanner.nextLine();
        String URL = "jdbc:sqlite:src/yoober_project.db";
        try (Connection connection = DriverManager.getConnection(URL);) {
            String query =  "SELECT SUM(r.CHARGE) AS TOTAL_MONEY " +
                    "FROM rides r " +
                    "JOIN ride_requests rq ON r.REQUEST_ID = rq.ID " +
                    "JOIN passengers p ON rq.PASSENGER_ID = p.ID " +
                    "JOIN accounts a ON p.ID = a.ID " +
                    "WHERE a.EMAIL = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, passengerEmail);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double totalAmount = resultSet.getDouble("TOTAL_MONEY");
                System.out.println("Total Money Spent by Passenger with email " + passengerEmail + ": " + totalAmount +" CAD $");
            } else {
                System.out.println("Passenger with email " + passengerEmail + " not found or has no ride history.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	
	public static String getUserInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt);
        return scanner.nextLine();
    }
	
	public static int getIntInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid integer.");
            scanner.next(); // consume invalid input
        }
        return scanner.nextInt();
    }
	 public static void insertPassengerDetails(Connection connection, int accountID, String creditCardNumber) throws SQLException {
	        String passengerQuery = "INSERT INTO passengers (ID, CREDIT_CARD_NUMBER) VALUES (?,?)";
	        try (PreparedStatement passengerStatement = connection.prepareStatement(passengerQuery)) {
	            passengerStatement.setInt(1, accountID);
	            passengerStatement.setString(2, creditCardNumber);
	            passengerStatement.executeUpdate();
	        }
	    }

	    public static void insertDriverDetails(Connection connection, int accountID, String licenseNumber, String licenseExpiryDate) throws SQLException {
	        String driversQuery = "INSERT INTO drivers (ID, LICENSE_ID) VALUES(?,?)";
	        try (PreparedStatement driversStatement = connection.prepareStatement(driversQuery)){
	            driversStatement.setInt(1, accountID);
	            int licenseID = insertLicense (connection, licenseNumber, licenseExpiryDate);
	            driversStatement.setInt(2, licenseID);
	            driversStatement.executeUpdate();   
	        }
	    }
	    public static int insertLicense(Connection connection, String licenseNumber, String licenseExpiryDate) throws SQLException{
	        String licenseQuery = "INSERT INTO licenses (NUMBER, EXPIRY_DATE) VALUES (?,?)";
	        try(PreparedStatement licenseStatement = connection.prepareStatement(licenseQuery, Statement.RETURN_GENERATED_KEYS)){
	            licenseStatement.setString(1, licenseNumber);
	            licenseStatement.setString(2, licenseExpiryDate);
	            int affectedRows = licenseStatement.executeUpdate();

	            if (affectedRows==0){
	                throw new SQLException("Creating license record failed, no rows affected.");
	            }
	            try (ResultSet generatedKeys = licenseStatement.getGeneratedKeys()){
	                if (generatedKeys.next()){
	                    return generatedKeys.getInt(1);
	                }else{
	                    throw new SQLException("Creating license record failed, no ID obtained.");
	                }
	            }
	        }
	    }
		
    public static void createNewAccount() {
    	String URL = "jdbc:sqlite:src/yoober_project.db";
        try (Connection connection = DriverManager.getConnection(URL)) {
            String firstName = getUserInput("Enter First Name: ");
            String lastName = getUserInput("Enter Last Name: ");
            String birthdate = getUserInput("Enter Birthdate (YYYY-MM-DD): ");
            String street = getUserInput("Enter Street Address: ");
            String city = getUserInput("Enter City: ");
            String province = getUserInput("Enter Province: ");
            String postalCode = getUserInput("Enter Postal Code: ");
            String phoneNumber = getUserInput("Enter Phone Number: ");
            String emailAddress = getUserInput("Enter Email Address: ");

            String insertAccountQuery = "INSERT INTO accounts (ID,FIRST_NAME, LAST_NAME, BIRTHDATE, PHONE_NUMBER, EMAIL, ADDRESS_ID) VALUES(?,?,?,?,?,?,?)";
            try (PreparedStatement insertAccountStatement = connection.prepareStatement(insertAccountQuery,Statement.RETURN_GENERATED_KEYS)) {
                insertAccountStatement.setString(2, firstName);
                insertAccountStatement.setString(3, lastName);
                insertAccountStatement.setString(4, birthdate);
                insertAccountStatement.setString(5, phoneNumber);
                insertAccountStatement.setString(6, emailAddress);
                
                ResultSet Keys = insertAccountStatement.getGeneratedKeys(); 
                        int accountID = Keys.getInt(1);
                        insertAccountStatement.setInt(1, accountID);
                  

                String insertAddressQuery = "INSERT INTO addresses (ID,STREET, CITY, PROVINCE, POSTAL_CODE) VALUES(?,?,?,?,?)";
                
                try (PreparedStatement insertAddressStatement = connection.prepareStatement(insertAddressQuery,Statement.RETURN_GENERATED_KEYS)) {
                    insertAddressStatement.setString(2, street);
                    insertAddressStatement.setString(3, city);
                    insertAddressStatement.setString(4, province);
                    insertAddressStatement.setString(5, postalCode);

                    int affectedRows = insertAddressStatement.executeUpdate();

                    if (affectedRows == 0) {
                        throw new SQLException("Creating address record failed, no rows affected.");
                    }

                    try (ResultSet generatedKeys = insertAccountStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int addressID = generatedKeys.getInt(1);
                            insertAccountStatement.setInt(7, addressID);
                            insertAddressStatement.setInt(1, addressID);
                            
                            affectedRows = insertAccountStatement.executeUpdate();

                            if (affectedRows == 0) {
                                throw new SQLException("Creating address record failed, no rows affected.");    
                            }
                            String role = getUserInput("Will this account be used by a Passenger, Driver, or Both?(P/D/B): ").toUpperCase();

                            if (role.equals("P")){
                                String creditCardNumber = getUserInput("Enter Credit Card Number: ");
                                insertPassengerDetails(connection, accountID, creditCardNumber);

                            }else if (role.equals("D")){
                                String licenseNumber = getUserInput("Enter Driver's License Number: ");
                                String licenseExpiryDate = getUserInput("Enter Driver's License Expiry Date");
                                insertDriverDetails(connection, accountID, licenseNumber, licenseExpiryDate);
                                
                            }else if (role.equals("B")){
                                String creditCardNumber = getUserInput("Enter Credit Card Number");
                                String licenseNumber = getUserInput("Enter Driver's License Number");
                                String licenseExpiryDate = getUserInput("Enter Driver's License Expiry Date");
                                insertPassengerDetails(connection, accountID, creditCardNumber);
                                insertDriverDetails(connection, accountID, licenseNumber, licenseExpiryDate);
                            
                            }else {
                                System.out.println("Invalid role. Please choose 'passenger', 'driver', or 'both'.");
                            }

                            System.out.println("New account created successfully!");
                            viewAllAccountDetails();

                        
                        }else {
                            throw new SQLException("Creating address record failed, no ID obtained.");
                        }
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    // for 5 part
    
  public static void BookRide(Connection  conn,int ID,String passengerEmail) {
	 
      String date = getUserInput("Enter Date of pickup (August 8, 2022- format): ");
      String time = getUserInput("Enter Time of pickup: ");
      int Number_of_riders = getIntInput("Enter Number of Riders: ");
      String data = "Select ID from accounts where EMAIL = ?"; 
      int pId = 0 ;
      
      try(PreparedStatement stmt = conn.prepareStatement(data)){
    	  stmt.setString(1, passengerEmail);

          ResultSet resultSet = stmt.executeQuery();

          if (resultSet.next()) {
              pId = resultSet.getInt("ID");
              
          }
      }catch (SQLException e) {
          e.printStackTrace();
	  }
      
      String Qry = "Select ADDRESS_ID from accounts where EMAIL = ?"; 
      int aId = 0 ;
      try(PreparedStatement stmt = conn.prepareStatement(Qry)){
    	  stmt.setString(1, passengerEmail);

          ResultSet resultSet = stmt.executeQuery();

          if (resultSet.next()) {
              aId = resultSet.getInt("ADDRESS_ID");
          }
          insertRideRequest(conn,pId,aId,date,time,Number_of_riders,ID);
      }catch (SQLException e) {
          e.printStackTrace();
	  }
      
     
  }
  
  private static void addFavoriteLocation(Connection connection, int passengerId, int destinationId, String locationName) throws SQLException {
		try{
	    
	        String sql = "INSERT INTO favourite_locations (PASSENGER_ID, LOCATION_ID, NAME) VALUES (?, ?, ?)";

	        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
	            preparedStatement.setInt(1, passengerId);
	            preparedStatement.setInt(2, destinationId);
	            preparedStatement.setString(3, locationName);

	            int rowsInserted = preparedStatement.executeUpdate();
	           
	            if (rowsInserted > 0) {
	                System.out.println("Favorite location added successfully. \n");
	            } else {
	                System.out.println("Failed to add favorite location. \n");
	            }
	        } 
		}
		catch (SQLException e) {
	        e.printStackTrace(); 
	    }
	}
  
  public static void insertRideRequest(Connection conn, int pId, int aId,
          String date, String time, int Number_of_riders, int ID) throws SQLException {
	  // Define the SQL query to insert a new ride request
	  String query = "INSERT INTO ride_requests (PASSENGER_ID, PICKUP_LOCATION_ID, PICKUP_DATE, PICKUP_TIME, NUMBER_OF_RIDERS,DROPOFF_LOCATION_ID) VALUES(?,?,?,?,?,?)";
	  try (PreparedStatement statement = conn.prepareStatement(query)){
		  statement.setInt(1, pId); 
		  statement.setInt(2, aId);
          statement.setString(3, date);
          statement.setString(4, time);
          statement.setInt(5,Number_of_riders);
          statement.setInt(6,ID);
            
          int rowsAffected = statement.executeUpdate(); 
          
          if (rowsAffected > 0) {
        	  System.out.println("\n----------------------------");
              System.out.println(" Ride Booked successfully!");
              System.out.println("-----------------------------");
          } else {
              System.out.println("Failed to insert data.");
          }
          
  } catch (SQLException e) {
      e.printStackTrace();
  }
  }
  
  public static void Addlocation(Connection conn,String Email) throws SQLException {
	  System.out.println("Enter the complete address of destination Sir");
	  String street = getUserInput("Enter Street Address: ");
      String city = getUserInput("Enter City: ");
      String province = getUserInput("Enter Province: ");
      String postalCode = getUserInput("Enter Postal Code: ");
      int addressID = 0 ;
      String query = "Select ID, ADDRESS_ID from accounts where EMAIL = ?"; 
      int pId = 0 ;
      int pickId = 0 ;
      try(PreparedStatement stmt = conn.prepareStatement(query)){
    	  stmt.setString(1, Email);

          ResultSet resultSet = stmt.executeQuery();

          if (resultSet.next()) {
              pId = resultSet.getInt("ID");
              pickId = resultSet.getInt("ADDRESS_ID");
          }
      }catch (SQLException e) {
          e.printStackTrace();
	  }
      
      String insertAddressSql = "INSERT INTO addresses (STREET, CITY, PROVINCE, POSTAL_CODE) VALUES (?, ?, ?, ?)";
      
      try (PreparedStatement preparedStatement = conn.prepareStatement(insertAddressSql, Statement.RETURN_GENERATED_KEYS)) {
          preparedStatement.setString(1, street);
          preparedStatement.setString(2, city);
          preparedStatement.setString(3, province);
          preparedStatement.setString(4, postalCode);

          preparedStatement.executeUpdate();

          // Retrieve the generated address ID
          try (ResultSet Keys = preparedStatement.getGeneratedKeys()) {
              if (Keys.next()) {
            	   addressID = Keys.getInt(1);
                  // Return the generated address ID
              } else {
                  throw new SQLException("Creating address failed. No ID obtained. \n");
              }
          }
          
          
  } catch (SQLException e) {
      e.printStackTrace();
      
  }
      String makesFav = getUserInput("Make this Location your favourite (y/n):");
      if ("y".equals(makesFav)){

          String locationName = getUserInput("Enter name of your Location: ");

           addFavoriteLocation(conn, pId, addressID,locationName);
      }
      
      String date = getUserInput("Enter Date of pickup (August 8, 2022- format): ");
      String time = getUserInput("Enter Time of pickup: ");
      int Number_of_riders = getIntInput("Enter Number of Riders: ");
      
      insertRideRequest(conn,pId,pickId ,date,time,Number_of_riders,addressID);
      

  }
    public static void submitRideRequest(){
    	String URL = "jdbc:sqlite:src/yoober_project.db";
    	Scanner sc  =  new Scanner(System.in);
        try (Connection connection = DriverManager.getConnection(URL)){
            String passengerEmail = getUserInput("Enter passenger email: ");
            
            String locationOfUser = getUserInput("You want to choose from your favourite destination? (y/n): ");
            
            int LOCATION_ID;
            if ("y".equals(locationOfUser)) {
                displayFavouriteLocations(passengerEmail);

                System.out.print("enter the ID of your location:");
                LOCATION_ID = sc.nextInt();
                BookRide(connection,LOCATION_ID,passengerEmail);
            }else if ("n".equals(locationOfUser)){
                Addlocation(connection,passengerEmail);
                
            }else {
                System.out.println("Invalid choice. Please choose 'Yes' or 'No'.");
                return;
            } 
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void displayFavouriteLocations(String passengerEmail){
    	String URL = "jdbc:sqlite:src/yoober_project.db";
        try (Connection connection = DriverManager.getConnection (URL)){
            String query = "SELECT ID, NAME, LOCATION_ID FROM favourite_locations WHERE PASSENGER_ID = (SELECT ID FROM accounts WHERE EMAIL = ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)){
                statement.setString(1, passengerEmail);
                try (ResultSet resultSet = statement.executeQuery()){
                    while ((resultSet.next())) {
                        int id = resultSet.getInt("ID");
                        String name = resultSet.getString("NAME");
                        int locationID = resultSet.getInt("LOCATION_ID");

                        String location  = getLocationByID(connection,locationID);
                        System.out.println("ID: " + id + ",\n Name: " + name + ",\n Location: " + location);
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

    }
   
    public static String getLocationByID(Connection conn,int locationID) {
    	
            String query = "SELECT STREET, CITY, PROVINCE, POSTAL_CODE FROM addresses WHERE ID = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setInt(1, locationID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String street = resultSet.getString("STREET");
                        String city = resultSet.getString("CITY");
                        String province = resultSet.getString("PROVINCE");
                        String postalCode = resultSet.getString("POSTAL_CODE");
    
                        return street + ", " + city + ", " + province + ", " + postalCode;
                    }
                }
            }
         catch (SQLException e) {
            e.printStackTrace();
        }
        return "Location not found";
    }
   
    // part 6
	
   
    public static void completeRide() throws SQLiteException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Complete a ride:\n");
        String URL = "jdbc:sqlite:src/yoober_project.db";

        try (Connection conn = DriverManager.getConnection(URL)) {
            // Display a list of rides that need completion
            displayUncompletedRides(conn);

            // Prompt the user to enter the ride ID they want to complete
            System.out.print("Enter the ID of the ride you want to complete: ");
            int rideId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            // Retrieve details of the selected ride
            RideDetails rideDetails = getRideDetails(conn, rideId);

            if (rideDetails != null) {
                // Prompt the user for additional details to complete the ride
                System.out.print("Enter the email address of the driver: ");
                String driverEmail = scanner.nextLine();

                System.out.print("Enter the date when the ride was completed (like - November 8, 2022): ");
                String completionDate = scanner.nextLine();

                System.out.print("Enter the time when the ride was completed (like 12:9 AM): ");
                String completionTime = scanner.nextLine();

                System.out.print("Enter the distance traveled during the ride: ");
                double distanceTraveled = scanner.nextDouble();
                scanner.nextLine(); // Consume the newline character

                System.out.print("Enter the total cost of the ride: ");
                double cost = scanner.nextDouble();
                scanner.nextLine(); // Consume the newline character

                System.out.print("Rate the driver's performance (1-5, 5 being the best): ");
                int driverRating = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                System.out.print("Rate the passenger's behavior (1-5, 5 being the best): ");
                int passengerRating = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character
                System.out.println();

                // Retry mechanism for updating the database with the completed ride details
                boolean updateSuccess = false;
                int maxRetries = 3;
                int retryCount = 0;

                while (!updateSuccess && retryCount < maxRetries) {
                    // Update the database with the completed ride details
					updateRide(conn, rideId, driverEmail, completionDate, completionTime, distanceTraveled, cost, driverRating, passengerRating);
					updateSuccess = true;
                }

                if (updateSuccess) {
                    System.out.println("Ride completion details successfully recorded.\n");
                } else {
                    System.err.println("Failed to update ride after multiple attempts");
                }

            } else {
                System.out.println("Invalid ride ID. Please enter a valid ID.\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    

    public static RideDetails getRideDetails(Connection conn, int ride_Id) throws SQLException {
        String sql = "SELECT rides.ID, accounts.FIRST_NAME AS PASSENGER_FIRST_NAME, " +
                "accounts.LAST_NAME AS PASSENGER_LAST_NAME, addresses_pickup.STREET AS PICKUP_STREET, " +
                "addresses_pickup.CITY AS PICKUP_CITY, addresses_dropoff.STREET AS DROPOFF_STREET, " +
                "addresses_dropoff.CITY AS DROPOFF_CITY, ride_requests.PICKUP_DATE, ride_requests.PICKUP_TIME " +
                "FROM rides " +
                "JOIN ride_requests ON rides.REQUEST_ID = ride_requests.ID " +
                "JOIN passengers ON ride_requests.PASSENGER_ID = passengers.ID " +
                "JOIN accounts ON passengers.ID = accounts.ID " +
                "JOIN addresses AS addresses_pickup ON ride_requests.PICKUP_LOCATION_ID = addresses_pickup.ID " +
                "JOIN addresses AS addresses_dropoff ON ride_requests.DROPOFF_LOCATION_ID = addresses_dropoff.ID " +
                "WHERE rides.ID = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, ride_Id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    RideDetails rideDetails = new RideDetails();
                    rideDetails.setRideId(resultSet.getInt("ID"));
                    rideDetails.setPassengerName(resultSet.getString("PASSENGER_FIRST_NAME") + " " +
                            resultSet.getString("PASSENGER_LAST_NAME"));
                    rideDetails.setPickupAddress(resultSet.getString("PICKUP_STREET") + ", " +
                            resultSet.getString("PICKUP_CITY"));
                    rideDetails.setDropoffAddress(resultSet.getString("DROPOFF_STREET") + ", " +
                            resultSet.getString("DROPOFF_CITY"));
                    rideDetails.setPickupDate(resultSet.getString("PICKUP_DATE"));
                    rideDetails.setPickupTime(resultSet.getString("PICKUP_TIME"));
                    return rideDetails;
                } else {
                    return null;
                }
            }
        }
    	catch (SQLException e) {
            e.printStackTrace(); 
        }
    	return null;
    }
public static void displayUncompletedRides(Connection conn) throws SQLException {
    String sql = "SELECT ride_requests.ID, accounts.FIRST_NAME AS PASSENGER_FIRST_NAME, " +
            "accounts.LAST_NAME AS PASSENGER_LAST_NAME, addresses_pickup.STREET AS PICKUP_STREET, " +
            "addresses_pickup.CITY AS PICKUP_CITY, addresses_dropoff.STREET AS DROPOFF_STREET, " +
            "addresses_dropoff.CITY AS DROPOFF_CITY, ride_requests.PICKUP_DATE, ride_requests.PICKUP_TIME, ride_requests.NUMBER_OF_RIDERS " +
            "FROM rides " +
            "JOIN ride_requests ON rides.REQUEST_ID = ride_requests.ID " +
            "JOIN passengers ON ride_requests.PASSENGER_ID = passengers.ID " +
            "JOIN accounts ON passengers.ID = accounts.ID " +
            "JOIN addresses AS addresses_pickup ON ride_requests.PICKUP_LOCATION_ID = addresses_pickup.ID " +
            "JOIN addresses AS addresses_dropoff ON ride_requests.DROPOFF_LOCATION_ID = addresses_dropoff.ID " +
            "WHERE rides.ACTUAL_START_DATE IS NOT NULL ";


    try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            System.out.println("Uncompleted Rides: \n");
            System.out.printf("%-4s%-20s%-30s%-35s%-20s%-15s%-20s\n", 
                    "ID", "Passenger", "Pickup Address", "Drop-off Address", "Pickup Date", "Pickup Time", "Number of Riders");
            
            while (resultSet.next()) {
                System.out.printf("%-4d%-20s%-30s%-35s%-20s%-15s%-20s\n",
                        resultSet.getInt("ID"),
                        resultSet.getString("PASSENGER_FIRST_NAME") + " " + resultSet.getString("PASSENGER_LAST_NAME"),
                        resultSet.getString("PICKUP_STREET") + ", " + resultSet.getString("PICKUP_CITY"),
                        resultSet.getString("DROPOFF_STREET") + ", " + resultSet.getString("DROPOFF_CITY"),
                        resultSet.getString("PICKUP_DATE"),
                        resultSet.getString("PICKUP_TIME"),
                        resultSet.getString("NUMBER_OF_RIDERS"));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
	
}

public static void updateRide(Connection conn, int ride_Id, String driverEmail, String endDate, String endTime,
        double distanceTraveled, double cost, int driverRating, int passengerRating) {
String sql = "UPDATE rides " +
"SET ACTUAL_START_DATE = CURRENT_DATE, ACTUAL_START_TIME = CURRENT_TIME, " +
"ACTUAL_END_DATE = ?, ACTUAL_END_TIME = ?, DISTANCE = ?, CHARGE = ?, " +
"RATING_FROM_DRIVER = ?, RATING_FROM_PASSENGER = ? " +
"WHERE ID = ?";

try {
	conn.setAutoCommit(false);
	
	try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
		preparedStatement.setString(1, endDate);
		preparedStatement.setString(2, endTime);
		preparedStatement.setDouble(3, distanceTraveled);
		preparedStatement.setDouble(4, cost);
		preparedStatement.setInt(5, driverRating);
		preparedStatement.setInt(6, passengerRating);
		preparedStatement.setInt(7, ride_Id);
		
		int rowsUpdated = preparedStatement.executeUpdate();
		
		if (rowsUpdated > 0) {
			System.out.println("Details of Ride updated successfully!!\n");
			conn.commit();
			} else {
			System.out.println("Failed to update ride details.\n");
			}
	}
} catch (SQLException e) {
			try {
				if (conn != null) {
					conn.rollback();
					}
				}catch (SQLException rollbackException) {
					rollbackException.printStackTrace();
					}
				e.printStackTrace();
			}
 finally {
			try {
				if (conn != null) {
				conn.setAutoCommit(true); // Reset auto-commit to true
				}
			} catch (SQLException setAutoCommitException) {
				setAutoCommitException.printStackTrace();
				}
		    }
}

}

// class will hold ride details 
class RideDetails  {
    private int rideId;
    private String passengerName;
    private String pickupAddress;
    private String dropoffAddress;
    private String pickupDate;
    private String pickupTime;

    public int getRideId() {
        return rideId;
    }

    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

	public String getPassengerName() {
		return passengerName;
	}

	public void setPassengerName(String passengerName) {
		this.passengerName = passengerName;
	}

	public String getPickupAddress() {
		return pickupAddress;
	}

	public void setPickupAddress(String pickupAddress) {
		this.pickupAddress = pickupAddress;
	}

	public String getDropoffAddress() {
		return dropoffAddress;
	}

	public void setDropoffAddress(String dropoffAddress) {
		this.dropoffAddress = dropoffAddress;
	}

	public String getPickupDate() {
		return pickupDate;
	}

	public void setPickupDate(String pickupDate) {
		this.pickupDate = pickupDate;
	}

	public String getPickupTime() {
		return pickupTime;
	}

	public void setPickupTime(String pickupTime) {
		this.pickupTime = pickupTime;
	}

}
