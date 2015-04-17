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
	
	private String convertStringToBinary(String s)
	{
		String binaryString = "";
		byte[] bytes = s.getBytes();
		for (byte b: bytes)
			binaryString += Integer.toBinaryString(b);
		return binaryString;
	}
	
	private LinkedList<String> generateSubKeys()						//Simple for now, but later might code it to be more complex
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
		LinkedList<Integer> decimalData = convertToDecimal(data);
		LinkedList<String> binaryData = convertToBinary(decimalData);
		LinkedList<String> blocks = segment(binaryData);
		
		this.key = convertStringToBinary(key).substring(0, 56);
		subKeys = generateSubKeys();
		switch (mode)
		{
			case ENCRYPT: 
				return executeFeistel(data, subKeys);
			case DECRYPT:
				return executeFeistel(data, reverseOrderSubKeys());
		}
		return null;
	}
	
	private LinkedList<Integer> convertToDecimal(String data)
	{
		LinkedList<Integer> decimals = new LinkedList<Integer>();
		for (int i = 0; i < data.length(); i++)
		{
			char c = data.charAt(i);
			int decimal = (int)c;
			decimals.add(decimal);
		}
		return decimals;
	}
	
	private LinkedList<String> convertToBinary(LinkedList<Integer> decimalData)	//returns list of each character in binary
	{
		LinkedList<String> binaryData = new LinkedList<String>();
		for (int i = 0; i < decimalData.size(); i++)
		{
			int decimal = decimalData.get(i);
			String binary = Integer.toBinaryString(decimal);
			//while (binary.length() < 8)
				//binary = "0" + binary;
			binary = pad(binary, 8);
			binaryData.add(binary);
		}
		return binaryData;
	}
	
	private LinkedList<String> segment(LinkedList<String> binaryData)
	{
		LinkedList<String> blocks = new LinkedList<String>();
		
		return blocks;
	}
	
	private String pad(String data, int totalBinaryDigits)
	{
		String paddedData = data;
		while (paddedData.length() < totalBinaryDigits)
			paddedData = "0" + paddedData;
		return paddedData;
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
		setOutputSBoxValues();
		/*
		LinkedList<String> temp = new LinkedList<String>();
		for (int i = 0; i < 16; i++)
			temp.add(sBox[0][i]);*/
		
		//setOutputSBoxValues(temp);
	}
	
	private void setInputSBoxValues()
	{
		for (int i = 0; i < 16; i++)
		{
			String inputValue = "" + Integer.toBinaryString(i);
			//while (inputValue.length() < 4)
				//inputValue = "0" + inputValue;		
			sBox[0][i] = pad(inputValue, 4);
		}
	}
	
	private void setOutputSBoxValues()
	{
		sBox[1][0] =  "1111"; sBox[1][1] =  "1010";
		sBox[1][2] =  "1110"; sBox[1][3] =  "0001";
		sBox[1][4] =  "0111"; sBox[1][5] =  "1001";
		sBox[1][6] =  "0000"; sBox[1][7] =  "1101";
		sBox[1][8] =  "0010"; sBox[1][9] =  "0011";
		sBox[1][10] = "0100"; sBox[1][11] = "1000";
		sBox[1][12] = "0101"; sBox[1][13] = "0110";
		sBox[1][14] = "1100"; sBox[1][15] = "1011";
	}
	
	/*
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
	}*/
	
	private String performXOR(String data1, String data2)
	{
		String result = "";
		for (int i = 0; i < data1.length(); i++)
		{
			if (data1.charAt(i) == data2.charAt(i))
					result += "0";
			else
				result += "1";
		}
		return result;
	}
	
	private String performBitInversion(String data)
	{
		String result = "";
		for (int i = 0; i < data.length(); i++)
		{
			if (data.charAt(i) == '0')
				result += "1";
			else
				result += "0";
		}
		return result;
	}
}
