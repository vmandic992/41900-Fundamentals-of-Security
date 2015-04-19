import java.util.LinkedList;

public class DES 
{
	private String[][] sBox = new String[2][16]; //Using a 4-bit S-Box
	private int numberOfRounds = 16;
	private int charactersPerBlock = 8;
	private String key;
	private LinkedList<String> subKeys;
	
	public static enum processingMode
	{
		ENCRYPT, DECRYPT
	}
	
	public static enum shiftDirection
	{
		LEFT, RIGHT
	}
	
	//Constructor should receive key
	public DES(String key)
	{
		constructSBox();
		printSBox();
		this.key = convertStringToBinary(key).substring(0, 56);				//Convert input key to binary, we only want the first 56 bits
		this.subKeys = generateSubKeys();									//Generate 16 x 56-bit sub-keys
		printKeys();
	}
	
	private void constructSBox()
	{
		setInputSBoxValues();
		setOutputSBoxValues();
	}
	
	private void setInputSBoxValues()
	{
		for (int i = 0; i < 16; i++)
		{
			String inputValue = "" + Integer.toBinaryString(i);	
			sBox[0][i] = pad(inputValue, 4);
		}
	}
	
	private String pad(String data, int totalBinaryDigits)
	{
		String paddedData = data;
		while (paddedData.length() < totalBinaryDigits)
			paddedData = "0" + paddedData;
		return paddedData;
	}
	
