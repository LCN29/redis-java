package com.can.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * <pre>
 *
 * </pre>
 *
 * @author
 * @date 2021-12-31  11:12
 */
public class Nio {

	public static void main(String[] args) throws IOException {

		//1 创建Selector选择器
		Selector selector = Selector.open();

		//2 创建ServerSocketChannel通道
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

		//3 为channel通道绑定监听端口
		serverSocketChannel.bind(new InetSocketAddress(8000));
		//设置非阻塞模式
		serverSocketChannel.configureBlocking(false);

		//4 把channel通道注册到selector选择器上
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("服务器已经启动成功了");

		for (; ; ) {
			//获取channel数量
			int readChannels = selector.select();

			if (readChannels == 0) {
				continue;
			}

			//获取可用的channel
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			//遍历集合
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()) {

				SelectionKey selectionKey = iterator.next();

				//移除set集合当前selectionKey
				iterator.remove();

				//6 根据就绪状态，调用对应方法实现具体业务操作
				//6.1 如果accept状态
				if(selectionKey.isAcceptable()) {

					//1 接入状态，创建socketChannel
					SocketChannel socketChannel = serverSocketChannel.accept();

					//2 把socketChannel设置非阻塞模式
					socketChannel.configureBlocking(false);

					//3 把channel注册到selector选择器上，监听可读状态
					socketChannel.register(selector,SelectionKey.OP_READ);

					//4 客户端回复信息
					socketChannel.write(Charset.forName("UTF-8").encode("欢迎进入聊天室，请注意隐私安全"));
				}

				//6.2 如果可读状态
				if(selectionKey.isReadable()) {

					//1 从SelectionKey获取到已经就绪的通道
					SocketChannel socketChannel = (SocketChannel)selectionKey.channel();

					//2 创建buffer
					ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

					//3 循环读取客户端消息
					int readLength = socketChannel.read(byteBuffer);
					String message = "1234";
					if(readLength >0) {
						//切换读模式
						byteBuffer.flip();

						//读取内容
						message += Charset.forName("UTF-8").decode(byteBuffer);
						System.out.println(message);
					}
				}
			}
		}
	}
}
