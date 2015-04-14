import java.util.*;

public class TripleDES 
{
	private String[][] sBox = new String[2][16]; //Using a 4-bit S-Box

	private LinkedList<String> subKeys = new LinkedList<String>();
	
	public enum processingType
	{
		ENCRYPT, DECRYPT
	}
	
	public TripleDES()
	{
		constructSBox();
		printSBox();
	}
	
	private void printSBox()
	{
		String s = "INPUT | OUTPUT" + "\n";
		s += "------|-------" + "\n";
		for (int i = 0; i < 16; i++)
		{
			s += sBox[0][i] + "  | " + sBox[1][i] + "\n";
		}
		System.out.println(s);
	}
	
	public void processData(String data, String key, processingType mode)
	{
		
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
