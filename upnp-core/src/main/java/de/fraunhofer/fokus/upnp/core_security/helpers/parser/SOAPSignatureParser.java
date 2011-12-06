/**
* 
* Copyright (C) 2004-2008 FhG Fokus
*
* This file is part of the FhG Fokus UPnP stack - an open source UPnP implementation
* with some additional features
*
* You can redistribute the FhG Fokus UPnP stack and/or modify it
* under the terms of the GNU General Public License Version 3 as published by
* the Free Software Foundation.
*
* For a license to use the FhG Fokus UPnP stack software under conditions
* other than those described here, or to purchase support for this
* software, please contact Fraunhofer FOKUS by e-mail at the following
* addresses:
*   upnpstack@fokus.fraunhofer.de
*
* The FhG Fokus UPnP stack is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see <http://www.gnu.org/licenses/>
* or write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*
*/
package de.fraunhofer.fokus.upnp.core_security.helpers.parser;

import java.math.BigInteger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.fraunhofer.fokus.upnp.util.Base64Helper;
import de.fraunhofer.fokus.upnp.util.SAXTemplateHandler;
import de.fraunhofer.fokus.upnp.util.security.DigestHelper;
import de.fraunhofer.fokus.upnp.util.security.PersistentRSAPublicKey;
import de.fraunhofer.fokus.upnp.util.security.PublicKeyCryptographyHelper;

/**
 * 
 * @author Alexander Koenig
 */
public class SOAPSignatureParser
{

  // private final String CANONICALIZATION_ALGORITHM = "http://www.w3.org/2001/10/xml-exc-c14n#";
  private final String           CONTROL_URL_TAG                 = "controlURL";

  // private final String DIGEST_ALGORITHM = "http://www.w3.org/2001/10/xmldsig#sha1";
  private final String           DIGEST_VALUE_TAG                = "DigestValue";

  // private final String DS_KEY_INFO_TAG = "ds:KeyInfo";
  private final String           EXPONENT_TAG                    = "Exponent";

  private final String           FRESHNESS_TAG                   = "Freshness";

  private final String           HEADER_TAG                      = "s:Header";

  private final String           KEY_INFO_TAG                    = "KeyInfo";

  private final String           KEY_NAME_TAG                    = "KeyName";

  private final String           KEY_VALUE_TAG                   = "KeyValue";

  private final String           LIFETIME_SEQUENCE_BASE_TAG      = "LifetimeSequenceBase";

  private final String           MODULUS_TAG                     = "Modulus";

  private final String           REFERENCE_TAG                   = "Reference";

  private final String           RSA_KEY_VALUE_TAG               = "RSAKeyValue";

  private final String           SEQUENCE_BASE_TAG               = "SequenceBase";

  private final String           SEQUENCE_NUMBER_TAG             = "SequenceNumber";

  // private final String SIGNATURE_ALGORITHM = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
  // private final String SIGNATURE_NS = "http://www.w3.org/2000/09/xmldsig#";
  private final String           SIGNATURE_TAG                   = "Signature";

  private final String           SIGNATURE_VALUE_TAG             = "SignatureValue";

  private PersistentRSAPublicKey publicKey                       = null;

  private boolean                isValidPublicKeySignature       = false;

  private boolean                hasValidDigests                 = false;

  private boolean                containsSignature               = false;

  private String                 calculatedFreshnessDigestBase64 = "";

  private String                 calculatedBodyDigestBase64      = "";

  /** String that holds the value the signature is calculated from */
  private String                 signatureSource                 = "";

  /** String that holds the parsed lifetimeSequenceBase */
  private String                 lifetimeSequenceBase            = "";

  /** String that holds the parsed controlURL */
  private String                 controlURL                      = "";

  /** String that holds the parsed sequenceBase */
  private String                 sequenceBase                    = "";

  /** String that holds the parsed sequenceNumber */
  private String                 sequenceNumber                  = "";

  /** String that holds the parsed signature value */
  private String                 signatureValueBase64            = "";

  /** String that holds the parsed keyName */
  private String                 keyName                         = "";

