import java.util.LinkedList;
import java.io.BufferedWriter;
import java.io.IOException;

public class DES 
{
	private String[][] sBox = new String[2][16]; 	//S-Box (4 bit)
	private int[] pBox = {3, 2, 0, 1, 0, 3, 2}; 	//P-Box (compression type - compresses 7 bits to 4 bits)
	
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
	private LinkedList<String> subKeys;				//Stores the 16 subkeys generated from the input 56-bit key (above)
	
	private BufferedWriter writer;					//Used to write to a capture file (passed in by parent TripleDES object)
	private String newLine = System.getProperty("line.separator");
	
	
	
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
	
	
	
	
	public DES(String key, BufferedWriter writer) throws IOException	//CONSTRUCTOR: receives the 56-bit DES key
	{
		this.writer = writer;
		writeToCapture("56-bit key: " + key);
		constructSBox();										//First construct the 4-bit S-Box
		
		this.key = convertStringToBinary(key);					//Convert input key to binary, and pad it so it's 56-bits in size
		
		this.subKeys = generateSubKeys();						//Generate 16 x 56-bit sub-keys
		
		printKeys();
		printSBox();
		printPBox();
	}
	
	
	//Used to write data to a file (using the 'writer' object, which points to a .txt file)
	private void writeToCapture(String data) throws IOException
	{
		if (writer != null)
			writer.write(data + newLine);
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
		while (paddedData.length() < totalBinaryDigits)				//E.g. arguments ('10',8) outputs   '00000010'
			paddedData = "0" + paddedData;							//E.g. arguments ('110',4) outputs  '0110'
																	//E.g. arguments ('1101',8) outputs '00001101'
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
	
	
	
	private void printSBox() throws IOException						//Prints the S-Box in pairs (input and output)
	{
		String s = "S-BOX (4x4):" + newLine;
		s += "INPUT | OUTPUT" + newLine;
		s += "------|-------" + newLine;
		for (int i = 0; i < 16; i++)
			s += sBox[0][i] + "  | " + sBox[1][i] + newLine;
		writeToCapture(s);
		//System.out.println(s);
	}
	
	
	private void printPBox() throws IOException							//Prints the compression P-Box
	{
		String s = "P-BOX (7 bits compressed to 4 bits)" + newLine;
		s += "Bit at Position X goes to Position Y" + newLine;
		s += "X: | 1 | 2 | 3 | 4 | 5 | 6 | 7 |" + newLine;
		s += "Y: | 4 | 3 | 1 | 2 | 1 | 4 | 3 |" + newLine + newLine;
		writeToCapture(s);
	}
	
	
	private String convertStringToBinary(String s)					//Takes a string and converts it into a binary string
	{
		String binaryString = "";									
		byte[] bytes = s.getBytes();								//String is converted to an array of bytes using Java method 'getBytes()'
		for (byte b: bytes)											//For each byte, convert it to a binary string
			binaryString += pad(Integer.toBinaryString(b), 8);		//Add the byte's binary string to the local string 'binaryString'
		return binaryString;										//Return the final concatenated binary string
		
																	//E.g. string "te" becomes byte_array[116, 101]
																	//116 becomes 01110100
																	//101 becomes 01100101
																	//etc... so return 0111010001100101
	}																
																		
	
	
	
	private LinkedList<String> generateSubKeys()							//Generates 16 subkeys from the main 56-bit key
	{	
		LinkedList<String> generatedKeys = new LinkedList<String>();
		String shiftedKey = key;											//Get the main 56-bit key
		for (int i = 0; i < numberOfRounds; i++)							//Execute 16 times (we need 1 key per round (16))
		{
			shiftedKey = performShift(shiftedKey, 3, shiftDirection.LEFT);	//Get 56-bit key, shift it LEFT by 3
			shiftedKey = performInversion(shiftedKey);						//Then invert the digits (i.e. 0 becomes 1 and 1 becomes 0)
			shiftedKey = performShift(shiftedKey, 5, shiftDirection.RIGHT); //The shift the result RIGHT by 5
			generatedKeys.add(shiftedKey);									//Add this new 56-bit key to the list of subkeys
																			//Then use THIS key to create the next one, and so on...
																			
																			//E.g. Input DES key is key '101110' (VERY BASIC example)
																			// 101110 generates the next key which is 010100
																			// 010100 generates the next key which is 111010
																			// and so on...
		}
		return generatedKeys;
	}
	
	
	
	
	private String performShift(String data, int shift, shiftDirection direction) //Takes a binary string and shifts it by a value
	{																			  //and in a specified direction
		String bitsToMove = "";
		String shiftedString = "";
		
		switch (direction)
		{																		  	//E.g. shift 1011000 left by 2:
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
	
	
	
	private void printKeys() throws IOException										//Prints the input 56-bit
	{																				//and then the 16 x 56-bit subkeys
		String s = "In Binary:  " + key + newLine + newLine;
		s += "Subkeys:    Key i = Key (i - 1) [Shifted left by 3, inverted, shifted right by 5]" + newLine + newLine;
		int i = 1;
		for (String subKey: subKeys)
		{
			s += "Subkey " + formatSpaces(i) + ": " + subKey + System.getProperty("line.separator");
			i++;
		}
		writeToCapture(s);
		//System.out.println(s);
	}
	
	
	
			
	private String formatSpaces(int i)												//Helps neaten output so it's alligned
	{
		if (i < 10)
			return "" + i + "  ";
		else
			return "" + i + " ";
	}
		
	
	
	
	//The primary method which encrypts/decrypts data, it receives:
	// 1. Data to encrypt or decrypt
	// 2. Mode to use: ECB or CBC
	// 3. Intialization Vector (IV): If ECB is used, then null is passed
	// 4. Processing mode: Encrypt or Decrypt
	
	public String processData(String data, blockCipherMode cipherMode, String IV, processingMode mode) throws IOException
	{
		if (mode == DES.processingMode.ENCRYPT)
			writeToCapture("Data to encrypt         : " + data + newLine);
		else
			writeToCapture("Data to decrypt         : " + data + newLine);
		
		//First, pad the data so its character count is a multiple of 8 (since each block is 8 characters (64-bits))
		//E.g. "Hello World" becomes "Hello World     " (5 spaces) - character count is 16, no longer 11
		String paddedData = padWithSpaces(data);
		
		//Then convert the data into a list of decimal (ASCII) digits
		LinkedList<Integer> decimalData = convertToDecimal(paddedData);
		
		//Then convert each decimal in the list to its 8-bit binary equivalent
		LinkedList<String> binaryData = convertToBinary(decimalData);
		
		//Then segment the binary data into blocks of 64-bits (i.e. collect 8 elements at a time and concatenate them to make a block)
		LinkedList<String> blocks = segmentIntoBlocks(binaryData);
		
		//Then either encrypt or decrypt
		//Note: - Decryption is the same process as encryption, but with reverse subkey order
		//		  This is the advantage of using the DES Feistel Structure
		switch (mode)
		{
			case ENCRYPT: 
				return processBlocks(blocks, subKeys, cipherMode, IV, mode);
			case DECRYPT:
			{
				writeToCapture("!!!!NOTE: Subkeys used in REVERSE order for DECRYPTION" + newLine);
				return processBlocks(blocks, reverseOrderSubKeys(), cipherMode, IV, mode);
			}
		}
		return null;
	}
	
	
	
	
	
	private String padWithSpaces(String data) throws IOException //Takes a string and pads it with spaces
	{
		String paddedData = data;
		int spaceCount = 0;
		while (paddedData.length() % charactersPerBlock != 0)	//If the character count isn't divisible by 8, add a space at the end
		{
			paddedData += " ";			
			spaceCount ++;										//Keep doing so until the count is a multiple of 8
		}
		writeToCapture("Data padded with spaces : " + paddedData + "[Padded with '" + spaceCount + "' spaces]" + newLine);
		return paddedData;										
	}
	
	
	
	private LinkedList<Integer> convertToDecimal(String data) throws IOException	//Converts a string to a list of ASCII decimals
	{
		String s = "Data in ASCII           : ";
		LinkedList<Integer> decimals = new LinkedList<Integer>();
		for (int i = 0; i < data.length(); i++)
		{
			char c = data.charAt(i);											//E.g. 'A'
			int decimal = (int)c;												//Decimal = 65 (which is 'A's ASCII value)
			decimals.add(decimal);												//Add 65 to the list 'decimals'
			s += decimal + ",";													//Then return the 'decimals' list
		}
		writeToCapture(s + newLine);
		return decimals;
	}

	
	
	private LinkedList<String> convertToBinary(LinkedList<Integer> decimalData) throws IOException	//Converts list of decimals to list of 8-bit binary strings
	{
		String s = "Data in Binary          : ";
		LinkedList<String> binaryData = new LinkedList<String>();
		for (int i = 0; i < decimalData.size(); i++)
		{
			int decimal = decimalData.get(i);									// E.g. '65' (from previous method 'convertToDecimal()')
			String binary = Integer.toBinaryString(decimal);					// '65' becomes 1000001					  _
			binary = pad(binary, 8);											// add padding so it's 8 characters long: 01000001
			binaryData.add(binary);												// Add 01000001 to the list 'binaryData'
			s += binary + ",";
		}
		writeToCapture(s + newLine);
		return binaryData;
	}
	
	
	
	//This method takes a list of binary strings (from method 'convertToBinary()') and segments it into 64-bit blocks
	/* It does this by gathering 8 elements at a time (i.e. 8 binary strings)
	 * It then concatenates these 8 elements into 1 binary string, and adds it to the 'blocks' list
	 * 
	 * 
	 * E.g. let's say we have "Hello Nice World" as our data: We begin with method 'processData()'
	 * 		1. padWithSpaces() returns "Hello Nice World" (no spaces, it's character count 16, and already is a multiple of 8)
	 * 
	 * 											  H   e   l   l   o      N  i  c   e      W   o   r   l   d
	 * 		2. convertToDecimal() returns "List: [72 101 108 108 111 32 78 105 99 101 32 87 111 114 108 100]"
	 * 
	 * 												72	    101       108     108      101               78			... etc...
	 * 		3. convertToBinary() returns "List: [01001000 01100101 01101100 01101100 01101111 00100000 01001110 01101001 01100011 01100101 00100000 01010111 01101111 01110010 01101100 01100100 ]
	 * 		
	 * 																	H e l l o   N i													   c e _ W o r l d
	 * 		4. segmentIntoBlocks() returns "List: [0100100001100101011011000110110001101111001000000100111001101001, 0110001101100101001000000101011101101111011100100110110001100100]
	 */
	private LinkedList<String> segmentIntoBlocks(LinkedList<String> binaryData) throws IOException
	{
		String s = "Data in 64-bit Blocks   : ";
		LinkedList<String> blocks = new LinkedList<String>();
		for (int i = 0; i <= binaryData.size() - charactersPerBlock; i += charactersPerBlock)
		{
			String block = "";
			for (int j = i; j < (i + charactersPerBlock); j++)
				block += binaryData.get(j);
			blocks.add(block);
			s += block + ",";
		}
		writeToCapture(s + newLine);
		return blocks;
	}
	
	
	
	
	//This method takes the list of blocks (from method 'segmentIntoBlocks()') and encrypts/decrypts each one
	/* Method receives: 1. The list of blocks
	 * 					2. The list of subkeys
	 * 					3. Cipher mode: ECB or CBC
	 * 					4. IV (will be null if mode is ECB)
	 * 					5. Processing mode: Encrypt or Decrypt
	 */
	private String processBlocks(LinkedList<String> blocks, LinkedList<String> subKeys, blockCipherMode cipherMode, String IV, processingMode mode) throws IOException
	{
		LinkedList<String> processedBlocks = new LinkedList<String>();
		
		switch(cipherMode)
		{
			case ECB: //Electronic Code Book									//If we're using ECB
			{
				writeToCapture(newLine + newLine + "<<<<<< PROCESSING IN 'ECB' MODE >>>>>>" + newLine + newLine);
				for (String block: blocks)										//For each block
				{				
					if (mode == DES.processingMode.ENCRYPT)
						writeToCapture(newLine + "Plaintext Block " + (blocks.indexOf(block) + 1) + ": " + block + newLine);
					else
						writeToCapture("Ciphertext Block " + (blocks.indexOf(block) + 1) + ": " + block + newLine);

					String processedBlock = executeFeistel(block, subKeys, mode);		//Process the block using Feistel structure and subkeys
					processedBlocks.add(processedBlock);						//Add the resulting block to the list 'processedBlocks'
					writeToCapture(" > Processed Block:     " + processedBlock + newLine + newLine);
				}
				return convertBinaryToText(processedBlocks);					//Return the list of result blocks, converted back to text
			}
			
			case CBC: //Cipher Block Chaining 
			{
				writeToCapture(newLine + newLine + "<<<<<< PROCESSING IN 'CBC' MODE >>>>>>" + newLine + newLine);
				String previousBlock = pad(convertStringToBinary(IV), 64); 		//If we're using CBC, set 'previousBlock' to IV
																				//The IV needs to be padded so it's 64 bits in size
				writeToCapture("!!!CBC: Initial Previous Ciphertext = IV:               (" + previousBlock + ")");
				for (String block: blocks)										//For each block
				{
					switch (mode)												//If we're ENCRYPTING with CBC
					{
						case ENCRYPT:
						{
							writeToCapture(newLine + "Plaintext Block: " + (blocks.indexOf(block) + 1) + ":                                      " + block + newLine);
							writeToCapture("!!!CBC: Previous Ciphertext:                             " + previousBlock + newLine);
							String chain = performXOR(block, previousBlock);	//Get current block and XOR it with the previous block
							writeToCapture("!!!CBC: Current Block XOR with Previous Ciphertext:      " + chain + newLine);
								
							previousBlock = executeFeistel(chain, subKeys, mode);		//Then encrypt the result of the XOR, then make the result
																				        //be the 'previousBlock' for the NEXT block
							
							processedBlocks.add(previousBlock);					//Add the result to the list of 'processedBlocks'
							writeToCapture(" > Processed Block:     " + previousBlock + newLine + newLine);
							break;
						}
						case DECRYPT:												 //If we're DECRYPTING with CBC
						{
							writeToCapture(newLine + "Ciphertext Block " + (blocks.indexOf(block) + 1) + ":                                      " + block + newLine);
	
							String decryptedBlock = executeFeistel(block, subKeys, mode);	 //Decrypt the current block
							writeToCapture(" > Decrypted Block:     " + decryptedBlock + newLine + newLine);
							writeToCapture("!!!CBC: Previous Ciphertext:                             " + previousBlock + newLine);
							String chain = performXOR(decryptedBlock, previousBlock);//XOR it with the 'previousBlock' (initially equal to IV)
							writeToCapture("!!!CBC: Decrypted Block XOR with Previous Ciphertext:    " + chain + newLine);
							processedBlocks.add(chain);								 //Add th XOR result to the list of 'processedBlocks'
							previousBlock = block;									 //Make the initial ciphertext block the 'previousBlock'
							writeToCapture(" > Final Processed Block:                                " + chain + newLine + newLine);

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
	 * 		(a) put the right 32-bits in the Feistel function with the current round subkey (see 'feistelFunction()' method below)
	 * 		(b) XOR the result with the left 32-bits
	 * 		(c) next round uses the XOR result as its right 32-bit half
	 * 		(d) next round uses the current round's original right 32-bit half as the left 32-bit half
	 * 3. After 16th round is over
	 * 		(a) swap the left and right 32-bit halves
	 */
	private String executeFeistel(String block, LinkedList<String> subKeys, processingMode mode) throws IOException
	{		
		writeToCapture(" <<< START FEISTEL STRUCTURE >>>" + newLine);
		String entryLeft = block.substring(0, block.length() / 2);					//32-bit if block is 64-bit
		String entryRight =block.substring(block.length() / 2, block.length());		//32-bit if block is 64-bit
		for (int i = 0; i < numberOfRounds; i++)	
		{
			writeToCapture("   ------------------------------------------------------------------------------------------------------------");			
			writeToCapture("   <<< ROUND " + (i + 1) + " >>> --------------------------------------------------------------------------------------------");
			writeToCapture("   ------------------------------------------------------------------------------------------------------------");

			writeToCapture("       - Left 32 Bits :            " + entryLeft + newLine);
			writeToCapture("       - Right 32 Bits:            " + entryRight + newLine);
			
			//Feistel Structure Operations
			String originalRight = entryRight;
			String processedRight = feistelFunction(entryRight, subKeys.get(i), i, mode); //+ key
			entryRight = performXOR(entryLeft, processedRight);
			
			writeToCapture("       - Left 32 Bits :            " + entryLeft + newLine);
			writeToCapture("       - FUNCTION OUTPUT:          " + processedRight + newLine);
			
			writeToCapture("       - Left XOR FUNCTION OUTPUT: " + entryRight + newLine);
			entryLeft = originalRight;
			
			writeToCapture("       - Next Round Left 32 Bits:  " + entryLeft +  " [Round's Original Right Half]" + newLine);
			writeToCapture("       - Next Round Right 32 Bits: " + entryRight + " [Left XOR FUNCTION OUTPUT]" + newLine);
			//System.out.println(entryLeft + entryRight);
		}
		writeToCapture("   ------------------------------------------------------------------------------------------------------------");			
		writeToCapture(" <<< END FEISTEL STRUCTURE >>>" + newLine);
		writeToCapture(" > Swap Left and Right: " + entryRight + entryLeft + newLine);
		return (entryRight + entryLeft);
	}
	
	
	//Takes a 32-bit half and modifies it using the 56-bit round subkey
	/*	1. Compress the 56-bit round key into 32-bits using method 'compressKey()' (which uses the compression P-Box)
	 * 	2. Split the 32-bit half into left and right halves (16-bits each)
	 *  3. Process each half using the S-Box
	 *  4. Swap the 2 halves and concatenate them into a 32-bit string
	 *  5. Invert the bits of the new 32-bit string
	 *  6. XOR the 32-bit string with the compressed 32-bit key
	 */
	private String feistelFunction(String blockRightHalf, String roundKey, int round, processingMode mode) throws IOException
	{
		writeToCapture("      <<< START FEISTEL FUNCTION >>> [Input = RIGHT 32-bit half]" + newLine);
		if (mode == processingMode.ENCRYPT)
			writeToCapture("          - Round Key:        " + roundKey + "   [Subkey: " + (round + 1) + "]");
		else
			writeToCapture("          - Round Key:        " + roundKey + "   [Subkey: " + (15 - round + 1) + "]");
		//String compressedKey = roundKey.substring(0, 32);
		String compressedKey = compressKey(roundKey);
		
		writeToCapture("          - Input Half:       " + blockRightHalf);
		
		String leftHalf = blockRightHalf.substring(0, blockRightHalf.length() / 2);
		String rightHalf = blockRightHalf.substring(blockRightHalf.length() / 2, blockRightHalf.length());
				
		String sBoxLeftHalf = performSubstitution(leftHalf);
		String sBoxRightHalf = performSubstitution(rightHalf);
		String sBoxResult = sBoxLeftHalf + sBoxRightHalf;
		
		writeToCapture("          - S-Box Result:     " + sBoxResult + "   [per every group of 4-bits]");
		
		String swapResult = performSwap(sBoxResult);
		
		writeToCapture("          - Split Into Two:   " + sBoxLeftHalf + " [L] " + sBoxRightHalf + " [R]");
		writeToCapture("          - Swap Halves:      " + swapResult);
		
		String invertResult = performInversion(swapResult);
		
		writeToCapture("          - Bit Inversion:    " + invertResult);
		
		String xorResult = performXOR(invertResult, compressedKey);
		
		writeToCapture("          - Compressed Key:   " + compressedKey + "   [done by compression P-Box]");
		writeToCapture("          - XOR with Key:     " + xorResult + newLine);
		writeToCapture("          - FUNCTION OUTPUT:  " + xorResult + newLine);
		
		writeToCapture("      <<< END FEISTEL FUNCTION >>> [Output = NEW RIGHT 32-bit half]" + newLine);

		return xorResult;
	}
	
	
	//Takes a list of blocks (64-bits) and converts the entire list into text for final output
	/* 1. Take each 64-bit block
	 * 2. Convert each group of 8-bits of the block into decimal (ASCII)
	 * 3. Convert each decimal into a character
	 * 4. Add the character to the final output string 'data'
	 */
	private String convertBinaryToText(LinkedList<String> binaryData) throws IOException
	{
		String output = "FINAL PROCESSED DATA BLOCKS: ";
		String data = "";
		for (String s: binaryData)
		{
			for (int i = 0; i <= ((charactersPerBlock * 8) - 8); i += 8)
			{
				String currentCharacter = s.substring(i, (i + 8));
				int characterDecimal = Integer.parseInt(currentCharacter, 2);
				data += (char)characterDecimal;
			}
			output += s + ",";
		}
		writeToCapture(output + newLine);
		writeToCapture("BLOCKS IN ASCII:             " + data + newLine);
		return data;
	}
	
	//Takes an input binary string and performs substitution (S-Box)
	/* 1. Take each group of 4-bits
	 * 2. Look up the 4-bit group in the S-Box
	 * 3. Output the corresponding S-Box output value, add the value to string 'sBoxResult'
	 * 4. Return the final processed string 'sBoxResult'
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
		
	
	//Takes an input binary string and splits it into 2 equal-size halves
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