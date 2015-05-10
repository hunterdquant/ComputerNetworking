/**
 * Created by blad on 1/21/15.
 */
public class CommandLineAdder {
    public static void main(String[] args) {
       
	   	//Try to parse the string to an integer and get the sum.
		try {
			
			int sum = 0;
			System.out.println(String.format("There are %d arguments", args.length));
        	
			for (int i = 0; i < args.length; i++) {
            	
				sum += Integer.parseInt(args[i]);
				System.out.println(String.format("  %s", args[i]));
        	}

			System.out.println("The sum of the arguments is: " + sum);
		} catch(NumberFormatException e) { //Catch an exception if the user entered invalid input.
			
			System.out.println("Argument cannot be interpreted as an integer." + e);
		}
    }
}
