package com.geek.ngrok;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLSocket;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgOn {
	boolean isrecv = true;
	NgrokClient ngrokcli;

	public MsgOn(NgrokClient ngrokcli) {
		this.ngrokcli = ngrokcli;
	}

	public void jsonunpack(String str, SSLSocket s) {
		JSONObject json;
		try {
			Log.print("recvstr:" + str);
			json = new JSONObject(str);

			String type = json.getString("Type");
			// Auth back
			if (type.equals("AuthResp")) {
				JSONObject Payload = json.getJSONObject("Payload");
				String Error = Payload.getString("Error");
				if (Error.endsWith("")) {
					Log.print("AuthResp .....OK....");
					AuthResp(json, s);
				} else {
					Log.print("AuthResp .....error....");
				}
			}

			if (type.equals("ReqProxy")) {
				ReqProxy(json);
			}

			// ping ack
			if (type.equals("Ping")) {
				Ping(json, s);
			}
			if (type.equals("Pong")) {
				Pong();
			}

			// NewTunnel
			if (type.equals("NewTunnel")) {
				JSONObject Payload = json.getJSONObject("Payload");
				String Error = Payload.getString("Error");
				if (Error.equals("")) {
					NewTunnel(json);
					Log.print("NewTunnel .....OK....");
				} else {
					Log.print("NewTunnel .....error....");
				}
			}

			// StartProxy
			if (type.equals("StartProxy")) {
				StartProxy(json, s);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void AuthResp(JSONObject json, SSLSocket s) {
		try {
			JSONObject Payload = json.getJSONObject("Payload");
			ngrokcli.ClientId = Payload.getString("ClientId");
			// 
			HashMap<String, String> tunelInfo;
			
	        for(int i = 0;i < ngrokcli.tunnels.size(); i ++){
	        	tunelInfo = ngrokcli.tunnels.get(i);
	        	String ReqId =  UUID.randomUUID().toString().toLowerCase().replace("-", "").substring(0, 16);
//	        	String ReqId =  Payload.getString("ReqId");
	        	System.out.println("》》》》》》》》》》》》》Reqid:"+ReqId);
	        	MsgSend.SendReqTunnel(s.getOutputStream(),ReqId, tunelInfo.get("Protocol"),tunelInfo.get("Hostname"),tunelInfo.get("Subdomain"),tunelInfo.get("RemotePort"),tunelInfo.get("HttpAuth"));
	        	HashMap<String, String> tunelInfo1 = new HashMap<String, String>();
	        	tunelInfo1.put("localhost", tunelInfo.get("localhost"));
	        	tunelInfo1.put("localport", tunelInfo.get("localport"));
//	        	System.out.println("++++++++ReqId:+++++"+ReqId);
//	        	System.out.println("++++++++tunnel local:+++++"+tunelInfo.get("localhost"));
	        	ngrokcli.tunnelinfos.put(ReqId, tunelInfo1);
	        	
	        }
			
			
			// start ping thread
			new PingThread(ngrokcli,s).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void ReqProxy(JSONObject json) {
		new ProxyThread(ngrokcli,ngrokcli.ClientId).start();
	}

	public void Ping(JSONObject json, SSLSocket s) {

		try {
			MsgSend.SendPong(s.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Pong() {
		ngrokcli.lasttime=System.currentTimeMillis() / 1000;
	}

	public  void NewTunnel(JSONObject json) {
		try {
			JSONObject Payload = json.getJSONObject("Payload");
			String ReqId = Payload.getString("ReqId");
			if(!Payload.has("LocalAddr")) {
				ngrokcli.tunnelinfos.put(Payload.getString("Url"), ngrokcli.tunnelinfos.get(ReqId));
			}else {
				String proto = Payload.getString("LocalAddr");
				HashMap<String, String> localMap = new HashMap<>();
				localMap.put("localhost",proto.split(":")[0]);
				localMap.put("localport", proto.split(":")[1]);
				ngrokcli.tunnelinfos.put(Payload.getString("Url"), localMap);
				ngrokcli.tunnelinfos.put(Payload.getString("Url"), localMap);
			}
			ngrokcli.tunnelinfos.remove(ReqId); //remove 
			System.out.println("Url:" + Payload.getString("Url")
					+ "  Protocol:" + Payload.getString("Protocol"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void StartProxy(JSONObject json, SSLSocket s) {
		try {
			this.isrecv = false;
			try{
				JSONObject Payload = json.getJSONObject("Payload");
				String Url = Payload.getString("Url");
				System.out.println("StartProxy:"+Url);
				Socket locals = new Socket(ngrokcli.tunnelinfos.get(Url).get("localhost"),Integer.parseInt(ngrokcli.tunnelinfos.get(Url).get("localport")));
//				Socket locals = new Socket("127.0.0.1", 8080);
				new SOCKSToThread(s.getInputStream(),
						locals.getOutputStream());
				new SOCKSToThread(locals.getInputStream(),
						s.getOutputStream());
			}catch (JSONException e) {
				e.printStackTrace();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void unpack(SSLSocket s) {
		byte[] packbuff = new byte[2048];
		int packbufflen = 0;
		byte[] buffer = new byte[1024];
		try {
			InputStream zx = s.getInputStream();
			while (isrecv) {
				int len = zx.read(buffer);
				if (len == -1) {
					break;
				}
				if (len == 0) {
					continue;
				}
				BytesUtil.myaddBytes(packbuff, packbufflen, buffer, len);
				packbufflen = packbufflen + len;

				if (packbufflen > 8) {
					int packlen = (int) BytesUtil
							.bytes2long(BytesUtil.leTobe(
									BytesUtil.cutOutByte(packbuff, 0, 8), 8), 0);
					packlen = packlen + 8;
					if (packbufflen == packlen) {
						jsonunpack(
								new String(BytesUtil.cutOutByte(packbuff, 8,
										packlen - 8)), s);
						packbufflen = 0;
					}

					else if (packbufflen > packlen) {
						jsonunpack(
								new String(BytesUtil.cutOutByte(packbuff, 8,
										packlen - 8)), s);
						packbufflen = packbufflen - packlen;
						BytesUtil.myaddBytes(packbuff, 0, BytesUtil.cutOutByte(
								packbuff, packlen, packbufflen), packbufflen);
					}

				}

			}
		} catch (IOException e) {
			isrecv=false;
			//e.printStackTrace();
			return ;
		}
	}
}
