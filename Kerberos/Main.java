
public class Main 
{
	public static void main(String[] args) 
	{
		//new KerberosSystem();
		
		
		//DES TEST
		DES des1 = new DES("ABCDEFGHIJK");
		
		String plaintext = "Hello world this is a test";
		String encrypted = des1.processData(plaintext, DES.processingMode.ENCRYPT);
		System.out.println("Plantext:   " + plaintext + "\n");
		System.out.println("CipherText: " + encrypted + "\n");
		
		//A test which would fail
		String decrypted = new DES("ACD").processData(encrypted, DES.processingMode.DECRYPT);
		System.out.println("Failed decryption: " + decrypted + "\n");
		
		String decrypted2 = des1.processData(encrypted, DES.processingMode.DECRYPT);
		System.out.println("Correct decryption: " + decrypted2);
	}
}
