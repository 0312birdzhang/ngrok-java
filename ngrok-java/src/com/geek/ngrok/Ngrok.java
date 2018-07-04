package com.geek.ngrok;


public class Ngrok {

	public void start() {
/*		String connectInfo = new GetNatappAuth().getConfig(Const.authToken, Const.clientToken, Const.token);
		if(null == connectInfo) {
			return;
		}
		String host = connectInfo.split(":")[0];
		int port = Integer.parseInt(connectInfo.split(":")[1]);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//new
		NgrokClient ngclient = new NgrokClient(host, port, Const.authToken, true);*/
		NgrokClient ngclient = new NgrokClient();
		//addtunnel
		ngclient.addTun("127.0.0.1",8080,"http","","",0,"");
		//start
		ngclient.start();
		//check error
		while(true){
			if(ngclient.lasttime+30<(System.currentTimeMillis() / 1000)&&ngclient.lasttime>0){
				Log.print("check err");
				
				ngclient.trfalg=false;
				ngclient.tunnelinfos.clear();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//reconnct
				ngclient.trfalg=true;
				ngclient.start();
				
			}else{
				Log.print("check ok");
			}
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new Ngrok().start();
		System.out.println("Started!");
	}
}