  /** Creates a new instance of AlgorithmsAndProtocolsParser */
  public SOAPSignatureParser(String parseText)
  {

    // System.out.println("Parse text is "+parseText);

    // retrieve freshness block
    int startIndex = parseText.indexOf("<Freshness");
    int endIndex = parseText.indexOf("</Freshness>");
    if (startIndex != -1 && endIndex != -1)
    {
      while (startIndex < endIndex && parseText.charAt(startIndex) != '>')
      {
        startIndex++;
      }
      if (startIndex < endIndex)
      {
        String freshness = parseText.substring(startIndex + 1, endIndex);
        // calculate digest
        calculatedFreshnessDigestBase64 =
          Base64Helper.byteArrayToBase64(DigestHelper.calculateSHAHashForString(freshness));

        // System.out.println("Calculated freshness digest is "+calculatedFreshnessDigestBase64);
      }
    }
    // retrieve body block
    startIndex = parseText.indexOf("<s:Body");
    endIndex = parseText.indexOf("</s:Body>");
    if (startIndex != -1 && endIndex != -1)
    {
      while (startIndex < endIndex && parseText.charAt(startIndex) != '>')
      {
        startIndex++;
      }
      if (startIndex < endIndex)
      {
        String body = parseText.substring(startIndex + 1, endIndex);
        // calculate digest
        calculatedBodyDigestBase64 = Base64Helper.byteArrayToBase64(DigestHelper.calculateSHAHashForString(body));

        // System.out.println("Calculated body digest is "+calculatedBodyDigestBase64);
      }
    }
    // retrieve signature source
    startIndex = parseText.indexOf("<SignedInfo");
    endIndex = parseText.indexOf("</SignedInfo>");
    if (startIndex != -1 && endIndex != -1)
    {
      while (startIndex < endIndex && parseText.charAt(startIndex) != '>')
      {
        startIndex++;
      }
      if (startIndex < endIndex)
      {
        signatureSource = parseText.substring(startIndex + 1, endIndex);
      }
    }
    // only parse message with signatures
    if (signatureSource.length() != 0)
    {
      containsSignature = true;
      // parse message
      try
      {
        DefaultHandlerClass parserClass = new DefaultHandlerClass();
        parserClass.parse(parseText);
      } catch (Exception ex)
      {
        System.err.println("Error:" + ex.getMessage());
      }
    }
  }

  public String getLifetimeSequenceBase()
  {
    return lifetimeSequenceBase;
  }

  public String getControlURL()
  {
    return controlURL;
  }

  public String getSequenceBase()
  {
    return sequenceBase;
  }

  public String getSequenceNumber()
  {
    return sequenceNumber;
  }

  public PersistentRSAPublicKey getPublicKey()
  {
    return publicKey;
  }

  public String getKeyName()
  {
    return keyName;
  }

  public String getSignatureBase64()
  {
    return signatureValueBase64;
  }

  public String getSignatureSource()
  {
    return signatureSource;
  }

  public boolean containsSignature()
  {
    return containsSignature;
  }

  /** Checks for a public key signature */
  public boolean isPublicKeySignature()
  {
    return publicKey != null && lifetimeSequenceBase.length() != 0;
  }

  /** Checks if a public key signature is valid */
  public boolean isValidPublicKeySignature()
  {
    return isPublicKeySignature() && isValidPublicKeySignature;
  }

  public boolean isSymmetricSignature()
  {
    return keyName.length() != 0 && sequenceBase.length() != 0 && sequenceNumber.length() != 0;
  }

  /** This inner class is used for the actual parsing */
  private class DefaultHandlerClass extends SAXTemplateHandler
  {
    private String     bodyDigestBase64      = "";

    private String     freshnessDigestBase64 = "";

    private String     referenceURI          = "";

    private BigInteger modulus;

    private BigInteger exponent;

    /**
     * Checks the start elements of the XML stream
     */
    public void processStartElement(String uri, String name, String qName, Attributes atts) throws SAXException
    {
      // check for reference
      if (getTagCount() == 6 && getCurrentTag().equals(REFERENCE_TAG))
      {
        referenceURI = "";
        for (int i = 0; i < atts.getLength(); i++)
        {
          if (atts.getQName(i).equals("URI"))
          {
            referenceURI = atts.getValue(i);
          }
        }
      }
    }

