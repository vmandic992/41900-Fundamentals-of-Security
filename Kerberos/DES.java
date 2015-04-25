import java.util.LinkedList;

public class DES 
{
	private String[][] sBox = new String[2][16]; 	//S-Box (4 bit)
	private int[] pBox = {1, 2, 3, 3, 0, 2, 1}; 	//P-Box (compression type - compresses 7 bits to 4 bits)
	
													/* P-Box array meaning:
													 * 	Index:	0	1	2	3	4	5	6
													 * 			1	2	3	1	0	2	3
													 * 		
													 *  Bit 0 goes to bit 1
													 *  Bit 1 goes to bit 2
													 *  Bit 2 goes to bit 3
													 *  Bit 3 goes to bit 1
													 *  Bit 4 goes to bit 0
													 *  Bit 5 goes to bit 2
													 *  Bit 6 goes to bit 3
													 */

	private int numberOfRounds = 16;				//Each block is encrypted in 16 rounds
	private int charactersPerBlock = 8;				//Each block is 8 characters in size (each character is 8 bits)
	private String key;								//This is the main key passed into the algorithm (56-bit DES key)
	private LinkedList<String> subKeys;				//Stores the 16 subkeys generated from the inpuy 56-bit key ('private String key')
	
	
	
	public static enum processingMode				//Do we want DES to encrypt or decrypt?
	{
		ENCRYPT, DECRYPT
	}
	
	
	
	public static enum shiftDirection				//Direction we want for shifting bits where necessary
	{
		LEFT, RIGHT
	}
	
	
	
	public static enum blockCipherMode				//Do we want to encrypt using ECB or CBC?
	{												//CBC prevents identical plaintext blocks encrypting to identical ciphertext blocks
		ECB, CBC
	}
	
	
	
	
	public DES(String key)										//CONSTRUCTOR: receives the 56-bit DES key
	{
		constructSBox();										//First construct the 4-bit S-Box
		
		this.key = pad(convertStringToBinary(key), 56);			//Convert input key to binary, and pad it so it's 56-bits in size
		
		this.subKeys = generateSubKeys();						//Generate 16 x 56-bit sub-keys
		
		//printKeys();
		//printSBox();
	}
	
	
	
	private void constructSBox()
	{
		setInputSBoxValues();									//Create the input values (4-bit means 16 combinations)
		setOutputSBoxValues();									//Set the output values (4-bit means 16 combinations)
	}
	
	
	
	private void setInputSBoxValues()
	{
		for (int i = 0; i < 16; i++)								//For values 0 to 15 (all possible values of 4-bits)
		{
			String inputValue = "" + Integer.toBinaryString(i);		//Convert the number to binary	
			sBox[0][i] = pad(inputValue, 4);						//Then pad the number so it's 4 bits long
																	//E.g. '2' becomes '10' in binary, which is padded to be '0010'
		}
	}
	
	
	
	private String pad(String data, int totalBinaryDigits)			//Takes a binary number and pads it with '0's
	{																//'totalBinaryDigits' is how long we want our input number to be
		String paddedData = data;
		while (paddedData.length() < totalBinaryDigits)				//E.g. arguments ('10',8) outputs  '00000010'
			paddedData = "0" + paddedData;							//E.g. arguments ('110',4) outputs '0110'
		return paddedData;
	}
	
	
	
