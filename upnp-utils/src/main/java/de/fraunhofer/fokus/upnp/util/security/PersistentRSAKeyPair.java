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
package de.fraunhofer.fokus.upnp.util.security;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import de.fraunhofer.fokus.upnp.util.FileHelper;

/**
 * This class is used to store and load a RSA key pair. This is a big security flaw and must be
 * replaced by a more appropriate solution (e.g. a TPM)
 * 
 * @author Alexander Koenig
 */
public class PersistentRSAKeyPair implements Key
{
  /**  */
  private static final long    serialVersionUID = 1L;

  private KeyPair              keyPair          = null;

  private BigInteger           privateExponent  = null;

  private BigInteger           publicExponent   = null;

  private BigInteger           modulus          = null;

  private PersistentRSAKeyPair keyInstance      = null;

  /** Creates a copy of an existing key pair */
  public PersistentRSAKeyPair(KeyPair keys)
  {
    keyInstance = this;
    this.keyPair = keys;
    if (keyPair != null)
    {
      privateExponent = ((RSAPrivateKey)keys.getPrivate()).getPrivateExponent();
      publicExponent = ((RSAPublicKey)keys.getPublic()).getPublicExponent();
      modulus = ((RSAKey)keys.getPrivate()).getModulus();
    }
  }

  /** Generates a new RSA key pair */
  public PersistentRSAKeyPair()
  {
    keyInstance = this;
    this.keyPair = PublicKeyCryptographyHelper.generateRSAKeyPair();
    if (keyPair != null)
    {
      privateExponent = ((RSAPrivateKey)keyPair.getPrivate()).getPrivateExponent();
      publicExponent = ((RSAPublicKey)keyPair.getPublic()).getPublicExponent();
      modulus = ((RSAKey)keyPair.getPrivate()).getModulus();
    }
  }

  /** Creates an instance with known values */
  public PersistentRSAKeyPair(BigInteger publicExponent, BigInteger privateExponent, BigInteger modulus)
  {
    keyInstance = this;
    this.privateExponent = privateExponent;
    this.publicExponent = publicExponent;
    this.modulus = modulus;
    if (isValid())
    {
      keyPair = new KeyPair(new RSAPublicKeyClass(), new RSAPrivateKeyClass());
    }
  }

  /** Loads a key pair from a file */
  public PersistentRSAKeyPair(String keyFile)
  {
    keyInstance = this;
    loadFromFile(keyFile);
    if (isValid())
    {
      keyPair = new KeyPair(new RSAPublicKeyClass(), new RSAPrivateKeyClass());
    }
  }

  /**
   * Tries to load a RSA key pair from a file. If no key is detected, a new key pair is generated
   * and saved in a new file with the given name.
   * 
   * @param keyFile
   *          Path and name to keyfile
   * 
   * @return The key from the file or a new key
   * 
   */
  public static KeyPair getPersistentKeyPair(String keyFile)
  {
    PersistentRSAKeyPair keyPair = null;
    // load persistent keys
    if (keyFile != null)
    {
      keyPair = new PersistentRSAKeyPair(keyFile);
    }
    // no keys found, generate new key pair
    if (keyPair == null || !keyPair.isValid())
    {
      System.out.println("Key from " + keyFile + " is missing or invalid, create new key");
      keyPair = new PersistentRSAKeyPair();
      // try to save to file
      if (keyFile != null)
      {
        keyPair.saveToFile(keyFile);
      }
    }
    return keyPair.getKeyPair();
  }

  /**
   * Creates a new RSA key pair.
   * 
   * @return A new key
   * 
   */
  public static KeyPair getTemporaryKeyPair()
  {
    PersistentRSAKeyPair keyPair = new PersistentRSAKeyPair();
    return keyPair.getKeyPair();
  }

  /** Returns the public key of this control point */
  public RSAPublicKey getPublicKey()
  {
    return (RSAPublicKey)keyPair.getPublic();
  }

