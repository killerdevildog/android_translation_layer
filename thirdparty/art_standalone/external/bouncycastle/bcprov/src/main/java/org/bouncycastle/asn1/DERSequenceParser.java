// Android-added: keep DER classes for backwards compatibility
package org.bouncycastle.asn1;

import java.io.IOException;

/**
 * @deprecated Use DLSequenceParser instead
 */
public class DERSequenceParser
    extends BERSequenceParser
{
    DERSequenceParser(ASN1StreamParser parser)
    {
        super(parser);
    }
}
// Android-added: keep DER classes for backwards compatibility
