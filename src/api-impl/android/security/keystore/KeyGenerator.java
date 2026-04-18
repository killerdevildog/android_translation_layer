package android.security.keystore;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;

public abstract class KeyGenerator extends KeyGeneratorSpi {

	protected javax.crypto.KeyGenerator keyGenerator;
	protected AlgorithmParameterSpec params;

	public static class AES extends KeyGenerator {
		@Override
		protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
		    throws InvalidAlgorithmParameterException {
			try {
				keyGenerator = javax.crypto.KeyGenerator.getInstance("AES", "BC");
				this.params = params;
				keyGenerator.init(random);
			} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
				e.printStackTrace();
				throw new UnsupportedOperationException("Unimplemented method 'engineInit'");
			}
		}
	}

	public static class HmacSHA512 extends KeyGenerator {
		@Override
		protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
		    throws InvalidAlgorithmParameterException {
			try {
				keyGenerator = javax.crypto.KeyGenerator.getInstance("HmacSHA512", "BC");
				this.params = params;
				keyGenerator.init(random);
			} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
				e.printStackTrace();
				throw new UnsupportedOperationException("Unimplemented method 'engineInit'");
			}
		}
	}

	@Override
	protected SecretKey engineGenerateKey() {
		System.out.println("generating key with alias " + ((KeyGenParameterSpec)params).getKeystoreAlias());
		SecretKey key = keyGenerator.generateKey();
		AndroidKeyStore.map.put(((KeyGenParameterSpec)params).getKeystoreAlias(), key);
		return key;
	}

	@Override
	protected void engineInit(SecureRandom random) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineInit'");
	}

	@Override
	protected void engineInit(int keysize, SecureRandom random) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineInit'");
	}
}
