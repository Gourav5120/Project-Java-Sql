import java.util.*;
public class App {

    public static void main(String[] args) throws Exception {
    	while(true) {
    	 System.out.println("Welcome to our Project of Database:");
         System.out.println("Gourav welcomes you here.....");
         Scanner sc  =  new Scanner(System.in);
         System.out.println("Enter the correct options according to the given choices");
         
         System.out.println("\nWelcome to the Ride Sharing Application!");
         System.out.println("___________________________________________________________\n");
         System.out.println("Menu Options:");
         System.out.println("1. View all account details");
         System.out.println("2. Calculate the average rating for a specific driver");
         System.out.println("3. Calculate the total money spent by a specific passenger");
         System.out.println("4. Create a new account");
         System.out.println("5. Submit a ride request");
         System.out.println("6. Complete a ride");
         System.out.println("0. Exit");
         System.out.println("\n___________________________________________________________\n");

         System.out.print("\nEnter your choice: ");
         int choice = sc.nextInt();
         sc.nextLine(); // Consume newline character

         switch (choice) {
             case 1:
                // viewAllAccountDetails();
            	 Operation.viewAllAccountDetails();
                 break;
             case 2:
                 // calculateAverageRating();
            	 Operation.calculateAverageRating();
                 break;
             case 3:
                 //calculateTotalMoneySpent();
            	 Operation.calculateTotalMoneySpent();
                 break;
             case 4:
                 //createNewAccount();
            	 Operation.createNewAccount() ;
                 break;
             case 5:
                 //submitRideRequest();
            	 Operation.submitRideRequest() ;
                 break;
             case 6:
                 Operation.completeRide();
                 break;
             case 0:
                 System.out.println("Exiting the Ride Sharing Application. Goodbye!");
                 sc.close();
                 System.exit(0);
             default:
                 System.out.println("Invalid choice. Please enter a valid option.");
             }
         
         System.out.println("\nSelect other options as well:\n");
    	}
        }
    	
        
        
      
        
        
    }