	private void setOutputSBoxValues()								//Statically-set S-Box output values
	{																//Input values "0000" to "1111" were set in 'setInputSBoxValues()'
		
        sBox[1][0] =  "1111"; 										//sBox[0][0] = "0000", 		 "0000" substitutes to "1111"
        sBox[1][1] =  "1010";										//sBox[0][0] = "0001", 		 "0001" substitutes to "1010"
        sBox[1][2] =  "1110"; 										//sBox[0][0] = "0010", 		 "0010" substitutes to "1110"
        sBox[1][3] =  "0001";										//sBox[0][0] = "0011", 		 "0011" substitutes to "0001"
        sBox[1][4] =  "0111"; 										//sBox[0][0] = "0100", 		 "0100" substitutes to "0111"
        sBox[1][5] =  "1001";										//sBox[0][0] = "0101", 		 "0101" substitutes to "1001"
        sBox[1][6] =  "0000"; 										//sBox[0][0] = "0110", 		 "0110" substitutes to "0000"
        sBox[1][7] =  "1101";										//sBox[0][0] = "0111", 		 "0111" substitutes to "1101"
        sBox[1][8] =  "0010"; 										//sBox[0][0] = "1000", 		 "1000" substitutes to "0010"
        sBox[1][9] =  "0011";										//sBox[0][0] = "1001", 		 "1001" substitutes to "0011"
        sBox[1][10] = "0100"; 										//sBox[0][0] = "1010", 		 "1010" substitutes to "0100"
        sBox[1][11] = "1000";										//sBox[0][0] = "1011", 		 "1011" substitutes to "1000"
        sBox[1][12] = "0101"; 										//sBox[0][0] = "1100", 		 "1100" substitutes to "0101"
        sBox[1][13] = "0110";										//sBox[0][0] = "1101", 		 "1101" substitutes to "0110"
        sBox[1][14] = "1100"; 										//sBox[0][0] = "1110", 		 "1110" substitutes to "1100"
        sBox[1][15] = "1011";										//sBox[0][0] = "1111", 		 "1111" substitutes to "1011"
	}
	
	
	
	private void printSBox()										//Prints the S-Box in pairs (input and output)
	{
		String s = "INPUT | OUTPUT" + "\n";
		s += "------|-------" + "\n";
		for (int i = 0; i < 16; i++)
			s += sBox[0][i] + "  | " + sBox[1][i] + "\n";
		System.out.println(s);
	}
	
	
	
	private String convertStringToBinary(String s)					//Takes a string and converts it into a binary string
	{
		String binaryString = "";									
		byte[] bytes = s.getBytes();								//String is converted to an array of bytes using Java method 'getBytes()'
		for (byte b: bytes)											//For each byte, convert it to a binary string
			binaryString += Integer.toBinaryString(b);				//Add the byte's binary string to 'binaryString'
		return binaryString;										//Return the final concatenated binary string
		
																	//E.g. string "te" becomes byte_array[116, 101]
																	//116 becomes 01110100
																	//101 becomes 01100101
																	//etc... so return 0111010001100101
	}																
																		
	
	
	
	//TO BE MODIFIED
	private LinkedList<String> generateSubKeys()							//Generates subkeys from the main 56-bit key
	{	
		LinkedList<String> generatedKeys = new LinkedList<String>();
		String shiftedKey = key;											//Get the main 56-bit key
		for (int i = 0; i < numberOfRounds; i++)							//Execute 16 times (we need 1 key per round (16))
		{
			shiftedKey = performShift(shiftedKey, 1, shiftDirection.LEFT);	//Get 56-bit key, shift it left by 1
			shiftedKey = performInversion(shiftedKey);						//Then invert the digits (i.e. 0 > 1 and 1 > 0)
			generatedKeys.add(shiftedKey);									//Add this new 56-bit key to the list of subkeys
																			//Then use THIS key to create the next one, and so on...
																			
																			//E.g. Input DES key is 'A'
																			// 'A' shifts and inverts to become 'B' (1st subkey)
																			// 'B' shifts and inverts to become 'C' (2nd subkey)
																			// and so on...
		}
		return generatedKeys;
	}
	
	
	
	
	private String performShift(String data, int shift, shiftDirection direction) //Takes a binary string and shifts it by a value
	{																			  //and in a specified direction
		String bitsToMove = "";
		String shiftedString = "";
		
		switch (direction)
		{																		  	//E.g. shit 1011000 left by 2
			case LEFT:															  	//Get first 2 bits from left '10'
			{																	  	//Move them to the end: 11000|10
				bitsToMove = data.substring(0, shift);							  	//Return 1100010
				for (int i = shift; i < data.length(); i++)
					shiftedString += data.charAt(i);
				return shiftedString + bitsToMove;
			}
			case RIGHT:															  	//E.g. shift 1011000 right by 2
			{																	  	//Get first 2 bits from right '00'
				bitsToMove = data.substring(data.length() - shift, data.length());	//Move them to the front: 00|10110
				for (int j = 0; j < data.length() - shift; j++)						//Return 0010110
					shiftedString += data.charAt(j);
				return bitsToMove + shiftedString;
			}
		}
		return null;
	}
	
	
	
