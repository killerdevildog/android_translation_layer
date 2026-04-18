package android.security.keystore;

import android.util.Slog;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

public class AndroidKeyStore extends KeyStoreSpi {

	private final static String TAG = "AndroidKeyStore";
	static HashMap<String, Key> map = new HashMap<>();

	@Override
	public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
		Slog.i(TAG, "engineGetKey alias=" + alias + " password=" + Arrays.toString(password));
		return map.get(alias);
	}

	@Override
	public Certificate[] engineGetCertificateChain(String alias) {
		Slog.i(TAG, "engineGetCertificateChain(" + alias + ") called");
		return new Certificate[0];
	}

	@Override
	public Certificate engineGetCertificate(String alias) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineGetCertificate'");
	}

	@Override
	public Date engineGetCreationDate(String alias) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineGetCreationDate'");
	}

	@Override
	public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain)
	    throws KeyStoreException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineSetKeyEntry'");
	}

	@Override
	public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineSetKeyEntry'");
	}

	@Override
	public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineSetCertificateEntry'");
	}

	@Override
	public void engineDeleteEntry(String alias) throws KeyStoreException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineDeleteEntry'");
	}

	@Override
	public Enumeration<String> engineAliases() {
		Slog.i(TAG, "engineAliases() called");
		return Collections.emptyEnumeration();
	}

	@Override
	public boolean engineContainsAlias(String alias) {
		// TODO Auto-generated method stub
		Slog.i(TAG, "engineContainsAlias(" + alias + ") called");
		return map.containsKey(alias);
	}

	@Override
	public int engineSize() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineSize'");
	}

	@Override
	public boolean engineIsKeyEntry(String alias) {
		// TODO Auto-generated method stub
		return map.containsKey(alias);
	}

	@Override
	public boolean engineIsCertificateEntry(String alias) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String engineGetCertificateAlias(Certificate cert) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineGetCertificateAlias'");
	}

	@Override
	public void engineStore(OutputStream stream, char[] password)
	    throws IOException, NoSuchAlgorithmException, CertificateException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'engineStore'");
	}

	@Override
	public void engineLoad(InputStream stream, char[] password)
	    throws IOException, NoSuchAlgorithmException, CertificateException {
	}
}
