package com.geek.ngrok;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * @author debo.zhang
 *
 */
public class GetNatappAuth {

	/**
	 * 获取连接信息
	 * @author debo.zhang
	 * @param authToken
	 * @param clientToken
	 * @param token
	 * @return
	 */
	public String getConfig(String authToken, String clientToken, String token) {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };
		StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("{");
        sBuffer.append("\"Authtoken\":").append("\"").append(authToken).append("\",");
        sBuffer.append("\"Clienttoken\":").append("\"").append(clientToken).append("\",");
        sBuffer.append("\"Token\":").append("\"").append(token).append("\"");
        sBuffer.append("}");
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
		    ctx.init(null, trustAllCerts, null);
			 LayeredConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(ctx);
			    CloseableHttpClient client = HttpClients.custom()
			            .setSSLSocketFactory(sslSocketFactory)
			            .build();
			    HttpPost post = new HttpPost(Const.authUrl);
			    post.setEntity(new StringEntity(sBuffer.toString()));
			    CloseableHttpResponse response = null;
				response = client.execute(post);
				int code = response.getStatusLine().getStatusCode();
				String reponMessage = EntityUtils.toString(response.getEntity());
				System.out.println(reponMessage);
				if(code == 200 ) {
					// 解析返回的json
		            JSONObject retJson = new JSONObject(reponMessage);
		            if(!retJson.getBoolean("Success")) {
		            	System.out.println(String.format("认证错误:%s, ErrorCode:%s", retJson.getString("Msg"), retJson.getString("ErrorCode")));
		            	return null;
		            }
//		            proto = authData['Data']['ServerAddr'].split(':')
		            JSONObject dataObj = retJson.getJSONObject("Data");
		            String proto = dataObj.getString("ServerAddr");
		            return proto;
				}else {
					return null;
				}
		} catch (Exception e) {
			System.out.println("客户端异常:" + e.getMessage()); 
		} 
		
		return null;
	}
}
