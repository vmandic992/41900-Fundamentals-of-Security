import java.io.IOException;
import java.text.ParseException;

public class Main 
{
	public static void main(String[] args) throws IOException, ParseException
	{
		new KerberosSystem();	
		
		//QUICK DES TEST CODE (to use, comment 'new KerberosSystem()' above, and uncomment all the following code)
		/*
		String key = "123DEFZHIJKLZNOPQRSTU";
		String iv = "12345BB8";
		
		String message = "Hello world this is a test";
		
		String cipherText = new TripleDES(key, iv, null).processData(message, DES.blockCipherMode.CBC, DES.processingMode.ENCRYPT);
		
		String plainText = new TripleDES(key, iv, null).processData(cipherText, DES.blockCipherMode.CBC, DES.processingMode.DECRYPT);
		
		System.out.println("Message:        " + message);
		System.out.println("Cipher:         " + cipherText);
		System.out.println("Plaintext:      " + plainText);*/
	}
}