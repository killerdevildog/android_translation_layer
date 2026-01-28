// Android-added: keep DER classes for backwards compatibility
package org.bouncycastle.asn1;

import java.io.IOException;

/**
 * @deprecated Use DLSetParser instead
 */
public class DERSetParser
    extends BERSetParser
{
    DERSetParser(ASN1StreamParser parser)
    {
        super(parser);
    }
}
// Android-added: keep DER classes for backwards compatibility
