import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TripleDES 
{
	private String key1, key2, key3;			//keys
	private String IV;							//for now, use the same IV for each 3DES operation
	
	private File file;
	private BufferedWriter writer;
	private String newLine = System.getProperty("line.separator");
	
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
	
	private void writeToCapture(String data) throws IOException
	{
		if (file != null)
			writer.write(data);
	}
	
	private void splitKey(String mainKey)
	{
		//MainKey may require padding if it's less than 21 characters
		key1 = mainKey.substring(0, 7);			//1st 7 characters = key 1
		key2 = mainKey.substring(7, 14);		//2nd 7 characters = key 2
		key3 = mainKey.substring(14, 21);		//3rd 7 characters = key 3
	}
	
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
			writer.close();
		return result3;
	}
	
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
			writer.close();
		return result3.trim();
	}
}


