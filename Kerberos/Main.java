import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class Main 
{
	public static void main(String[] args) throws IOException, ParseException
	{
		new KerberosSystem();	
		
		/*
		String key = "ABCDEFGHIJKLMNOPQRSTU";
		String iv = "12345678";
		
		String message = "41900 is an awesome subject!";
		
		long start = System.currentTimeMillis();
		String cipherText = new TripleDES(key, iv, null).processData(message, DES.blockCipherMode.CBC, DES.processingMode.ENCRYPT);
		long timeTaken = System.currentTimeMillis() - start;
		
		String plainText = new TripleDES(key, iv, null).processData(cipherText, DES.blockCipherMode.CBC, DES.processingMode.DECRYPT);
		
		System.out.println("Message:        " + message);
		System.out.println("Cipher:         " + cipherText);
		System.out.println("Plaintext:      " + plainText);
		System.out.println("Execution Time: " + timeTaken);*/
	}
}