	private void setOutputSBoxValues()										//Output values (inputs are sBox[0][i])
	{
        sBox[1][0] =  "1111"; 
        sBox[1][1] =  "1010";
        sBox[1][2] =  "1110"; 
        sBox[1][3] =  "0001";
        sBox[1][4] =  "0111"; 
        sBox[1][5] =  "1001";
        sBox[1][6] =  "0000"; 
        sBox[1][7] =  "1101";
        sBox[1][8] =  "0010"; 
        sBox[1][9] =  "0011";
        sBox[1][10] = "0100"; 
        sBox[1][11] = "1000";
        sBox[1][12] = "0101"; 
        sBox[1][13] = "0110";
        sBox[1][14] = "1100"; 
        sBox[1][15] = "1011";
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
	
	//TO BE MODIFIED: So that subkey generation process is more complex
	private LinkedList<String> generateSubKeys()
	{	
		LinkedList<String> generatedKeys = new LinkedList<String>();
		String shiftedKey = key;
		for (int i = 0; i < numberOfRounds; i++)
		{
			shiftedKey = performShift(shiftedKey, 1, shiftDirection.LEFT);
			generatedKeys.add(shiftedKey);
		}
		return generatedKeys;
	}
	
	private String performShift(String data, int shift, shiftDirection direction)
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
		
	public String processData(String data, processingMode mode)
	{
		String paddedData = padWithSpaces(data);
		LinkedList<Integer> decimalData = convertToDecimal(paddedData);
		LinkedList<String> binaryData = convertToBinary(decimalData);
		LinkedList<String> blocks = segmentIntoBlocks(binaryData);
		
		switch (mode)
		{
			case ENCRYPT: 
				return processBlocks(blocks, subKeys);
			case DECRYPT:
				return processBlocks(blocks, reverseOrderSubKeys());				//Decryption uses keys in reverse order
		}
		return null;
	}
	
	private String padWithSpaces(String data)
	{
		String paddedData = data;
		while (paddedData.length() % charactersPerBlock != 0)
			paddedData += " ";
		return paddedData;
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
	
	private LinkedList<String> convertToBinary(LinkedList<Integer> decimalData)		//Returns list of each character in binary
	{
		LinkedList<String> binaryData = new LinkedList<String>();
		for (int i = 0; i < decimalData.size(); i++)
		{
			int decimal = decimalData.get(i);
			String binary = Integer.toBinaryString(decimal);
			binary = pad(binary, 8);
			binaryData.add(binary);
		}
		return binaryData;
	}
	
	private LinkedList<String> segmentIntoBlocks(LinkedList<String> binaryData)
	{
		LinkedList<String> blocks = new LinkedList<String>();
		for (int i = 0; i <= binaryData.size() - charactersPerBlock; i += charactersPerBlock)
		{
			String block = "";
			for (int j = i; j < (i + charactersPerBlock); j++)
				block += binaryData.get(j);
			blocks.add(block);
		}
		return blocks;
	}
	
	private String processBlocks(LinkedList<String> blocks, LinkedList<String> subKeys)			//Implement CBC at later stage
	{
		LinkedList<String> processedBlocks = new LinkedList<String>();
		for (String block: blocks)
		{
			String processedBlock = executeFeistel(block, subKeys);
			processedBlocks.add(processedBlock);
		}	
		return convertBinaryToText(processedBlocks);											//Final text output (plain or cipher)
	}
	
	private LinkedList<String> reverseOrderSubKeys()
	{
		LinkedList<String> subKeysReverse = new LinkedList<String>();
		for (int i = subKeys.size() - 1; i >= 0; i--)
			subKeysReverse.add(subKeys.get(i));
		return subKeysReverse;
	}
	
	//============================================================================================================== OPERATIONS:::
	
	private String executeFeistel(String block, LinkedList<String> subKeys)
	{		
		String entryLeft = block.substring(0, block.length() / 2);					//32-bit if block is 64-bit
		String entryRight =block.substring(block.length() / 2, block.length());		//32-bit if block is 64-bit
		for (int i = 0; i < numberOfRounds; i++)	
		{
			String originalRight = entryRight;
			String processedRight = feistelFunction(entryRight, subKeys.get(i)); //+ key
			entryRight = performXOR(entryLeft, processedRight);
			entryLeft = originalRight;
			System.out.println(entryLeft + entryRight);
		}
		return (entryRight + entryLeft);
	}
	
	private String feistelFunction(String blockRightHalf, String roundKey)	//receives 32-bit block half and 56-bit key
	{
		//String compressedKey = roundKey.substring(0, roundKey.length() / 2);
		String compressedKey = roundKey.substring(0, 32); //For now, statically code it to be 32, will do compression P-Box later (56-bit to 32-bit)
		
		String leftHalf = blockRightHalf.substring(0, blockRightHalf.length() / 2);
		String rightHalf = blockRightHalf.substring(blockRightHalf.length() / 2, blockRightHalf.length());
				
		String sBoxResult = performSubstitution(leftHalf) + performSubstitution(rightHalf);
		
		String swapResult = performSwap(sBoxResult);
		
		String invertResult = performInversion(swapResult);
		
		String xorResult = performXOR(invertResult, compressedKey);
		
		return xorResult;
	}
	
	private String convertBinaryToText(LinkedList<String> binaryData)
	{	//Method assumes block size of 16 bit (may change later to account for larger block sizes)	
		String data = "";
		for (String s: binaryData)
		{
			for (int i = 0; i <= ((charactersPerBlock * 8) - 8); i += 8)
			{
				String currentCharacter = s.substring(i, (i + 8));
				int characterDecimal = Integer.parseInt(currentCharacter, 2);
				data += (char)characterDecimal;
			}
		}
		return data;
	}
	
	private String performSubstitution(String data)
	{
		String sBoxResult = "";
		String fourBitGroup = "";
		for (int i = 0; i <= data.length() - 4; i += 4)
		{
			fourBitGroup = data.substring(i, (i + 4));
			for (int j = 0; j < 16; j++)
			{
				if (sBox[0][j].equals(fourBitGroup))
				{
					sBoxResult += sBox[1][j];
					break;
				}
			}
		}
		return sBoxResult;
	}
		
	private String performSwap(String data)
	{
		String leftHalf = data.substring(0, data.length() / 2);
		String rightHalf = data.substring(data.length() / 2, data.length());
		return (rightHalf + leftHalf);
	}
	
	//Will be used later to compress key from 56 bits to 32 bits using compression P-Box
	private String performCompression(String data)
	{
		return null;
	}
	
	private String performInversion(String data)
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
}