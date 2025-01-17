package org.zeromq.devices;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.devices.ZMQQueue;

public class ZMQQueueTest {

	@Test
	public void testQueue() {
		Context context = ZMQ.context(1);

		ZMQ.Socket clients = context.socket(ZMQ.ROUTER);
		clients.bind("inproc://gate_clients");

		ZMQ.Socket workers = context.socket(ZMQ.DEALER);
		workers.bind("inproc://gate_workers");

		ZMQ.Socket client = context.socket(ZMQ.REQ);
		client.connect("inproc://gate_clients");

		ZMQ.Socket worker = context.socket(ZMQ.REP);
		worker.connect("inproc://gate_workers");

		Thread t = new Thread(new ZMQQueue(context, clients, workers));
		t.start();

		for (int i = 0; i < 10; i++) {
			byte[] req = ("request" + i).getBytes();
			byte[] rsp = ("response" + i).getBytes();

			client.send(req, 0);

			// worker receives request
			byte[] reqTmp = worker.recv(0);

			assertArrayEquals(req, reqTmp);

			// worker sends response
			worker.send(rsp, 0);

			// client receives response
			byte[] rspTmp = client.recv(0);

			assertArrayEquals(rsp, rspTmp);
		}

		t.interrupt();
	}
}
