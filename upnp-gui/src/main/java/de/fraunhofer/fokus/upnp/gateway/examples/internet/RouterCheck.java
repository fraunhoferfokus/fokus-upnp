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
package de.fraunhofer.fokus.upnp.gateway.examples.internet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.fraunhofer.fokus.upnp.gateway.internet.InternetManagementConstants;
import de.fraunhofer.fokus.upnp.util.ThreadHelper;
import de.fraunhofer.fokus.upnp.util.network.BinaryMessageObject;
import de.fraunhofer.fokus.upnp.util.network.IPHelper;
import de.fraunhofer.fokus.upnp.util.network.SocketHelper;

/**
 * @author Alexander Koenig
 * 
 */
public class RouterCheck extends JFrame implements Runnable
{

  private static final long serialVersionUID                = 1L;

  private JPanel            jContentPane                    = null; // @jve:decl-index=0:visual-constraint="75,23"

  private JPanel            jBackgroundPanel                = null;

  private JPanel            jFillPanel                      = null;

  private JLabel            jAddressLabel                   = null;

  private JCheckBox         jGatewayMSEARCHReceiveCheckBox  = null;

  private JButton           jGatewayUDPButton               = null;

  private JCheckBox         jGatewayHTTPReceiveCheckBox     = null;

  private JButton           jGatewayHTTPButton              = null;

  private JCheckBox         jGatewayHTTPSendCheckBox        = null;

  private JComboBox         jAddressComboBox                = null;

  private JLabel            jStatusLabel                    = null;

  private JCheckBox         jDeviceDiscoveryReceiveCheckBox = null;

  private JCheckBox         jDeviceMSEARCHReceiveCheckBox   = null;

  private JCheckBox         jDeviceHTTPReceiveCheckBox      = null;

  private JCheckBox         jDeviceHTTPSendCheckBox         = null;

  private JButton           jDeviceDiscoveryButton          = null;

  private JButton           jDeviceMSEARCHButton            = null;

  private JButton           jDeviceHTTPButton               = null;

  private JButton           jButton3                        = null;

  private boolean           terminateThread                 = false;

  private boolean           terminated                      = false;

  private DatagramSocket    deviceDirectoryMSearchSocket;

  private ServerSocket      deviceDirectoryHTTPSocket;

  private DatagramSocket    deviceDiscoverySocket;

  private DatagramSocket    deviceMSearchSocket;

  private ServerSocket      deviceHTTPSocket;

  private JButton           jTestAllButton                  = null;

  private JButton           jClearAllButton                 = null;

