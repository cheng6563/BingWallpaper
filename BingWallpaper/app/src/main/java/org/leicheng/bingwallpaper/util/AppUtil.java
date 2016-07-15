package org.leicheng.bingwallpaper.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.leicheng.bingwallpaper.bean.BingImage;
import org.leicheng.bingwallpaper.exception.GetWebPageException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class AppUtil {
	public void getNowWallpaper(final File outFile) throws ClientProtocolException, IOException, IllegalArgumentException, DOMException, SAXException, ParserConfigurationException,
			IllegalAccessException, InvocationTargetException {
		getBingWallpaperFile(getNowBingImages(0).getUrl(), outFile);
	}

	public String getMiddleValue(String tag, String left, String right) {
		try {
			if (tag.indexOf(left) == -1)
				return null;
			tag = tag.substring(tag.indexOf(left) + left.length());
			if (tag.indexOf(right) == -1)
				return null;
			tag = tag.substring(0, tag.indexOf(right));
			return tag;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<BingImage> getNowBingImages() throws ClientProtocolException, IOException, SAXException, ParserConfigurationException, IllegalArgumentException, DOMException, IllegalAccessException,
			InvocationTargetException {
		List<BingImage> nowBingImages = new ArrayList<BingImage>();
		nowBingImages.addAll(getNowBingImages(0, 8));
		nowBingImages.addAll(getNowBingImages(8, 8));
		return nowBingImages;

	}

	public BingImage getNowBingImages(int index) throws ClientProtocolException, IOException, SAXException, ParserConfigurationException, IllegalArgumentException, DOMException,
			IllegalAccessException, InvocationTargetException {
		return getNowBingImages(index, 1).get(0);
	}

	public List<BingImage> getNowBingImages(int index, int number) throws ClientProtocolException, IOException, SAXException, ParserConfigurationException, IllegalArgumentException, DOMException,
			IllegalAccessException, InvocationTargetException {
		String url = "http://www.bing.com/HPImageArchive.aspx?format=xml&idx=" + index + "&n=" + number + "";
		Log.d("url", url);
		List<BingImage> results = new ArrayList<BingImage>();
		InputStream html = HttpUtil.getHtmlInputStream(url);
		Document parse = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(html);
		Node root = parse.getDocumentElement();
		NodeList bingImages = root.getChildNodes();
		for (int i = 0; i < bingImages.getLength(); i++) {
			Node imageNode = bingImages.item(i);
			if (imageNode.getNodeType() != Node.ELEMENT_NODE || !imageNode.getNodeName().equals("image")) {
				continue;
			}
			BingImage bingImageItem = xmlToBingImageEntity(imageNode);

			results.add(bingImageItem);
		}
		return results;
	}

	public BingImage xmlToBingImageEntity(Node imageNode) throws IllegalArgumentException, DOMException, IllegalAccessException, InvocationTargetException {
		BingImage bingImageItem = new BingImage();
		NodeList imageProps = imageNode.getChildNodes();
		// bingImageItem.setXml(imageNode);
		for (int j = 0; j < imageProps.getLength(); j++) {
			Node propNode = imageProps.item(j);
			if (imageNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Class<BingImage> bingImageClass = BingImage.class;
			Field[] fields = bingImageClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase(propNode.getNodeName())) {
					Method set = null;
					try {
						set = bingImageClass.getMethod("set" + firstWordUP(field.getName()), String.class);
						set.invoke(bingImageItem, propNode.getFirstChild().getNodeValue());
					} catch (NoSuchMethodException e) {
						try {
							set = bingImageClass.getMethod("set" + firstWordUP(field.getName()), Integer.class);
							set.invoke(bingImageItem, Integer.parseInt(propNode.getFirstChild().getNodeValue()));
						} catch (NoSuchMethodException e1) {
							try {
								set = bingImageClass.getMethod("set" + firstWordUP(field.getName()), Long.class);
								set.invoke(bingImageItem, Long.parseLong(propNode.getFirstChild().getNodeValue()));
							} catch (NoSuchMethodException e2) {
								e2.printStackTrace();
							}
						}
					}
				}
			}
		}
		return bingImageItem;
	}

	public void getBingWallpaperFile(String url, File file) throws ClientProtocolException, IOException {
		url = "http://cn.bing.com" + url;
		String fhdUrl = url.replace("1366", "1920").replace("768", "1080");
		InputStream htmlInputStream = null;
		try {
			htmlInputStream = HttpUtil.getHtmlInputStream(fhdUrl);
		} catch (GetWebPageException e) {
			htmlInputStream = HttpUtil.getHtmlInputStream(url);
		}
		if (file.exists()) {
			file.delete();
		}
		htmlInputStream = new BufferedInputStream(htmlInputStream);
		OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(file));
		int ch;
		while ((ch = htmlInputStream.read()) >= 0) {
			fileOutputStream.write(ch);
		}
		htmlInputStream.close();
		fileOutputStream.close();
	}

	private String firstWordUP(String a) {
		return a.substring(0, 1).toUpperCase() + a.substring(1);
	}

	public File getCacheFile(Context context) {
		return new File(context.getCacheDir().getPath() + "/now.jpg");
	}

	public void writeImageInfos(Context context, List<?> list) {
		ObjectOutputStream opt = null;
		try {
			opt = new ObjectOutputStream(new FileOutputStream(context.getFilesDir().getPath() + "imageInfo"));
			opt.writeObject(list);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (opt != null)
				try {
					opt.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	@SuppressWarnings("unchecked")
	public List<BingImage> readImageInfos(Context context) {
		ObjectInputStream ipt = null;
		try {
			ipt = new ObjectInputStream(new FileInputStream(context.getFilesDir().getPath() + "imageInfo"));
			return (List<BingImage>) ipt.readObject();
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		} finally {
			if (ipt != null)
				try {
					ipt.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public boolean isWifi(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}
}
