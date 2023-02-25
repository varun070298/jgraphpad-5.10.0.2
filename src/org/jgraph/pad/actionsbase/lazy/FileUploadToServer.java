/*
 * Copyright (C) 2001-2004 Gaudenz Alder
 *
 * GPGraphpad is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * GPGraphpad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPGraphpad; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.jgraph.pad.actionsbase.lazy;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;

import javax.imageio.ImageIO;

import org.jgraph.JGraph;
import org.jgraph.pad.jgraphpad.GPAction;
import org.microplatform.SessionParameters;
import org.microplatform.SwingWorker;
import org.microplatform.loaders.Translator;

/*
 * Currently implemented to work along with the MoinMoin wiki (instead of
 * TwikiDraw), but could work with other servers/wiki with little work.
 * JGraphpad 3.0 was also known to work with TikiWiki. Hould work with any server supporting
 * HTTP uploads (HTTPS require further work).
 */
public class FileUploadToServer extends GPAction {

	protected boolean exitOnSave = true;

	private static final String NL = "\r\n";

	private static final String NLNL = NL + NL;

	/**
	 * This action is implemented in a non blocking way to avoid possible bad refresh while saving in applet conditions
	 */
	public void actionPerformed(ActionEvent e) {
		String readonly = mdiContainer.getSessionParameters().getParam(SessionParameters.READONLY, false);
		if (readonly.equals("true")) {
			mdiContainer.error(Translator.getString("Error.ReadOnly"));
			return;
		}
		String id = mdiContainer.getSessionParameters().getParam(SessionParameters.ID, false);
		mdiContainer.getPluginInvoker().execJavascript("javascript:uploadCalled('" + id + "')");
		
		if (!e.getActionCommand().equals("sameThread")) {
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					uploadAll();
					return null;
				}
			};
			worker.start();
		} else {// whenever possible, the upload takes place in a swingworker background thread, however, when quitting an applet we can't do that because we could freeze the browser
			mdiContainer.warning("Please wait until the upload complete if you doesn't the data to be lost");
			uploadAll();
			mdiContainer.info("If you didn't receive any error message it means the upload was done properly");
		}
	}
	
	public void uploadAll() {
		if (mdiContainer != null) {
			JGraph graph = getCurrentGraph();
			try {
				// disabled as it was wronly used in the MoinMoin wiki (the map
				// seems correct however):
				// TODO make it work again!
				// uploadMap(graph);

				Object[] selection = graph.getSelectionCells();
				boolean gridVisible = graph.isGridVisible();
				boolean opaque = graph.isOpaque();
				graph.setGridVisible(false);
				graph.clearSelection();
				graph.setOpaque(false);

				BufferedImage image = graph.getImage(graph.getBackground(), 5);
				uploadImage(image);

				graph.setSelectionCells(selection);
				graph.setGridVisible(gridVisible);
				graph.setOpaque(opaque);

				getCurrentGPDocument().setModified(false);
				//since the image is uploaded and as the graph upload will take more time (due to the Sun bad java.beans slow implementation 
				// when used in unsigned applet; beanInfo codebase lookup), we can already  toogle editing view in the HTML page eventually.
				String id = mdiContainer.getSessionParameters().getParam(SessionParameters.ID, false);
				mdiContainer.getPluginInvoker().execJavascript("javascript:uploading('" + id + "')");

				upload(graph);
				mdiContainer.getPluginInvoker().execJavascript("javascript:uploaded('" + id + "')");//everything is done
			} catch (Exception e1) {
				mdiContainer.error(e1.toString());
			}
		}
		if (exitOnSave)
			mdiContainer.getCommand("FileExit").actionPerformed(null);
	}

	// TODO: compress (don't forget to decompress the file also then)
	public void upload(JGraph graph) throws IOException {
		String savePath = mdiContainer.getSessionParameters().getParam(
				SessionParameters.UPLOADPATH, true);
		String baseName = mdiContainer.getSessionParameters().getParam(
				SessionParameters.UPLOADFILE, true);
		String drawingPath = mdiContainer.getSessionParameters().getParam(
				SessionParameters.DOWNLOADPATH, true);
		post(savePath, baseName + ".xml", "text/plain", drawingPath, getCurrentGPDocument().getFile().getFileAsXML(), "JGraphpad file");
	}

	public void uploadImage(BufferedImage image) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", bos);
		bos.flush();
		// This is required for correct conversion
		byte[] aByte = bos.toByteArray();
		int size = aByte.length;
		char[] aChar = new char[size];
		for (int i = 0; i < size; i++) {
			aChar[i] = (char) aByte[i];
		}
		String savePath = mdiContainer.getSessionParameters().getParam(
				SessionParameters.UPLOADPATH, true);
		String baseName = mdiContainer.getSessionParameters().getParam(
				SessionParameters.UPLOADFILE, true);
		String imageUploadPath = mdiContainer.getSessionParameters().getParam(
				SessionParameters.VIEWPATH, true);

		post(savePath, baseName + ".png", "image/png", imageUploadPath, String.valueOf(
				aChar, 0, aChar.length), "JGraphpad png file");
	}

	public void uploadMap(JGraph graph) throws IOException {
		String html = FileExportImageMap.myEncoder.encode(graph, "map");

		String savePath = mdiContainer.getSessionParameters().getParam(
				SessionParameters.UPLOADPATH, true);
		String baseName = mdiContainer.getSessionParameters().getParam(
				SessionParameters.UPLOADFILE, true);
		String drawingPath = mdiContainer.getSessionParameters().getParam(
				SessionParameters.DOWNLOADPATH, true);
		String mapPath = drawingPath.substring(0, drawingPath.length() - 5);

		post(savePath, baseName + ".map", "text/plain", mapPath + ".map", html,
				"JGraphpad map file");
	}

	/**
	 * Submits POST command to the server, and reads the reply.
	 */
	public boolean post(String url, String fileName, String type, String path,
			String content, String comment) throws MalformedURLException,
			IOException {

		String sep = "89692781418184";
		while (content.indexOf(sep) != -1)
			sep += "x";

		String message = makeMimeForm(fileName, type, path, content, comment,
				sep);

		URL server = new URL(mdiContainer.getSessionParameters().getParam(
				SessionParameters.PROTOCOL, false), mdiContainer
				.getSessionParameters().getParam(SessionParameters.HOSTNAME,
						true), Integer.parseInt(mdiContainer.getSessionParameters()
				.getParam(SessionParameters.HOSTPORT, false)), url);
		URLConnection connection = server.openConnection();

		connection.setAllowUserInteraction(false);
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		connection.setRequestProperty("Content-type",
				"multipart/form-data; boundary=" + sep);
		connection.setRequestProperty("Content-length", Integer
				.toString(message.length()));

		System.out.println(url);
		String replyString = null;
		try {
			DataOutputStream out = new DataOutputStream(connection
					.getOutputStream());
			out.writeBytes(message);
			out.close();
			System.out.println("Wrote " + message.length() + " bytes to\n"
					+ connection);

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				String reply = null;
				while ((reply = in.readLine()) != null) {
					if (reply.startsWith("ERROR ")) {
						replyString = reply.substring("ERROR ".length());
					}
				}
				in.close();
			} catch (IOException ioe) {
				mdiContainer.error(ioe.toString());
				replyString = ioe.toString();
			}
		} catch (UnknownServiceException use) {
			replyString = use.getMessage();
			mdiContainer.error(use.toString());
			System.out.println(message);
		}
		if (replyString != null) {
			System.out.println("---- Reply " + replyString);
			if (replyString.startsWith("URL ")) {
				//TODO useful?
			} else if (replyString.startsWith("java.io.FileNotFoundException")) {
				// debug; when run from appletviewer, the http connection
				// is not available so write the file content
				if (path.endsWith(".xml") || path.endsWith(".map"))
					System.out.println(content);
			} else
				// showStatus(replyString);
				return false;
		} else {
			// showStatus(url + " saved");
			return true;
		}
		return true;// TODO sure?
	}

	/**
	 * @return whether or not a reply was received
	 */
	static public boolean post(String protocol, String serverName,
			int portNumber, String url, String fileName, String type,
			String path, String content, String comment)
			throws MalformedURLException, IOException {
		String sep = "89692781418184";
		while (content.indexOf(sep) != -1)
			sep += "x";
		String message = makeMimeFormOld(fileName, type, path, content,
				comment, sep);
		// Ask for parameters
		URL server = new URL(protocol, serverName, portNumber, url);
		URLConnection connection = server.openConnection();
		connection.setAllowUserInteraction(false);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-type",
				"multipart/form-data; boundary=" + sep);
		connection.setRequestProperty("Content-length", Integer
				.toString(message.length()));
		String replyString = null;
		try {
			DataOutputStream out = new DataOutputStream(connection
					.getOutputStream());
			out.writeBytes(message);
			out.close();
			System.out.println("Wrote " + message.length() + " bytes to\n"
					+ connection);
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				String reply = null;
				while ((reply = in.readLine()) != null) {
					if (reply.startsWith("ERROR ")) {
						replyString = reply.substring("ERROR ".length());
					}
				}
				in.close();
			} catch (IOException ioe) {
				replyString = ioe.toString();
				System.out.println(ioe + ": " + connection);
			}
		} catch (UnknownServiceException use) {
			replyString = use.getMessage();
			System.out.println(use);
			System.out.println(message);
		}
		if (replyString != null) {
			return false;
		}
		return true;
	}

	/**
	 * @return the MIME form of the input strings
	 */
	private static String makeMimeFormOld(String fileName, String type,
			String path, String content, String comment, String sep) {
		String encoding = "";
		if (type.equals("image/png") || type.equals("image/jpg")
				|| type.equals("image/png")) {
			encoding = "Content-Transfer-Encoding: binary" + NL;
		}

		String mime_sep = NL + "--" + sep + NL;
		return "--" + sep + "\r\n"
				+ "Content-Disposition: form-data; name=\"filename\"" + NLNL
				+ fileName + mime_sep
				+ "Content-Disposition: form-data; name=\"noredirect\"" + NLNL
				+ 1 + mime_sep
				+ "Content-Disposition: form-data; name=\"filepath\"; "
				+ "filename=\"" + path + "\"" + NL + "Content-Type: " + type
				+ NL + encoding + NL + content + mime_sep
				+ "Content-Disposition: form-data; name=\"filecomment\"" + NLNL
				+ comment + NL + "--" + sep + "--" + NL;
	}

	/** Post the given message */
	private String makeMimeForm(String fileName, String type, String path,
			String content, String comment, String sep) {

		String binary = "";
		if (type.equals("image/png")) {
			binary = "Content-Transfer-Encoding: binary" + NL;
		}

		String mime_sep = NL + "--" + sep + NL;

		return "--" + sep + "\r\n"
				+ "Content-Disposition: form-data; name=\"filename\"" + NLNL
				+ fileName + mime_sep
				+ "Content-Disposition: form-data; name=\"noredirect\"" + NLNL
				+ 1 + mime_sep
				+ "Content-Disposition: form-data; name=\"filepath\"; "
				+ "filename=\"" + path + "\"" + NL + "Content-Type: " + type
				+ NL + binary + NL + content + mime_sep
				+ "Content-Disposition: form-data; name=\"filecomment\"" + NLNL
				+ comment + NL + "--" + sep + "--" + NL;
	}

}
