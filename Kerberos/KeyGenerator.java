import java.util.Random;

public class KeyGenerator 
{
	private String key = "";
	//private Random r;
	
	public KeyGenerator(int length)
	{
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
		//r = new Random();
		
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
