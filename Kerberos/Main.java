import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Main 
{
	public static void main(String[] args) throws IOException, ParseException
	{
		new KerberosSystem();
		
		/*
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 0);
		String date1 = dateFormat.format(cal.getTime());
		System.out.println("Valid until: " + date1);
		
		
		String dateInMsg = date1;
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date expiryDate = format.parse(dateInMsg);
			
		
		Date currentDate = Calendar.getInstance().getTime();
		System.out.println("Time now: " + currentDate);
		System.out.println(currentDate.before(expiryDate));*/
		
		/*
		String capturePath = "TripleDESCapture.txt";
		String capturePath2 = "TripleDESCapture2.txt";
		
		String key = "MyAwesomeTripleDESKey";
		String IV = "12345";
		TripleDES senderTripleDES = new TripleDES(key, IV, capturePath);
		TripleDES receiverTripleDES = new TripleDES(key, IV, capturePath2);
		
		String message = "Hello Vedran This is a test";
		String cipher = senderTripleDES.processData(message, DES.blockCipherMode.CBC, DES.processingMode.ENCRYPT);
		String plaintext = receiverTripleDES.processData(cipher, DES.blockCipherMode.CBC, DES.processingMode.DECRYPT);
		
		System.out.println("Message: " + message);
		System.out.println("CipherText: " + cipher);
		System.out.println("Plaintext: " + plaintext.trim());*/
	}
}
