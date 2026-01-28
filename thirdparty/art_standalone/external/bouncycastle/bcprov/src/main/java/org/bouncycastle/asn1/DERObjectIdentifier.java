// Android-added: keep DER classes for backwards compatibility
package org.bouncycastle.asn1;

import java.nio.charset.StandardCharsets;

/**
 *
 * @deprecated Use ASN1ObjectIdentifier instead of this,
 */
public class DERObjectIdentifier
    extends ASN1ObjectIdentifier
{
    DERObjectIdentifier(byte[] bytes)
    {
        super(new String(bytes, StandardCharsets.UTF_8));
    }
}
// Android-added: keep DER classes for backwards compatibility