    /**
     * Checks the end elements of the XML stream
     */
    public void processEndElement(String uri, String localName, String qName) throws SAXException
    {
      if (getTagCount() == 7 && getCurrentTag().equals(RSA_KEY_VALUE_TAG))
      {
        if (modulus != null && exponent != null)
        {
          publicKey = new PersistentRSAPublicKey(exponent, modulus);
          modulus = null;
          exponent = null;
        }
      }
      // check for reference
      if (getTagCount() == 6 && getCurrentTag().equals(REFERENCE_TAG))
      {
        referenceURI = "";
      }
      if (getTagCount() == 2)
      {
        // end of header found, process gathered information
        if (getCurrentTag().equals(HEADER_TAG) && signatureSource.length() > 0 && freshnessDigestBase64.length() > 0 &&
          bodyDigestBase64.length() > 0 && signatureValueBase64.length() > 0)
        {
          boolean bodyVerified = calculatedBodyDigestBase64.equals(bodyDigestBase64);
          boolean freshnessVerified = calculatedFreshnessDigestBase64.equals(freshnessDigestBase64);

          hasValidDigests = bodyVerified && freshnessVerified;

          // try to check public key signature
          if (publicKey != null)
          {
            boolean signatureVerified =
              PublicKeyCryptographyHelper.verifyRSASignatureForString(publicKey, signatureSource, signatureValueBase64);

            isValidPublicKeySignature = hasValidDigests && signatureVerified;
          }

          /*
           * System.out.println("Verify signature"); System.out.println("Body is "+(bodyVerified ?
           * "verified" : "invalid")); System.out.println("Freshness is "+(freshnessVerified ?
           * "verified" : "invalid")); System.out.println("Signature is "+(signatureVerified ?
           * "verified" : "invalid"));
           */
        }
      }
    }

    /**
     * Methods is called if content was found
     */
    public void processContentElement(String content)
    {
      // retrieve public key
      if (getTagCount() == 8 && getTag(4).equals(KEY_INFO_TAG) && getTag(5).equals(KEY_VALUE_TAG) &&
        getTag(6).equals(RSA_KEY_VALUE_TAG))
      {
        if (getCurrentTag().equals(MODULUS_TAG))
        {
          byte[] modulusBytes = Base64Helper.base64ToByteArray(content);
          modulus = new BigInteger(modulusBytes);
        }
        if (getCurrentTag().equals(EXPONENT_TAG))
        {
          byte[] exponentBytes = Base64Helper.base64ToByteArray(content);
          exponent = new BigInteger(exponentBytes);
        }
      }
      // retrieve session key
      if (getTagCount() == 6 && getTag(4).equals(KEY_INFO_TAG) && getTag(5).equals(KEY_NAME_TAG))
      {
        keyName = content;
      }
      // retrieve digest value
      if (getTagCount() == 7 && getTag(5).equals(REFERENCE_TAG) && getCurrentTag().equals(DIGEST_VALUE_TAG))
      {
        if (referenceURI.equals("#Body"))
        {
          bodyDigestBase64 = content;
        }

        if (referenceURI.equals("#Freshness"))
        {
          freshnessDigestBase64 = content;
        }
      }
      // retrieve freshness data
      if (getTagCount() == 5 && getTag(3).equals(FRESHNESS_TAG))
      {
        if (getCurrentTag().equals(LIFETIME_SEQUENCE_BASE_TAG))
        {
          lifetimeSequenceBase = content;
        }

        if (getCurrentTag().equals(CONTROL_URL_TAG))
        {
          controlURL = content;
        }

        if (getCurrentTag().equals(SEQUENCE_BASE_TAG))
        {
          sequenceBase = content;
        }

        if (getCurrentTag().equals(SEQUENCE_NUMBER_TAG))
        {
          sequenceNumber = content;
        }
      }
      // fill signature data
      if (getTagCount() == 5 && getTag(3).equals(SIGNATURE_TAG) && getCurrentTag().equals(SIGNATURE_VALUE_TAG))
      {
        signatureValueBase64 = content;
      }
    }

  }
}
