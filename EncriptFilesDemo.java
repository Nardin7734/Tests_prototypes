package com.dn.howto;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class EncriptFilesDemo extends JPanel implements ActionListener
{
	static private final String newline = "\n";
	private JButton select, decrypt, encript;
	private JTextArea log;
	private JFileChooser fc;
	private DESKeySpec desKeySpec;
	private SecretKeyFactory skf;
	private SecretKey secretKey;
	private Cipher cipher;
	private FileInputStream fis;
	private FileOutputStream fos;
	private final String key = "12345678";
	private File inputFile;
	
	public EncriptFilesDemo() 
	{
		super( new BorderLayout() );
		log = new JTextArea( 5, 20 );
		log.setMargin( new Insets( 5, 5, 5, 5 ));
		log.setEditable( false );
		JScrollPane logScrollPane = new JScrollPane(log);
		fc = new JFileChooser();
		fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
		select = new JButton( "Select a File..." );
		select.addActionListener( this );
		
		JPanel choosePanel = new JPanel();
		choosePanel.add( select );
		
		decrypt = new JButton( "Decrypt" );
		decrypt.setEnabled( false );
		decrypt.addActionListener( this );
		encript = new JButton( "Encript" );
		encript.setEnabled( false );
		encript.addActionListener( this );
		JPanel executePanel = new JPanel();
		executePanel.add( encript );
		executePanel.add( decrypt );
		
		add( choosePanel, BorderLayout.PAGE_START );
		add( executePanel, BorderLayout.PAGE_END );
		add( logScrollPane, BorderLayout.CENTER );
        
	}
	
	private  void encriptDecrypt( File file, int mode )
	{
		try
		{
			desKeySpec = new DESKeySpec( key.getBytes() );
			skf = SecretKeyFactory.getInstance( "DES" );
			secretKey = skf.generateSecret( desKeySpec );
			fis = new FileInputStream( file );
			String name = noExtensionName(file.getName() );
			String extension = extension( file.getName() );
			cipher = Cipher.getInstance( "DES/ECB/PKCS5Padding" );
			
			if( mode == Cipher.DECRYPT_MODE )
			{
				fos = new FileOutputStream( new File( file.getParent() + "\\" + name + "_decrypted" + extension ) );
				cipher.init( cipher.DECRYPT_MODE,  secretKey, SecureRandom.getInstance( "SHA1PRNG" ) );
				CipherOutputStream cos = new CipherOutputStream( fos, cipher );
				write(fis, cos);
			}
			else if( mode == Cipher.ENCRYPT_MODE )
			{
				fos = new FileOutputStream( new File( file.getParent() + "\\" + name + "_encripted" + extension ) );
				cipher.init( cipher.ENCRYPT_MODE,  secretKey, SecureRandom.getInstance( "SHA1PRNG" ) );
				CipherInputStream cis = new CipherInputStream( fis, cipher);
				write(cis, fos);
			}
		}
		catch( InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | 
				NoSuchPaddingException | IOException e )
		{
			e.printStackTrace();
		}
	}
	
	private String noExtensionName( String fullName )
	{
		int extSeparatorIndex = fullName.lastIndexOf( "." );
		return fullName.substring( 0, extSeparatorIndex );
	}
	
	private String extension( String fullName )
	{
		int extSeparatorIndex = fullName.lastIndexOf( "." );
		return fullName.substring( extSeparatorIndex, fullName.length() );
	}


	
	private void write( InputStream in, OutputStream out ) throws IOException
	{
		byte[] buffer = new byte[64];
		int numOfBytesRead;
		
		while( ( numOfBytesRead = in.read( buffer ) ) != -1 )
		{
			out.write( buffer, 0, numOfBytesRead );
		}
		out.close();
		in.close();
	}
	
	private static void createAndShowGUI()
	{
		JFrame frame = new JFrame( "Encript/Decrypt Demo" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.add( new EncriptFilesDemo() );
        frame.pack();
        frame.setVisible( true );
	}
	
	private void validateSelection( File inputFile )
	{
		if( inputFile != null )
		{
			encript.setEnabled( true );
			decrypt.setEnabled( true );
		}
		else
		{
			encript.setEnabled( false );
			decrypt.setEnabled( false );
		}
	}
	
	@Override
	public void actionPerformed( ActionEvent e ) 
	{
		if( e.getSource() == select )
		{
			
			int returnVal = fc.showOpenDialog( EncriptFilesDemo.this );
			if( returnVal == JFileChooser.APPROVE_OPTION )
			{
				log.setText( null );
				inputFile = fc.getSelectedFile();
				log.append( "Arquivo: " + inputFile.getName() + "." + newline );
				
				validateSelection( inputFile );
				
			}
			else
			{
				log.setCaretPosition( log.getDocument().getLength() );
			}
		}
		else if( e.getSource() == encript )
		{
			encriptDecrypt( inputFile, Cipher.ENCRYPT_MODE );
			log.append( "Encrypted file." + newline );
			validateSelection( null );
			
		}
		else if( e.getSource() == decrypt )
		{
			encriptDecrypt( inputFile, Cipher.DECRYPT_MODE );
			log.append( "Decrypted file." + newline );
			validateSelection( null );
		}
		
	}
	
	public static void main( String[] args ) 
	{
		SwingUtilities.invokeLater( new Runnable() {
            public void run() {
				createAndShowGUI();
            }
		});
	}
	
}