  /** Returns the private key of this control point */
  public RSAPrivateKey getPrivateKey()
  {
    return (RSAPrivateKey)keyPair.getPrivate();
  }

  /** Retrieves the KeyPair associated with this instance */
  public KeyPair getKeyPair()
  {
    return keyPair;
  }

  /* (non-Javadoc)
   * @see java.security.Key#getAlgorithm()
   */
  public String getAlgorithm()
  {
    return "RSA";
  }

  /* (non-Javadoc)
   * @see java.security.Key#getEncoded()
   */
  public byte[] getEncoded()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see java.security.Key#getFormat()
   */
  public String getFormat()
  {
    return null;
  }

  public boolean isValid()
  {
    return privateExponent != null && publicExponent != null && modulus != null;
  }

  /** Saves the RSA key pair into a file. */
  public void saveToFile(String keyFileName)
  {
    // System.out.println(" Saving keys to file: "+keyFile);
    keyFileName = FileHelper.getAppropriateFileName(keyFileName);
    try
    {
      File keyFile = new File(keyFileName);
      if (!keyFile.getParentFile().exists())
      {
        keyFile.getParentFile().mkdirs();
      }

      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(keyFileName), "utf-8"));

      writer.write("Modulus:" + modulus.toString() + "\n");
      writer.write("PublicExponent:" + publicExponent.toString() + "\n");
      writer.write("PrivateExponent:" + privateExponent.toString() + "\n");
      writer.close();
    } catch (Exception ex)
    {
      System.out.println("Error:" + ex.getMessage());
    }
  }

  /** Loads the RSA key pair from a file. */
  private void loadFromFile(String keyFileName)
  {
    // System.out.println(" Loading keys from file: "+keyFile);
    keyFileName = FileHelper.getAppropriateFileName(keyFileName);
    if (new File(keyFileName).exists())
    {
      try
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(keyFileName), "utf-8"));
        String line;
        line = reader.readLine();
        while (line != null)
        {
          // make sure there wasn`t just an unnecessary newline at the end...
          // ignore comments
          if (line.length() > 0 && line.charAt(0) != '#')
          {
            // get individual items
            StringTokenizer tokens = new StringTokenizer(line, ":");
            try
            {
              String tag = tokens.nextToken();
              String value = tokens.nextToken();
              if (tag.compareToIgnoreCase("Modulus") == 0)
              {
                modulus = new BigInteger(value);
              }

              if (tag.compareToIgnoreCase("PrivateExponent") == 0)
              {
                privateExponent = new BigInteger(value);
              }

              if (tag.compareToIgnoreCase("PublicExponent") == 0)
              {
                publicExponent = new BigInteger(value);
              }
            } catch (NoSuchElementException nsee)
            {
              System.out.println("Exception while loading keys: " + nsee.getMessage());
            }
          }
          line = reader.readLine();
        }
        reader.close();
      } catch (Exception ex)
      {
        ex.printStackTrace(System.out);
      }
    }
  }

  /** Inner class to encapsulate the RSAPrivateKeyInterface */
  private class RSAPrivateKeyClass implements RSAPrivateKey
  {

    private static final long serialVersionUID = 1L;

    public String getAlgorithm()
    {
      return keyInstance.getAlgorithm();
    }

    public byte[] getEncoded()
    {
      return keyInstance.getEncoded();
    }

    public String getFormat()
    {
      return keyInstance.getFormat();
    }

    public BigInteger getModulus()
    {
      return modulus;
    }

    public BigInteger getPrivateExponent()
    {
      return privateExponent;
    }

  }

  private class RSAPublicKeyClass implements RSAPublicKey
  {

    private static final long serialVersionUID = 1L;

    public String getAlgorithm()
    {
      return keyInstance.getAlgorithm();
    }

    public byte[] getEncoded()
    {
      return keyInstance.getEncoded();
    }

    public String getFormat()
    {
      return keyInstance.getFormat();
    }

    public BigInteger getModulus()
    {
      return modulus;
    }

    public BigInteger getPublicExponent()
    {
      return publicExponent;
    }
  }

}
