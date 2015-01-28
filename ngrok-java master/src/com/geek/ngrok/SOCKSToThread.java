package com.geek.ngrok;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SOCKSToThread extends Thread {
	private DataInputStream in; // ������
	private DataOutputStream out; // д����

	public SOCKSToThread(InputStream _in, OutputStream _out) {
		in = new DataInputStream(_in);
		out = new DataOutputStream(_out);

		start();
	}

	public void run() {
		// �߳����к���,ѭ����ȡ��������,�����͸���ؿͻ���
		int readbytes = 0;
		byte buf[] = new byte[1000];
		while (true) { // ѭ��
			try {
				if (readbytes == -1)
					break; // ���������˳�ѭ��
				readbytes = in.read(buf, 0, 1000);
				if (readbytes > 0) {
					out.write(buf, 0, readbytes);
					out.flush();
				}
			} catch (Exception e) {
				break;
			} // �쳣���˳�ѭ��
		}
		//���Զ�����ӹرա���Ҳ�رձ��ص����ӡ����������޳�ʱ����
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}