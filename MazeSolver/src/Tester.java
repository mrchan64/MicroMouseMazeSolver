import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

public class Tester {
	
	private static JFrame window;
	private static Path path1;
	private static Path path2;

	static int x_off = 100;
	static int y_off = 100;
	static int x_size = 50;
	static int y_size = 50;
	
	static int wait_step = 50;
	static int wait_between = 500;

	public static void main(String[] args) {
		makeDisplay();
		int counter = 1;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		while(true){
			long seed = (long)(Math.random()*10000000);
			System.out.println("Run "+counter+ " | Seed " + seed + " | Started "+dtf.format(LocalDateTime.now()));
			runFullMaze(seed);
			counter++;
		}
	}
	
	public static void runFullMaze(long seed){
		Node[][] realmaze, algomaze;
		Algorithm algo = new Algorithm(18);
		algomaze = algo.currMaze;
		realmaze = Generator.generateMaze(seed, 18, 5); //2312300, 123890, 2312300, 73498
		clearInterface();
		setMaze(realmaze);
		setAlgo(algomaze, algo);
		path1.setPath(Algorithm.bfs(realmaze, realmaze[0][0], realmaze[8][8]));
		updateGame();
		
		Node currn = algo.currNode;
		
		wait_ms(wait_between);
		
		// this part is finding the center of the maze
		while(!algo.verify()){
			wait_ms(wait_step);
			Node n = algo.nextToMidSquare();
			n = realmaze[n.x][n.y];
			algo.visitSquare(n, checkOnLeft(realmaze, currn, n), checkOnRight(realmaze, currn, n));
			currn = n;
			path2.setPath(Algorithm.bfs(algomaze, algomaze[0][0], algomaze[8][8]));
			updateGame();
		}
		
		wait_ms(wait_between);
		
		// this part is verifying the shortest path;
		while(algo.bfsUncertain()>0){
			wait_ms(wait_step);
			Node n = algo.nextFinalizeSquare();
			n = realmaze[n.x][n.y];
			algo.visitSquare(n, checkOnLeft(realmaze, currn, n), checkOnRight(realmaze, currn, n));
			currn = n;
			path2.setPath(Algorithm.bfs(algomaze, algomaze[0][0], algomaze[8][8]));
			updateGame();
		}
		
		wait_ms(wait_between);
		
		// this part is going home;
		while(!algo.verifyHome()){
			wait_ms(wait_step);
			Node n = algo.nextGoHomeSquare();
			n = realmaze[n.x][n.y];
			algo.visitSquare(n, false, false);
			currn = n;
			updateGame();
		}
		
		wait_ms(wait_between);
		
		// this part is going to target;
		while(!algo.verify()){
			wait_ms(wait_step);
			Node n = algo.nextGoTargSquare();
			n = realmaze[n.x][n.y];
			algo.visitSquare(n, false, false);
			currn = n;
			updateGame();
		}
//		System.out.println("Done!");
	}
	
	public static void makeDisplay(){
		window = new JFrame("Ultimate Tic Tac Toe Evolution");
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(null);
		window.setResizable(false);
		window.getContentPane().setPreferredSize(new Dimension(2500,1500));
		window.pack();
		window.setVisible(true);
	}
	
	public static void setMaze(Node[][] maze){
		Container manage = window.getContentPane();
		JLayeredPane jlp = new JLayeredPane();
		jlp.setBounds(0, 0, 2500, 1600);
		for(int i = 0; i<maze.length; i++){
			for(int j = 0; j<maze.length; j++){
				jlp.add(new Square(x_off+x_size*i, y_off+y_size*(maze.length-j-1), x_size, y_size, maze[i][j]), new Integer(1));
			}
		}
		path1 = new Path(x_off, y_off, x_size, y_size);
		jlp.add(path1, new Integer(2));
		manage.add(jlp);
		
		window.pack();
		window.repaint();
	}
	
	public static void setAlgo(Node[][] maze, Algorithm algo){
		Container manage = window.getContentPane();
		JLayeredPane jlp = new JLayeredPane();
		jlp.setBounds(0, 0, 2500, 1600);
		for(int i = 0; i<maze.length; i++){
			for(int j = 0; j<maze.length; j++){
				jlp.add(new Square(x_off*2+x_size*maze.length+x_size*i, y_off+y_size*(maze.length-j-1), x_size, y_size, maze[i][j]), new Integer(1));
			}
		}
		path2 = new Path(x_off*2+x_size*maze.length, y_off, x_size, y_size);
		Curr c = new Curr(x_off*2+x_size*maze.length, y_off, x_size, y_size, algo);
		jlp.add(path2, new Integer(2));
		jlp.add(c, new Integer(3));
		manage.add(jlp);
		
		window.pack();
		window.repaint();
	}
	
