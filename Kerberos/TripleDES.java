import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TripleDES 
{
	private String key1, key2, key3;			//The 3 DES keys (56-bits each)
	private String IV;							//The 64-bit IV (all 3 DES objects use the same IV)
	
	private File file;							//Object points to the capture file
	private BufferedWriter writer;				//Used to write to the capture file
	
	
	private String newLine = System.getProperty("line.separator");
	
	/*	- Constructor takes a key, IV and capture file
	 * 	- Makes a file object pointing to the capture file via its file-path
	 *  - Makes a writer which will be used to write data to the file
	 *  - Splits the 168-bitkey into 3 56-bit DES keys
	 *  - Sets the IV
	 */
	public TripleDES(String key, String IV, String captureFilePath) throws IOException
	{
		if (captureFilePath != null)
		{
			file = new File(captureFilePath);
			writer = new BufferedWriter(new FileWriter(file));
		}
		splitKey(key);
		this.IV = IV;
		writeInitialCaptureData(key);
	}
	
	/* - Writes initial data to the capture file
	 */
	private void writeInitialCaptureData(String key) throws IOException
	{	
		writeToCapture("Triple DES Capture" + newLine + newLine);
		writeToCapture("168-bit key: " + key + newLine + newLine);
		writeToCapture("3 x 56-bit DES keys: " + key1 + ", " + key2 + ", " + key3 + newLine + newLine);
		if (IV == null)
			writeToCapture("Initialization Vector: N/A (Using ECB)" + newLine + newLine);
		else
			writeToCapture("Initialization Vector: " + IV + newLine + newLine);
		writeToCapture("");
			
	}
	
	/*	- Takes data to write to the file
	 *  - Writes the data to the file
	 * 
	 */
	private void writeToCapture(String data) throws IOException
	{
		if (file != null)
			writer.write(data);
	}
	
	/*	- Splits the 168-bit (21 character) key into 3 56-bit (7-character) DES keys
	 * 
	 */
	private void splitKey(String mainKey)
	{
		//MainKey may require padding if it's less than 21 characters
		key1 = mainKey.substring(0, 7);			//1st 7 characters = key 1
		key2 = mainKey.substring(7, 14);		//2nd 7 characters = key 2
		key3 = mainKey.substring(14, 21);		//3rd 7 characters = key 3
	}
	
	/*	- The method which encrypts or decrypts data
	 * 	- The method receives the data to encrypt/decrypt, as well as the cipher-mode (CBC or ECB), and
	 *    the processing mode (encrypt or decrypt)
	 * 
	 */
	public String processData(String data, DES.blockCipherMode cipherMode, DES.processingMode mode) throws IOException
	{
		switch (mode)
		{
			case ENCRYPT: 
			{
				return encrypt(data, cipherMode);
			}
			case DECRYPT: 
			{
				return decrypt(data, cipherMode);
			}
		}
		return null;
	}
	
	
	/*	- Executes DES 3 times for ENCRYPTION
	 *  	1. Executes 1st DES in encryption mode (passing in key 1)
	 *  	2. Executes 2nd DES in decryption mode (passing in key 2)
	 *  	3. Executes 3rd DES in encryption mode (passing in key 3)
	 *  
	 *  - Each DES object is passed the same writer, because we want each DES to write to the same capture file, so then
	 *    the capture file shows the operations of all 3 DES executions sequentially
	 * 
	 */
	private String encrypt(String data, DES.blockCipherMode cipherMode) throws IOException
	{
		//3DES encryption: ENCRYPT[DECRYPT[ENCRYPT[data, key1], key2], key3]
		writeToCapture("=========================================================================================================================" + newLine);
		writeToCapture("STAGE 1: DES [ENCRYPT] ==================================================================================================" + newLine);
		writeToCapture("=========================================================================================================================" + newLine + newLine);

		String result1 = new DES(key1, writer).processData(data, 	cipherMode, IV, DES.processingMode.ENCRYPT);
		
		writeToCapture("=========================================================================================================================" + newLine);
		writeToCapture("STAGE 2: DES [DECRYPT] ==================================================================================================" + newLine);
		writeToCapture("=========================================================================================================================" + newLine + newLine);
		
		String result2 = new DES(key2, writer).processData(result1, cipherMode, IV, DES.processingMode.DECRYPT);
		
		writeToCapture("=========================================================================================================================" + newLine);
		writeToCapture("STAGE 3: DES [ENCRYPT] ==================================================================================================" + newLine);
		writeToCapture("=========================================================================================================================" + newLine + newLine);
		
		String result3 = new DES(key3, writer).processData(result2, cipherMode, IV, DES.processingMode.ENCRYPT);
		
		if (writer != null)
			writer.close();	//Close the writer, we're done writing to the file
		return result3;
	}
	
	
	/*	- Executes DES 3 times for DECRYPTION
	 *  	1. Executes 1st DES in decryption mode (passing in key 3)
	 *  	2. Executes 2nd DES in encryption mode (passing in key 2)
	 *  	3. Executes 3rd DES in decryption mode (passing in key 1)
	 *  
	 *  - Each DES object is passed the same writer, because we want each DES to write to the same capture file, so then
	 *    the capture file shows the operations of all 3 DES executions sequentially
	 *    
	 *  - IMPORTANT:
	 *  	> When we encrypt plaintext from the method above 'encrypt()', we might add spaces for padding
	 *  	> When we decrypt and obtain the initial plaintext, we want to remove the trailing spaces
	 *  	> This is what 'trim()' is used for at the end
	 * 
	 */
	private String decrypt(String cipher, DES.blockCipherMode cipherMode) throws IOException
	{
		//3DES decryption: DECRYPT[ENCRYPT[DECRYPT[cipher, key3], key2], key1]
		writeToCapture("=========================================================================================================================" + newLine);
		writeToCapture("STAGE 1: DES [DECRYPT] ==================================================================================================" + newLine);
		writeToCapture("=========================================================================================================================" + newLine + newLine);
		
		String result1 = new DES(key3, writer).processData(cipher, 	cipherMode, IV, DES.processingMode.DECRYPT);
		
		writeToCapture("=========================================================================================================================" + newLine);
		writeToCapture("STAGE 2: DES [ENCRYPT] ==================================================================================================" + newLine);
		writeToCapture("=========================================================================================================================" + newLine + newLine);
		
		String result2 = new DES(key2, writer).processData(result1, cipherMode, IV, DES.processingMode.ENCRYPT);
		
		writeToCapture("=========================================================================================================================" + newLine);
		writeToCapture("STAGE 3: DES [DECRYPT] ==================================================================================================" + newLine);
		writeToCapture("=========================================================================================================================" + newLine + newLine);
		
		//include code to trim off spaces at the end
		String result3 = new DES(key1, writer).processData(result2, cipherMode, IV, DES.processingMode.DECRYPT);
		
		if (writer != null)
			writer.close();		//Close the writer, we're done writing to the file
		return result3.trim();
	}
}


