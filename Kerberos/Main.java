import java.io.IOException;


public class Main 
{
	public static void main(String[] args) throws IOException
	{
		new KerberosSystem();
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
