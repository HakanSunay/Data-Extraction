package ca.pfv.spmf.welwindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.*;

import ca.pfv.spmf.gui.PreferencesManager;

public class Xiazai extends JDialog {
	private static final long serialVersionUID = 1L;

	private JButton jButton1;
	private JProgressBar jProgressBar1;
	private boolean state = false;
	private int count = 0;
	
	public static long byteCountRead = 0;

	private Thread workThead = null;
	MainPlugin mainPlugin;

	public Xiazai(final String path, final MainPlugin mainPlugin) {
		this.mainPlugin = mainPlugin;
		this.setAlwaysOnTop(true);
		this.setModal(true);
		byteCountRead = 0;
		initComponents();
		Thread thread = new Thread() {
			public void run() {
				try {
					 int idx = mainPlugin.jTable1.getSelectedRow();
						//=================================
						// Create the URL: 
						//  http://www.philippe-fournier-viger.com/spmf/plugins/{pluginname}/{pluginname}.jar
						String url1 = PreferencesManager.getInstance().getRepositoryURL() 
								   + Plugins.pluginNames.get(idx) // plugin name
								   + "/"
								   + Plugins.pluginNames.get(idx)
								   + ".jar";
						//=================================
//				       String url1 = Plugins.url1.replace("{pluginname}", Plugins.pluginName[idx][0]);
				    final String requestUrl = path;
					downLoadFromUrl(url1, Plugins.listPlugin.get(idx).getName()+".jar",path);
					mainPlugin.jTable1.setValueAt("Yes", idx, 3);

					mainPlugin.jButton3Remove.setEnabled(true);
					mainPlugin.jButton2Update.setEnabled(true);
					mainPlugin.jButton2Install.setEnabled(false);
				} catch (IOException e) {
					e.printStackTrace();
					state = false;
					jButton1.setText("Cancel");
					 jProgressBar1.setIndeterminate(false);
					 jProgressBar1.setMinimum(jProgressBar1.getMaximum());
					jProgressBar1.setString(byteCountRead+" bytes - download failed");
		 			 setTitle("Download failed");
				}
			}
		};
		thread.start();
	}

	private void initComponents() {

		jProgressBar1 = new JProgressBar();
		 jProgressBar1.setStringPainted(true);
		 jProgressBar1.setIndeterminate(true);
		jButton1 = new JButton();
		setTitle("Downloading plugin...");
		jButton1.setText("Cancel");

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(70, 70, 70)
								.addComponent(jProgressBar1,
										GroupLayout.PREFERRED_SIZE, 288,
										GroupLayout.PREFERRED_SIZE)
								.addContainerGap(71, Short.MAX_VALUE))
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addContainerGap(GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addComponent(jButton1)
								.addGap(28, 28, 28)));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addGap(48, 48, 48)
						.addComponent(jProgressBar1,
								GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED, 50,
								Short.MAX_VALUE).addComponent(jButton1)
						.addContainerGap()));

		jButton1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				state = false;
				setVisible(false);
			}
		});
		pack();
		setLocationRelativeTo(null);
	}

	public void downLoadFromUrl(String urlStr, String fileName, String savePath)
			throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setConnectTimeout(3 * 1000);

		conn.setRequestProperty("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		state = true;
		if (workThead == null) {
			workThead = new WorkThead();
			workThead.start();
		}

		InputStream inputStream = conn.getInputStream();

		byte[] getData = readInputStream(inputStream);

		File saveDir = new File(savePath);
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
		File file = new File(saveDir + File.separator + fileName);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(getData);
		if (fos != null) {
			fos.close();
		}
		if (inputStream != null) {
			inputStream.close();
		}
	}


	
	
	public static byte[] readInputStream(InputStream inputStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
			byteCountRead+=len;
		}
		bos.close();
		return bos.toByteArray();
	}

	class WorkThead extends Thread {
		public void run() {
			while (count < 100) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (state) {
					count++;
					SwingUtilities.invokeLater(new Runnable() {
					
						public void run() {
							jProgressBar1.setValue(count);
							jProgressBar1.setString(byteCountRead+" bytes read");
						}
					});
				}
			}
		

			 jProgressBar1.setIndeterminate(false);
			 jProgressBar1.setMinimum(jProgressBar1.getMaximum());
				jProgressBar1.setString(byteCountRead+" bytes - completed");
			 jButton1.setText("Done");
 			 setTitle("Download completed");
		}
	}

}
