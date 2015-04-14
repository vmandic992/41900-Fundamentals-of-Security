import java.util.LinkedList;
import java.util.Random;

public class DES 
{
	private String[][] sBox = new String[2][16]; //Using a 4-bit S-Box
	private int numberOfRounds = 16;
	private String key;
	private LinkedList<String> subKeys;
	
	public enum processingType
	{
		ENCRYPT, DECRYPT
	}
	
	public enum shiftDirection
	{
		LEFT, RIGHT
	}
	
	public DES()
	{
		//constructor will receive the key
		constructSBox();
	}
	
	private void printKeys()
	{
		String s = "Primary Key: " + key + "\n" + "\n";
		s += "Subkeys:" + "\n";
		int i = 1;
		for (String subKey: subKeys)
		{
			s += "Subkey " + formatSpaces(i) + ": " + subKey + "\n";
			i++;
		}
		System.out.println(s);
	}
	
	private String formatSpaces(int i)
	{
		if (i < 10)
			return "" + i + "  ";
		else
			return "" + i + " ";
	}
	
	private void printSBox()
	{
		String s = "INPUT | OUTPUT" + "\n";
		s += "------|-------" + "\n";
		for (int i = 0; i < 16; i++)
			s += sBox[0][i] + "  | " + sBox[1][i] + "\n";
		System.out.println(s);
	}
	
	private String convertToBinary(String key)
	{
		String binaryKey = "";
		byte[] keyBytes = key.getBytes();
		for (byte b : keyBytes)
			binaryKey += Integer.toBinaryString(b);
		return binaryKey.substring(0, 56);
	}
	
	private LinkedList<String> generateSubKeys(String key)
	{
		LinkedList<String> generatedKeys = new LinkedList<String>();
		String shiftedKey = key;
		for (int i = 0; i < numberOfRounds; i++)
		{
			shiftedKey = shift(shiftedKey, 1, shiftDirection.LEFT);
			generatedKeys.add(shiftedKey);
		}
		return generatedKeys;
	}
	
	private String shift(String data, int shift, shiftDirection direction)
	{
		String bitsToMove = "";
		String shiftedString = "";
		
		switch (direction)
		{
			case LEFT:
			{
				bitsToMove = data.substring(0, shift);
				for (int i = shift; i < data.length(); i++)
					shiftedString += data.charAt(i);
				return shiftedString + bitsToMove;
			}
			case RIGHT:
			{
				bitsToMove = data.substring(data.length() - shift, data.length());
				for (int j = 0; j < data.length() - shift; j++)
					shiftedString += data.charAt(j);
				return bitsToMove + shiftedString;
			}
		}
		return null;
	}
	
	public String processData(String data, String key, processingType mode)
	{
		//Data needs to be converted into binary, segmented and padded before the following
		this.key = convertToBinary(key);
		subKeys = generateSubKeys(this.key);
		switch (mode)
		{
			case ENCRYPT: 
				return executeFeistel(data, subKeys);
			case DECRYPT:
				return executeFeistel(data, reverseOrderSubKeys());
		}
		return null;
	}
	
	
	private LinkedList<String> reverseOrderSubKeys()
	{
		LinkedList<String> subKeysReverse = new LinkedList<String>();
		for (int i = subKeys.size() - 1; i >= 0; i--)
			subKeysReverse.add(subKeys.get(i));
		return subKeysReverse;
	}
	
	private String executeFeistel(String data, LinkedList<String> subKeys)
	{
		//Before anything, first the code must segment data into blocks
		//Then, encrypt or decrypt each block by doing the below
		//Execute 16 rounds of encryption (decryption uses the same process but reverse key order)
		/*Each round:
		 * 1. Split input into 2 halves
		 * 2. Input right half into Feistel Function (F)
		 * 3. Input round key into Feistel Function
		 * 4. XOR left half with result of Feistel Function
		 * 5. Place XOR result into the next round as right half
		 * 6. Place original right half of current round into next round as left half
		 * 7. After the last round, swap the left and right halves
		 */
		return null;
	}
	
	private void constructSBox()
	{
		setInputSBoxValues();
		
		LinkedList<String> temp = new LinkedList<String>();
		for (int i = 0; i < 16; i++)
			temp.add(sBox[0][i]);
		
		setOutputSBoxValues(temp);
	}
	
	private void setInputSBoxValues()
	{
		for (int i = 0; i < 16; i++)
		{
			String inputValue = "" + Integer.toBinaryString(i);
			while (inputValue.length() < 4)
				inputValue = "0" + inputValue;		
			sBox[0][i] = inputValue;
		}
	}
	
	private void setOutputSBoxValues(LinkedList<String> temp)
	{
		Random rnd = new Random();
		int randomIndex = 0;
		for (int i = 0; i < 16; i++)
		{
			randomIndex = rnd.nextInt(16 - i);
			String outputValue = temp.get(randomIndex);
			temp.remove(randomIndex);
			sBox[1][i] = outputValue;
		}
	}
}
