package hellstrike21291;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class MainWindow extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	private static Socket commandGate;
	private static DatagramSocket dataGate;
	private static DatagramPacket dataBuf;
	private static Semaphore sem;
	private static Point[] p;

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		
		commandGate = new Socket("127.0.0.1", 60000);
		System.out.println("Создано TCP соединение");
		
		dataGate = new DatagramSocket(7000);
		System.out.println("Создан UDP сокет");
		
		dataBuf = new DatagramPacket(new byte[12], 12);
		System.out.println("Создан UDP пакет");
		
		sem = new Semaphore(1);
		System.out.println("Создан семафор");
		
		p = new Point[256];
		System.out.println("Выделено место под 256 точек");
		
		MainWindow guiThread = new MainWindow();
		System.out.println("Создан объект класса MainWindow");
		
		DataOutputStream dos = new DataOutputStream(commandGate.getOutputStream());
		dos.writeInt(7000);
		dos.writeInt(1);
		System.out.println("Отправлены данные о порте и объекте");
		
		System.out.println("=====================================================");
		while(true) {
			System.out.println("Ожидаются данные");
			dataGate.receive(dataBuf);
			System.out.println("Данные получены");
			
			if(dataBuf.getLength() == 12) {
				System.out.println("Получено 12 байт:");
				byte[] data = dataBuf.getData();
				
				int id = Byte.toUnsignedInt(data[3]);
				int x = (data[4] & 255) << 24 | (data[5] & 255) << 16 | (data[6] & 255) << 8 | (data[7] & 255);
				int y = (data[8] & 255) << 24 | (data[9] & 255) << 16 | (data[10] & 255) << 8 | (data[11] & 255);
				System.out.println("\tID: " + id);
				System.out.println("\tX: " + x);
				System.out.println("\tY: " + y);
				
				try {
					System.out.println("Ожидаем семафор");
					sem.acquire();
					System.out.println("Семафор занят");
					if(x < 0 || y < 0) 
						p[id] = null;
					
					else 
						p[id] = new Point(x, y);
					
					System.out.println("Информаци о точка обновлена");
					sem.release();
					System.out.println("Семафор освобожден");
				} catch (InterruptedException e) {
					guiThread.dispose();
					e.printStackTrace();
				}
				
				guiThread.repaint();
			}
			else {
				guiThread.dispose();
			}
		}
	}
	
	public MainWindow() {
		super("АВТ-818 Жигулин Сальков");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBounds(200, 100, 800, 600);
		setResizable(false);
		
		Canvas canvas = new Canvas(p, sem);
		add(canvas);
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				
				try {
					byte[] data = new byte[1];
					
					if(e.getKeyCode() == KeyEvent.VK_UP) {
						data[0] = 1;
						commandGate.getOutputStream().write(data[0]);
					}
					else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
						data[0] = 2;
						commandGate.getOutputStream().write(data[0]);
					}
					else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
						data[0] = 3;
						commandGate.getOutputStream().write(data[0]);
					}
					else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
						data[0] = 4;
						commandGate.getOutputStream().write(data[0]);
					}
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
				System.out.println("Данные посланы");
			}
		});
		
		setVisible(true);
	}
	
	@Override
	public void paintComponents(Graphics g) {
		try {
			System.out.println("Ожидание семафора");
			sem.acquire();
			System.out.println("Семафор получен");
			
			g.setColor(Color.black);
			g.fillRect(0, 0, 800, 600);
			
			g.setColor(Color.green);
			for(int i = 0; i < 256; i++) {
				if(p[i] != null) {
					g.fillRect(p[i].x, p[i].y, 10, 10);
				}
			}
			
			sem.release();
			System.out.println("Семафор освобожден");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.paintComponents(g);
	}
	
	@Override
	public void dispose() {
		System.out.println("Уничтожение!!!!");
		try {
			commandGate.getOutputStream().write(new byte[1]);
			commandGate.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		dataGate.close();
		super.dispose();
		System.exit(0);
	}
}

class Canvas extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private Semaphore sem;
	private Point[] p;
	
	public Canvas(Point[] p, Semaphore sem) {
		this.sem = sem;
		this.p = p;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		try {
			sem.acquire();
			g.setColor(Color.black);
			g.fillRect(0, 0, 800, 600);
			
			g.setColor(Color.green);
			for(int i = 0; i < 256; i++) {
				if(p[i] != null) {
					g.fillRect(p[i].x, p[i].y, 10, 10);
				}
			}
			sem.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		super.paintComponent(g);
	}
	
}