  /**
   * This method initializes jBackgroundPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJBackgroundPanel()
  {
    if (jBackgroundPanel == null)
    {
      GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
      gridBagConstraints10.gridx = 0;
      gridBagConstraints10.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints10.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints10.gridy = 11;
      GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
      gridBagConstraints9.gridx = 1;
      gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints9.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints9.gridy = 11;
      GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
      gridBagConstraints8.gridx = 1;
      gridBagConstraints8.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints8.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints8.gridy = 13;
      GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
      gridBagConstraints7.gridx = 1;
      gridBagConstraints7.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints7.gridheight = 2;
      gridBagConstraints7.gridy = 9;
      GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
      gridBagConstraints61.gridx = 1;
      gridBagConstraints61.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints61.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints61.gridy = 8;
      GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
      gridBagConstraints51.gridx = 1;
      gridBagConstraints51.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints51.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints51.gridy = 7;
      GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
      gridBagConstraints42.gridx = 0;
      gridBagConstraints42.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints42.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints42.gridy = 10;
      GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
      gridBagConstraints32.gridx = 0;
      gridBagConstraints32.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints32.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints32.gridy = 9;
      GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
      gridBagConstraints22.gridx = 0;
      gridBagConstraints22.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints22.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints22.gridy = 8;
      GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
      gridBagConstraints12.gridx = 0;
      gridBagConstraints12.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints12.insets = new java.awt.Insets(1, 2, 2, 2);
      gridBagConstraints12.gridy = 7;
      GridBagConstraints gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints.gridy = 13;
      jStatusLabel = new JLabel();
      jStatusLabel.setText("");
      jStatusLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
      GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
      gridBagConstraints41.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints41.gridy = 1;
      gridBagConstraints41.weightx = 1.0;
      gridBagConstraints41.gridwidth = 2;
      gridBagConstraints41.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints41.gridx = 0;
      GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
      gridBagConstraints31.gridx = 0;
      gridBagConstraints31.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints31.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints31.gridy = 6;
      GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
      gridBagConstraints21.gridx = 1;
      gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints21.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints21.gridheight = 2;
      gridBagConstraints21.gridy = 5;
      GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
      gridBagConstraints11.gridx = 0;
      gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints11.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints11.gridy = 5;
      GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
      gridBagConstraints6.gridx = 1;
      gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints6.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints6.gridy = 4;
      GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
      gridBagConstraints5.gridx = 0;
      gridBagConstraints5.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints5.gridy = 4;
      GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
      gridBagConstraints4.gridx = 0;
      gridBagConstraints4.insets = new java.awt.Insets(2, 2, 2, 2);
      gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints4.gridy = 0;
      jAddressLabel = new JLabel();
      jAddressLabel.setText("Adresse");
      jAddressLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
      GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
      gridBagConstraints2.gridx = 0;
      gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints2.weightx = 1.0;
      gridBagConstraints2.weighty = 1.0;
      gridBagConstraints2.gridwidth = 2;
      gridBagConstraints2.gridy = 13;
      jBackgroundPanel = new JPanel();
      jBackgroundPanel.setLayout(new GridBagLayout());
      jBackgroundPanel.add(getJFillPanel(), gridBagConstraints2);
      jBackgroundPanel.add(jAddressLabel, gridBagConstraints4);
      jBackgroundPanel.add(getJGatewayMSEARCHReceiveCheckBox(), gridBagConstraints5);
      jBackgroundPanel.add(getJTestAllButton(), gridBagConstraints9);
      jBackgroundPanel.add(getJClearAllButton(), gridBagConstraints10);
      jBackgroundPanel.add(getJGatewayUDPButton(), gridBagConstraints6);
      jBackgroundPanel.add(getJGatewayHTTPReceiveCheckBox(), gridBagConstraints11);
      jBackgroundPanel.add(getJGatewayHTTPButton(), gridBagConstraints21);
      jBackgroundPanel.add(getJGatewayHTTPSendCheckBox(), gridBagConstraints31);
      jBackgroundPanel.add(getJAddressComboBox(), gridBagConstraints41);
      jBackgroundPanel.add(jStatusLabel, gridBagConstraints);
      jBackgroundPanel.add(getJDeviceDiscoveryReceiveCheckBox(), gridBagConstraints12);
      jBackgroundPanel.add(getJDeviceMSEARCHReceiveCheckBox(), gridBagConstraints22);
      jBackgroundPanel.add(getJDeviceHTTPReceiveCheckBox(), gridBagConstraints32);
      jBackgroundPanel.add(getJDeviceHTTPSendCheckBox(), gridBagConstraints42);
      jBackgroundPanel.add(getJDeviceDiscoveryButton(), gridBagConstraints51);
      jBackgroundPanel.add(getJDeviceMSEARCHButton(), gridBagConstraints61);
      jBackgroundPanel.add(getJDeviceHTTPButton(), gridBagConstraints7);
      jBackgroundPanel.add(getJButton3(), gridBagConstraints8);
    }
    return jBackgroundPanel;
  }

  /**
   * This method initializes jFillPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJFillPanel()
  {
    if (jFillPanel == null)
    {
      jFillPanel = new JPanel();
    }
    return jFillPanel;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getJGatewayMSEARCHReceiveCheckBox()
  {
    if (jGatewayMSEARCHReceiveCheckBox == null)
    {
      jGatewayMSEARCHReceiveCheckBox = new JCheckBox();
      jGatewayMSEARCHReceiveCheckBox.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
      jGatewayMSEARCHReceiveCheckBox.setText("Empfange Gateway-M-SEARCH");
    }
    return jGatewayMSEARCHReceiveCheckBox;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getJGatewayUDPButton()
  {
    if (jGatewayUDPButton == null)
    {
      jGatewayUDPButton = new JButton();
      jGatewayUDPButton.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
      jGatewayUDPButton.setText("Teste Gateway-M-SEARCH");
      jGatewayUDPButton.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(java.awt.event.ActionEvent e)
        {
          try
          {
            DatagramPacket packet =
              new DatagramPacket(new byte[] {
                  0, 0, 0, 0
              },
                4,
                InetAddress.getByName(getJAddressComboBox().getSelectedItem().toString()),
                deviceDirectoryMSearchSocket.getLocalPort());
            deviceDirectoryMSearchSocket.send(packet);
          } catch (Exception ex)
          {
          }
        }
      });
    }
    return jGatewayUDPButton;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getJGatewayHTTPReceiveCheckBox()
  {
    if (jGatewayHTTPReceiveCheckBox == null)
    {
      jGatewayHTTPReceiveCheckBox = new JCheckBox();
      jGatewayHTTPReceiveCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jGatewayHTTPReceiveCheckBox.setText("Empfange Gateway-HTTP");
    }
    return jGatewayHTTPReceiveCheckBox;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getJGatewayHTTPButton()
  {
    if (jGatewayHTTPButton == null)
    {
      jGatewayHTTPButton = new JButton();
      jGatewayHTTPButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jGatewayHTTPButton.setText("Teste Gateway-HTTP");
      jGatewayHTTPButton.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(java.awt.event.ActionEvent e)
        {
          try
          {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(getJAddressComboBox().getSelectedItem().toString(),
              InternetManagementConstants.HTTP_DEVICE_DIRECTORY_DEVICE_REQUEST_PORT), 5000);
            jGatewayHTTPSendCheckBox.setSelected(true);
            socket.close();
          } catch (Exception ex)
          {
            System.out.println("Could not connect to remote gateway server");
            jGatewayHTTPSendCheckBox.setSelected(false);
          }
        }
      });
    }
    return jGatewayHTTPButton;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getJGatewayHTTPSendCheckBox()
  {
    if (jGatewayHTTPSendCheckBox == null)
    {
      jGatewayHTTPSendCheckBox = new JCheckBox();
      jGatewayHTTPSendCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jGatewayHTTPSendCheckBox.setText("Kann senden Gateway-HTTP");
    }
    return jGatewayHTTPSendCheckBox;
  }

  /**
   * This method initializes jAddressComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getJAddressComboBox()
  {
    if (jAddressComboBox == null)
    {
      jAddressComboBox = new JComboBox();
      jAddressComboBox.setEditable(true);
      jAddressComboBox.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
    }
    return jAddressComboBox;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getJDeviceDiscoveryReceiveCheckBox()
  {
    if (jDeviceDiscoveryReceiveCheckBox == null)
    {
      jDeviceDiscoveryReceiveCheckBox = new JCheckBox();
      jDeviceDiscoveryReceiveCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jDeviceDiscoveryReceiveCheckBox.setText("Empfange Discovery");
    }
    return jDeviceDiscoveryReceiveCheckBox;
  }

  /**
   * This method initializes jCheckBox1
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getJDeviceMSEARCHReceiveCheckBox()
  {
    if (jDeviceMSEARCHReceiveCheckBox == null)
    {
      jDeviceMSEARCHReceiveCheckBox = new JCheckBox();
      jDeviceMSEARCHReceiveCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jDeviceMSEARCHReceiveCheckBox.setText("Empfange M-SEARCH");
    }
    return jDeviceMSEARCHReceiveCheckBox;
  }

  /**
   * This method initializes jCheckBox2
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getJDeviceHTTPReceiveCheckBox()
  {
    if (jDeviceHTTPReceiveCheckBox == null)
    {
      jDeviceHTTPReceiveCheckBox = new JCheckBox();
      jDeviceHTTPReceiveCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jDeviceHTTPReceiveCheckBox.setText("Empfange HTTP");
    }
    return jDeviceHTTPReceiveCheckBox;
  }

  /**
   * This method initializes jCheckBox3
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getJDeviceHTTPSendCheckBox()
  {
    if (jDeviceHTTPSendCheckBox == null)
    {
      jDeviceHTTPSendCheckBox = new JCheckBox();
      jDeviceHTTPSendCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jDeviceHTTPSendCheckBox.setText("Kann senden HTTP");
    }
    return jDeviceHTTPSendCheckBox;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getJDeviceDiscoveryButton()
  {
    if (jDeviceDiscoveryButton == null)
    {
      jDeviceDiscoveryButton = new JButton();
      jDeviceDiscoveryButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jDeviceDiscoveryButton.setText("Teste Discovery");
      jDeviceDiscoveryButton.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(java.awt.event.ActionEvent e)
        {
          try
          {
            DatagramPacket packet =
              new DatagramPacket(new byte[] {
                  0, 0, 0, 0
              },
                4,
                InetAddress.getByName(getJAddressComboBox().getSelectedItem().toString()),
                deviceDiscoverySocket.getLocalPort());
            deviceDiscoverySocket.send(packet);
          } catch (Exception ex)
          {
          }
        }
      });
    }
    return jDeviceDiscoveryButton;
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getJDeviceMSEARCHButton()
  {
    if (jDeviceMSEARCHButton == null)
    {
      jDeviceMSEARCHButton = new JButton();
      jDeviceMSEARCHButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jDeviceMSEARCHButton.setText("Teste M-SEARCH");
      jDeviceMSEARCHButton.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(java.awt.event.ActionEvent e)
        {
          try
          {
            DatagramPacket packet =
              new DatagramPacket(new byte[] {
                  0, 0, 0, 0
              },
                4,
                InetAddress.getByName(getJAddressComboBox().getSelectedItem().toString()),
                deviceMSearchSocket.getLocalPort());
            deviceMSearchSocket.send(packet);
          } catch (Exception ex)
          {
          }
        }
      });
    }
    return jDeviceMSEARCHButton;
  }

  /**
   * This method initializes jButton2
   * 
   * @return javax.swing.JButton
   */
  private JButton getJDeviceHTTPButton()
  {
    if (jDeviceHTTPButton == null)
    {
      jDeviceHTTPButton = new JButton();
      jDeviceHTTPButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jDeviceHTTPButton.setText("Teste HTTP");
      jDeviceHTTPButton.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(java.awt.event.ActionEvent e)
        {
          try
          {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(getJAddressComboBox().getSelectedItem().toString(),
              InternetManagementConstants.HTTP_DEVICE_REQUEST_PORT), 5000);
            jDeviceHTTPSendCheckBox.setSelected(true);
            socket.close();
          } catch (Exception ex)
          {
            System.out.println("Could not connect to remote device server");
            jDeviceHTTPSendCheckBox.setSelected(false);
          }
        }
      });
    }
    return jDeviceHTTPButton;
  }

  /**
   * This method initializes jButton3
   * 
   * @return javax.swing.JButton
   */
  private JButton getJButton3()
  {
    if (jButton3 == null)
    {
      jButton3 = new JButton();
      jButton3.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jButton3.setText("Teste alles");
    }
    return jButton3;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getJTestAllButton()
  {
    if (jTestAllButton == null)
    {
      jTestAllButton = new JButton();
      jTestAllButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jTestAllButton.setText("Teste alles");
      jTestAllButton.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(java.awt.event.ActionEvent e)
        {
          try
          {
            DatagramPacket packet =
              new DatagramPacket(new byte[] {
                  0, 0, 0, 0
              },
                4,
                InetAddress.getByName(getJAddressComboBox().getSelectedItem().toString()),
                deviceDirectoryMSearchSocket.getLocalPort());
            deviceDirectoryMSearchSocket.send(packet);
          } catch (Exception ex)
          {
          }
          try
          {
            DatagramPacket packet =
              new DatagramPacket(new byte[] {
                  0, 0, 0, 0
              },
                4,
                InetAddress.getByName(getJAddressComboBox().getSelectedItem().toString()),
                deviceDiscoverySocket.getLocalPort());
            deviceDiscoverySocket.send(packet);
          } catch (Exception ex)
          {
          }
          try
          {
            DatagramPacket packet =
              new DatagramPacket(new byte[] {
                  0, 0, 0, 0
              },
                4,
                InetAddress.getByName(getJAddressComboBox().getSelectedItem().toString()),
                deviceMSearchSocket.getLocalPort());
            deviceMSearchSocket.send(packet);
          } catch (Exception ex)
          {
          }
          try
          {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(getJAddressComboBox().getSelectedItem().toString(),
              InternetManagementConstants.HTTP_DEVICE_DIRECTORY_DEVICE_REQUEST_PORT), 5000);
            jGatewayHTTPSendCheckBox.setSelected(true);
            socket.close();
          } catch (Exception ex)
          {
            System.out.println("Could not connect to remote gateway server");
            jGatewayHTTPSendCheckBox.setSelected(false);
          }
          try
          {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(getJAddressComboBox().getSelectedItem().toString(),
              InternetManagementConstants.HTTP_DEVICE_REQUEST_PORT), 5000);
            jDeviceHTTPSendCheckBox.setSelected(true);
            socket.close();
          } catch (Exception ex)
          {
            System.out.println("Could not connect to remote device server");
            jDeviceHTTPSendCheckBox.setSelected(false);
          }
        }
      });
    }
    return jTestAllButton;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getJClearAllButton()
  {
    if (jClearAllButton == null)
    {
      jClearAllButton = new JButton();
      jClearAllButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
      jClearAllButton.setText("L�sche alle Tests");
      jClearAllButton.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(java.awt.event.ActionEvent e)
        {
          jGatewayMSEARCHReceiveCheckBox.setSelected(false);
          jGatewayHTTPReceiveCheckBox.setSelected(false);
          jGatewayHTTPSendCheckBox.setSelected(false);
          jDeviceDiscoveryReceiveCheckBox.setSelected(false);
          jDeviceMSEARCHReceiveCheckBox.setSelected(false);
          jDeviceHTTPReceiveCheckBox.setSelected(false);
          jDeviceHTTPSendCheckBox.setSelected(false);
        }
      });
    }
    return jClearAllButton;
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    new RouterCheck().setVisible(true);
  }

  /**
   * This is the default constructor
   */
  public RouterCheck()
  {
    super();
    initialize();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize()
  {
    this.setSize(400, 330);
    Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation((screenSize.width - getSize().width) / 2, (screenSize.height - getSize().height) / 2);
    this.setContentPane(getJContentPane());
    this.setTitle("RouterCheck");
    this.addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent e)
      {
        System.out.println("Try to terminate application");
        terminate();
        try
        {
          deviceDirectoryMSearchSocket.close();
          deviceDiscoverySocket.close();
          deviceMSearchSocket.close();
          deviceDirectoryHTTPSocket.close();
          deviceHTTPSocket.close();
        } catch (Exception ex)
        {
        }
        System.exit(0);
      }
    });
    try
    {
      deviceDirectoryMSearchSocket =
        new DatagramSocket(InternetManagementConstants.SSDP_DEVICE_DIRECTORY_DEVICE_M_SEARCH_PORT);
      deviceDiscoverySocket = new DatagramSocket(InternetManagementConstants.SSDP_DEVICE_PORT);
      deviceMSearchSocket = new DatagramSocket(InternetManagementConstants.SSDP_DEVICE_M_SEARCH_SEND_PORT);

      deviceDirectoryHTTPSocket =
        new ServerSocket(InternetManagementConstants.HTTP_DEVICE_DIRECTORY_DEVICE_REQUEST_PORT);
      deviceDirectoryHTTPSocket.setSoTimeout(50);
      deviceHTTPSocket = new ServerSocket(InternetManagementConstants.HTTP_DEVICE_REQUEST_PORT);
      deviceHTTPSocket.setSoTimeout(50);

    } catch (Exception e)
    {
      System.out.println("Could not start sockets");
    }
    Vector addresses = new Vector();
    addresses.add("akoenig.dyndns.org");
    addresses.add("pbross.dyndns.org");
    addresses.add("192.168.1.2");
    addresses.add("192.168.1.3");
    addresses.add("192.168.1.4");
    jAddressComboBox.setModel(new DefaultComboBoxModel(addresses));
    jAddressComboBox.setSelectedIndex(0);
    new Thread(this).start();
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane()
  {
    if (jContentPane == null)
    {
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.setSize(new java.awt.Dimension(300, 180));
      jContentPane.setPreferredSize(new java.awt.Dimension(300, 180));
      jContentPane.add(getJBackgroundPanel(), java.awt.BorderLayout.CENTER);
    }
    return jContentPane;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    while (!terminateThread)
    {
      BinaryMessageObject message = SocketHelper.readBinaryMessage(null, deviceDirectoryMSearchSocket, 50);
      if (message != null)
      {
        jGatewayMSEARCHReceiveCheckBox.setSelected(true);
        jStatusLabel.setText("Received message from " + IPHelper.toString(message.getSourceAddress()));
      }
      message = SocketHelper.readBinaryMessage(null, deviceDiscoverySocket, 50);
      if (message != null)
      {
        jDeviceDiscoveryReceiveCheckBox.setSelected(true);
        jStatusLabel.setText("Received message from " + IPHelper.toString(message.getSourceAddress()));
      }
      message = SocketHelper.readBinaryMessage(null, deviceMSearchSocket, 50);
      if (message != null)
      {
        jDeviceMSEARCHReceiveCheckBox.setSelected(true);
        jStatusLabel.setText("Received message from " + IPHelper.toString(message.getSourceAddress()));
      }
      try
      {
        Socket socket = deviceDirectoryHTTPSocket.accept();
        if (socket != null)
        {
          jGatewayHTTPReceiveCheckBox.setSelected(true);
          jStatusLabel.setText("Connection request from " +
            IPHelper.toString((InetSocketAddress)socket.getRemoteSocketAddress()));
          socket.close();
        }
      } catch (Exception e)
      {
      }
      try
      {
        Socket socket = deviceHTTPSocket.accept();
        if (socket != null)
        {
          jDeviceHTTPReceiveCheckBox.setSelected(true);
          jStatusLabel.setText("Connection request from " +
            IPHelper.toString((InetSocketAddress)socket.getRemoteSocketAddress()));
          socket.close();
        }
      } catch (Exception e)
      {
      }
      ThreadHelper.sleep(50);
    }
    terminated = true;
  }

  public void terminate()
  {
    terminateThread = true;
    while (!terminated)
    {
      try
      {
        Thread.sleep(10);
      } catch (Exception ex)
      {
      }
    }
  }

} // @jve:decl-index=0:visual-constraint="207,25"