	public static void updateGame(){
		window.repaint();
	}
	
	public static void clearInterface(){
		window.getContentPane().removeAll();
	}
	
	public static boolean checkOnLeft(Node[][] maze, Node start, Node end){
		int traveldir;
		for(traveldir = 0; traveldir<4; traveldir++){
			if(start.nb[traveldir]==end)break;
		}
		if(end.nb[(traveldir+1)%4]==null || start.nb[(traveldir+1)%4]==null)return false;
		return end.nb[(traveldir+1)%4].nbwall[(traveldir+2)%4]==2;
	}
	
	public static boolean checkOnRight(Node[][] maze, Node start, Node end){
		int traveldir;
		for(traveldir = 0; traveldir<4; traveldir++){
			if(start.nb[traveldir]==end)break;
		}
		if(end.nb[(traveldir+3)%4]==null || start.nb[(traveldir+3)%4]==null)return false;
		return end.nb[(traveldir+3)%4].nbwall[(traveldir+2)%4]==2;
	}
	
	public static void wait_ms(long timems){
		try {
			Thread.sleep(timems);
		} catch (InterruptedException e) {
			System.out.println("Can't sleep?");
		}
//		long time = System.currentTimeMillis();
//		while(System.currentTimeMillis()-timems<time){
//			int[][] i = new int[1000][1000];
//		}
	}
	
	public static class Square extends JPanel{
		
		int x, y, x_size, y_size;
		Node n;
		
		public Square(int x, int y, int x_size, int y_size, Node n){
			this.x = x;
			this.y = y;
			this.x_size = x_size;
			this.y_size = y_size;
			this.n = n;
			this.setBounds(x,y,x_size,y_size);
			this.setLayout(null);
			this.setOpaque(false);
		}
		
		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.BLUE);
			g2.fillRect(0, 0, x_size, y_size);
			g2.setStroke(new BasicStroke(10));
			g2.setColor(Color.GREEN);
			if(n.nbwall[0]==0)g2.drawLine(0, 0, x_size, 0);
			if(n.nbwall[1]==0)g2.drawLine(0, 0, 0, y_size);
			if(n.nbwall[2]==0)g2.drawLine(0, y_size, x_size, y_size);
			if(n.nbwall[3]==0)g2.drawLine(x_size, 0, x_size, y_size);
			g2.setColor(Color.RED);
			if(n.nbwall[0]==2)g2.drawLine(0, 0, x_size, 0);
			if(n.nbwall[1]==2)g2.drawLine(0, 0, 0, y_size);
			if(n.nbwall[2]==2)g2.drawLine(0, y_size, x_size, y_size);
			if(n.nbwall[3]==2)g2.drawLine(x_size, 0, x_size, y_size);
			g2.setColor(Color.WHITE);
			if(n.visited)g2.drawOval(x_size/2-x_size/16, y_size/2-y_size/16, x_size/8, y_size/8);
		}
		
	}
	
	public static class Path extends JLabel{
		int x, y, x_size, y_size;
		Node[] n;
		
		public Path(int x, int y, int x_size, int y_size){
			this.x = x;
			this.y = y;
			this.x_size = x_size;
			this.y_size = y_size;
			n = null;
			this.setBounds(x,y,x_size*18,y_size*18);
			this.setLayout(null);
			this.setOpaque(false);
		}
		
		public void setPath(Node[] n){
			this.n = n;
		}
		
		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(5));
			g2.setColor(Color.ORANGE);
			if(n == null)return;
			for(int i = 0; i<n.length-1; i++){
				Node currn = n[i];
				Node nextn = n[i+1];
				int x1 = currn.x*x_size + x_size/2;
				int y1 = (18-currn.y)*y_size - y_size/2;
				int x2 = nextn.x*x_size + x_size/2;
				int y2 = (18-nextn.y)*y_size - y_size/2;
				g2.drawLine(x1, y1, x2, y2);
			}
		}
	}
	
	public static class Curr extends JLabel{
		int x, y, x_size, y_size;
		Algorithm algo;
		
		public Curr(int x, int y, int x_size, int y_size, Algorithm algo){
			this.x = x;
			this.y = y;
			this.x_size = x_size;
			this.y_size = y_size;
			this.algo = algo;
			this.setBounds(x,y,x_size*18,y_size*18);
			this.setLayout(null);
			this.setOpaque(false);
		}
		
		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(5));
			g2.setColor(Color.MAGENTA);
			int x1 = algo.currNode.x*x_size+x_size/2-x_size/4;
			int y1 = (18-algo.currNode.y)*y_size-y_size/2-y_size/4;
			int x2 = x_size/2;
			int y2 = y_size/2;
			g2.drawRect(x1, y1, x2, y2);
		}
	}
	

}
