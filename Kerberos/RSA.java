import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA
{
	private BigInteger e, d, n;

	private int bits = 512;

	public RSA()
	{
		SecureRandom r = new SecureRandom(); // Generate random number here

		BigInteger p = new BigInteger(bits / 2, 100, r); // generating p

		BigInteger q = new BigInteger(bits / 2, 100, r); // generating q

		n = p.multiply(q); // calculating n = pq

		BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE)); // Calculating (p - 1)(q - 1)
		e = new BigInteger("3");

		while(m.gcd(e).intValue() > 1)
			e = e.add(new BigInteger("2"));

		d = e.modInverse(m);
	}

	public BigInteger getE()
	{
		return this.e;
	}

	public BigInteger getD()
	{
		return this.d;
	}

	public BigInteger getN()
	{
		return this.n;
	}
}