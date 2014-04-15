package com.base.bot.player;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


public class EspnViewer {
	private CloseableHttpClient httpclient = null;
	private static String loginUrl = "https://r.espn.go.com/members/util/loginUser", baseUrl = "http://espn.go.com/";

	public EspnViewer(String username, String password) {
		BasicCookieStore cookieStore = new BasicCookieStore();
		httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore)
				.build();

		logintoESPN(username, password);
	}

	private void logintoESPN(String username, String password) {

		HttpPost httpost = new HttpPost(loginUrl);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("language", "en"));
		nvps.add(new BasicNameValuePair("affiliateName", "espn"));
		nvps.add(new BasicNameValuePair("appRedirect", baseUrl));
		nvps.add(new BasicNameValuePair("parentLocation", baseUrl));
		nvps.add(new BasicNameValuePair("registrationFormId", "espn"));
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));

		httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpost);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getPageContents(String url) {
		String returnString = "";

		HttpGet httpget = new HttpGet(url);

		// Create a custom response handler
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			@Override
			public String handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException(
							"Unexpected response status: " + status);
				}
			}

		};
		try {
			returnString = httpclient.execute(httpget, responseHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnString;
	}

	private String generateUrlParams(List<NameValuePair> nvps) {
		String outString = "?";
		for (NameValuePair nvp : nvps) {
			outString += nvp.getName() + "="
					+ URLEncoder.encode(nvp.getValue()) + "&";
		}
		return outString.substring(0, outString.length() - 1);
	}

	public String makePostWithMatchingGetParams(String url,
			List<NameValuePair> postParams) {
		return makePostWithVaringGetParams(url, postParams, postParams);
	}

	public void closeConnection() {
		try {
			httpclient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String makePostWithVaringGetParams(String url,
			List<NameValuePair> getParams, List<NameValuePair> postParams) {
		url = url + generateUrlParams(getParams);

		String returnString = "";
		HttpPost httpost = new HttpPost(url);

		httpost.setEntity(new UrlEncodedFormEntity(postParams, Consts.UTF_8));
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			@Override
			public String handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				// if (status >= 200 && status < 300) {
				HttpEntity entity = response.getEntity();
				return entity != null ? EntityUtils.toString(entity) : null;
				// } else {
				// throw new ClientProtocolException(
				// "Unexpected response status: " + status);
				// }
			}

		};
		try {
			returnString = httpclient.execute(httpost, responseHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnString;
	}

}