	private void printKeys()														//Prints the input 56-bit
	{																				//and then the 16 x 56-bit subkeys
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
	
	
	
			
	private String formatSpaces(int i)												//Helps neaten output that's all...
	{
		if (i < 10)
			return "" + i + "  ";
		else
			return "" + i + " ";
	}
		
	
	
	
	//If another object wants a DES object to encrypt or decrypt something, it will call THIS method, passing the DES object the:
	// 1. Data to encrypt or decrypt
	// 2. Mode to use: ECB or CBC
	// 3. Intialization Vector (IV): If ECB is used, then null is passed
	// 4. Processing mode: Encrypt or Decrypt
	
	public String processData(String data, blockCipherMode cipherMode, String IV, processingMode mode)
	{
		//First, pad the data so its character count is a multiple of 8 (since each block is 8 characters (64-bits)
		//E.g. "Hello World" becomes "Hello World     " (5 spaces)
		String paddedData = padWithSpaces(data);
		
		//Then convert the data into a list of decimal digits
		LinkedList<Integer> decimalData = convertToDecimal(paddedData);
		
		//Then convert each decimal in the list to its binary equivalent
		LinkedList<String> binaryData = convertToBinary(decimalData);
		
		//Then segment the binary data into blocks of 64-bits
		LinkedList<String> blocks = segmentIntoBlocks(binaryData);
		
		//Then either encrypt or decrypt
		//Note: - Decryption is the same process as encryption, but with reverse subkey order
		switch (mode)
		{
			case ENCRYPT: 
				return processBlocks(blocks, subKeys, cipherMode, IV, mode);
			case DECRYPT:
				return processBlocks(blocks, reverseOrderSubKeys(), cipherMode, IV, mode);
		}
		return null;
	}
	
	
	
	
	
	private String padWithSpaces(String data)					//Takes a string and pads it with spaces
	{
		String paddedData = data;
		while (paddedData.length() % charactersPerBlock != 0)	//If the character count isn't divisible by 8, add a space at the end
			paddedData += " ";									//Keep doing so until the count is a multiple of 8
		return paddedData;										
	}
	
	
	
	private LinkedList<Integer> convertToDecimal(String data)					//Converts a string to a list of decimals
	{
		LinkedList<Integer> decimals = new LinkedList<Integer>();
		for (int i = 0; i < data.length(); i++)
		{
			char c = data.charAt(i);											//E.g. 'A'
			int decimal = (int)c;												//Decimal = 65 (which is 'A's ASCII value)
			decimals.add(decimal);												//Add 65 to the list 'decimals'
		}
		return decimals;
	}
	
	
	
	private LinkedList<String> convertToBinary(LinkedList<Integer> decimalData)	//Converts list of decimals to list of binary strings
	{
		LinkedList<String> binaryData = new LinkedList<String>();
		for (int i = 0; i < decimalData.size(); i++)
		{
			int decimal = decimalData.get(i);									//E.g. '65' (from previous method 'convertToDecimal()')
			String binary = Integer.toBinaryString(decimal);					// '65' becomes 1000001					  ^
			binary = pad(binary, 8);											// add padding so it's 8 characters long: 01000001
			binaryData.add(binary);												//Add 01000001 to the list 'binaryData'
		}
		return binaryData;
	}
	
	
	
	//This method takes a list of binary strings (from method 'convertToBinary()') and segments it into 64-bit blocks
	/* It does this by gathering 8 elements at a time (i.e. 8 binary strings)
	 * It then concatenates these 8 elements into 1 binary string, and adds it to the 'blocks' list
	 * 
	 * 
	 * E.g. let's say we have "Hello Nice World" as our data: We begin with method 'processData()'
	 * 		1. padWithSpaces() returns "Hello Nice World" (no spaces, it's character count is a multiple of 8)
	 * 
	 * 											  H   e   l   l   o  __  N  i  c   e  __ W   o   r   l   d
	 * 		2. convertToDecimal() returns "List: [72 101 108 108 111 32 78 105 99 101 32 87 111 114 108 100]"
	 * 
	 * 												72	    101       108     108      101       _       78			... etc...
	 * 		3. convertToBinary() returns "List: [01001000 01100101 01101100 01101100 01101111 00100000 01001110 01101001 01100011 01100101 00100000 01010111 01101111 01110010 01101100 01100100 ]
	 * 		
	 * 																	H e l l o _ N i													c e _ W o r l d
	 * 		4. segmentIntoBlocks() returns "List: [0100100001100101011011000110110001101111001000000100111001101001, 0110001101100101001000000101011101101111011100100110110001100100]
	 */
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
	
	
	
	
	//This method takes the list of blocks (from method 'segmentIntoBlocks()') and encrypts/decrypts each one
	/* Method receives: 1. The list of blocks
	 * 					2. The list of subkeys
	 * 					3. Cipher mode: ECB or CBC
	 * 					4. IV (will be null if mode is ECB)
	 * 					5. Processing mode: Encrypt or Decrypt
	 */
	private String processBlocks(LinkedList<String> blocks, LinkedList<String> subKeys, blockCipherMode cipherMode, String IV, processingMode mode)
	{
		LinkedList<String> processedBlocks = new LinkedList<String>();
		
		switch(cipherMode)
		{
			case ECB: //Electronic Code Book									//If we're using ECB
			{
				for (String block: blocks)										//For each block
				{
					String processedBlock = executeFeistel(block, subKeys);		//Process the block using Feistel structure and subkeys
					processedBlocks.add(processedBlock);						//Add the resulting block to the list 'processedBlocks'
				}
				return convertBinaryToText(processedBlocks);					//Return the list of result blocks, converted back to text
			}
			
			case CBC: //Cipher Block Chaining 
			{
				String previousBlock = pad(convertStringToBinary(IV), 64); 		//If we're using CBC, set 'previousBlock' to IV
																				//The IV needs to be padded so it's 64 bits in size
				
				for (String block: blocks)										//For each block
				{
					switch (mode)												//If we're encrypting with CBC
					{
						case ENCRYPT:
						{
							String chain = performXOR(block, previousBlock);	//Get current block and XOR it with the previous block
							
							previousBlock = executeFeistel(chain, subKeys);		//Then encrypt the result of the XOR, then make the result
																				//be the 'previousBlock' for the NEXT block
							
							processedBlocks.add(previousBlock);					//Add the result to the list of 'processedBlocks'
							break;
						}
						case DECRYPT:												 //If we're decrypting with CBC
						{
							String decryptedBlock = executeFeistel(block, subKeys);	 //Decrypt the current block
							String chain = performXOR(decryptedBlock, previousBlock);//XOR it with the 'previousBlock' (initially equal to IV)
							processedBlocks.add(chain);								 //Add th XOR result to the list of 'processedBlocks'
							previousBlock = block;									 //Make the initial ciphertext block the 'previousBlock'
							break;															
						}
					}
				}
				return convertBinaryToText(processedBlocks);						//Then return the list converted back to text
			}
		}
		return null;
	}
	
	
	
	
	
	private LinkedList<String> reverseOrderSubKeys()					//Reverses the order of subkeys (for decryption only)
	{
		LinkedList<String> subKeysReverse = new LinkedList<String>();
		for (int i = subKeys.size() - 1; i >= 0; i--)
			subKeysReverse.add(subKeys.get(i));
		return subKeysReverse;
	}
	
	
	
	//============================================================================================================== OPERATIONS:::
	
	
	//Takes an input 64-bit block and list of 16 x 56-bit subkeys and performs the following:
	/*	1. Splits the block into 2 halves (left and right) each 32-bits
	 * 	2. For 16 rounds:
	 * 		(a) put the right 32-bits in the feistel function with the current round subkey (see 'feistelFunction()' method below)
	 * 		(b) XOR the result with the left 32-bits
	 * 		(c) next round uses the result as its right 32-bit half
	 * 		(d) next round uses the current round's original right 32-bit half as the left 32-bit half
	 * 3. After 16th round is over
	 * 		(a) swap the left and right 32-bit halves
	 */
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
			//System.out.println(entryLeft + entryRight);
		}
		return (entryRight + entryLeft);
	}
	
	
	//Takes a 32-bit half and modifies it using the 56-bit round subkey
	/*	1. Compress the 56-bit key into 32-bits using method 'compressKey()' (which uses compression P-Box)
	 * 	2. Split the 32-bit half into left and right halves (16-bits each)
	 *  3. Process each half using the S-Box
	 *  4. Swap the 2 halves and concatenate them into a 32-bit sting
	 *  5. Invert the bits of the new 32-bit string
	 *  6. XOR the 32-bit string with the compressed 32-bit key
	 */
	private String feistelFunction(String blockRightHalf, String roundKey)
	{
		//String compressedKey = roundKey.substring(0, 32);
		String compressedKey = compressKey(roundKey);
		String leftHalf = blockRightHalf.substring(0, blockRightHalf.length() / 2);
		String rightHalf = blockRightHalf.substring(blockRightHalf.length() / 2, blockRightHalf.length());
				
		String sBoxResult = performSubstitution(leftHalf) + performSubstitution(rightHalf);
		
		String swapResult = performSwap(sBoxResult);
		
		String invertResult = performInversion(swapResult);
		
		String xorResult = performXOR(invertResult, compressedKey);
		
		return xorResult;
	}
	
	
	//Takes a list of blocks (64-bits) and converts the entire list into text for final output
	/* 1. Take each 64-bit block
	 * 2. Convert each group of 8-bits in the block into decimal
	 * 3. Convert each decimal into a character
	 * 4. Add the character to the final output string 'data'
	 */
	private String convertBinaryToText(LinkedList<String> binaryData)
	{
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
	
	
	//Takes an input binary string and performs substitution (S-Box)
	/* 1. Take each group of 4-bits
	 * 2. Look up the 4-bit group in the S-Box
	 * 3. Output the corresponding S-Box output value
	 */
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
		
	
	//Takes an input binary string and split it into 2 equal-size halves
	//The 2 halves are swapped
	private String performSwap(String data)
	{
		String leftHalf = data.substring(0, data.length() / 2);
		String rightHalf = data.substring(data.length() / 2, data.length());
		return (rightHalf + leftHalf);
	}
	
	
	//Takes an input 56-bit round subkey and compresses it into 32-bits
	/*  1. Split the 56-bit key into 8 groups of 7-bits
	 *  2. Compress each group of 7-bits into 4-bits using compression P-Box
	 *  3. Concatenate all 8 x 4-bit groups to form a new 32-bit key
	 */
	public String compressKey(String data)
	{
		String compressedKey = "";
		LinkedList<String> sevenBitGroups = new LinkedList<String>();
		
		for (int i = 0; i <= data.length() - 7; i += 7)
			sevenBitGroups.add(data.substring(i, i + 7));
		
		for (String s: sevenBitGroups)
		{
			char[] compressedGroup = new char[4];
			for (int j = 0; j < s.length(); j++)
				compressedGroup[pBox[j]] = s.charAt(j);
			for (int k = 0; k < compressedGroup.length; k++)
				compressedKey += compressedGroup[k];
		}
		
		return compressedKey;
	}
	
	
	//Takes an input binary string and inverts each bit
	/* 1. A '0' becomes a '1'
	 * 2. A '1' becomes a '0'
	 */
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
	
	
	//Takes 2 input binary strings and XORs the 2
	/*  1. Both strings will be the same size
	 *  2. Using XOR truth table;
	 *  	(a) 0 0 = 0
	 *  	(b) 0 1 = 1
	 *  	(c) 1 0 = 1
	 *  	(d) 1 1 = 0
	 */
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