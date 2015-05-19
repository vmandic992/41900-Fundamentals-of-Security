import java.util.Random;

public class KeyGenerator 
{
	private String key = "";
	public static char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
	//private Random r;
	
	public KeyGenerator(int length)
	{
		//char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
		//r = new Random();
		
		/*	- While the length of the key is less than the desired length
		 *  - Add a new random character from the char[] array above
		 *  - Make sure the random index is less than the length of the chars[] array
		 */
		while(key.length() < length)
		{
			char c = chars[new Random().nextInt(chars.length)];
			key += c;
		}
	}
	
	public String getKey()
	{
		return this.key;
	}

}
