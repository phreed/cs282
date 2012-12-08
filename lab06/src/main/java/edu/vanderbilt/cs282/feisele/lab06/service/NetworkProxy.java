package edu.vanderbilt.cs282.feisele.lab06.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import edu.vanderbilt.cs282.feisele.lab06.annotation.DesignPattern;

import android.net.Uri;

public class NetworkProxy {
	
	@DesignPattern(name = "downloader", namespace = "gof", pattern = "strategy", role = "concrete-strategy")
	static public class JsoupProxy {

		protected Class<Jsoup> jsoupClass = Jsoup.class;

		/**
		 * This method is provided to make the class testable. An alternate
		 * 
		 * @param mainUrl
		 * @return
		 * @throws NoSuchMethodException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 */
		private Connection getConnection(final String mainUrl)
				throws SecurityException, NoSuchMethodException,
				IllegalArgumentException, IllegalAccessException,
				InvocationTargetException {
			final Method method = jsoupClass.getMethod("connect", String.class);
			return (Connection) method.invoke(null, (Object) mainUrl);
		}

		public Connection connect(String mainUrl) {
			try {
				return getConnection(mainUrl);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * A factory to provide the JsoupProxy.
		 * 
		 * @return
		 */
		public static JsoupProxy getInstance() {
			return new JsoupProxy();
		}
	}
	
	@DesignPattern(name = "downloader", namespace = "gof", pattern = "strategy", role = "concrete-strategy")
	static public class UrlProxy {

		protected Class<Uri> uriClass = Uri.class;

		/**
		 * This method is provided to make the class testable. An alternate
		 * 
		 * @param mainUrl
		 * @return
		 * @throws NoSuchMethodException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 * @throws MalformedURLException 
		 */
		private URL getConnection(final String mainUrl)
				throws SecurityException, NoSuchMethodException,
				IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, MalformedURLException {
			return new URL(mainUrl);
		}

		public URL connect(String mainUrl) throws MalformedURLException {
			try {
				return getConnection(mainUrl);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * A factory to provide the JsoupProxy.
		 * 
		 * @return
		 */
		public static UrlProxy getInstance() {
			return new UrlProxy();
		}
	}


}
