import java.util.ArrayList;
import java.util.Random;


public class Generator {
	
	static Random gen;
	
	public static Node[][] generateMaze(long seed, int mSize, int loops){
		gen = new Random(seed);
		Node[][] currMaze = new Node[mSize][mSize];
		for(int i = 0; i<mSize; i++){
			for(int j = 0; j<mSize; j++){
				currMaze[i][j] = new Node(i,j);
			}
		}
		// connect neighbors
		for(int i = 0; i<mSize; i++){
			for(int j = 0; j<mSize-1; j++){
				//top
				currMaze[i][j].nb[0] = currMaze[i][j+1];
				//left
				currMaze[j+1][i].nb[1] = currMaze[j][i];
				//bottom
				currMaze[i][j+1].nb[2] = currMaze[i][j];
				//right
				currMaze[j][i].nb[3] = currMaze[j+1][i];
			}
		}
		//fill out curr walls
		for(int i = 0; i<mSize; i++){
			// top
			currMaze[i][mSize-1].nbwall[0] = 2;
			// left
			currMaze[0][i].nbwall[1] = 2;
			// bottom
			currMaze[i][0].nbwall[2] = 2;
			// right
			currMaze[mSize-1][i].nbwall[3] = 2;
		}
		currMaze[0][0].makeWall(3);
		currMaze[0][0].makeNoWall(0);
		
		int mazeOp = gen.nextInt(8);
		int counter = 0;
		for(int i = 7; i<9; i++){
			for(int j = 7; j<9; j++){
				if(counter!=mazeOp){
					if(i==7){
						currMaze[i][j].makeWall(1);
						currMaze[i][j].makeNoWall(3);
					}else{
						currMaze[i][j].makeWall(3);
						currMaze[i][j].makeNoWall(1);
					}
				}else{
					if(i==7){
						currMaze[i][j].makeNoWall(1);
					}else{
						currMaze[i][j].makeNoWall(3);
					}
				}
				counter++;
				if(counter!=mazeOp){
					if(j==7){
						currMaze[i][j].makeWall(2);
						currMaze[i][j].makeNoWall(0);
					}else{
						currMaze[i][j].makeWall(0);
						currMaze[i][j].makeNoWall(2);
					}
				}else{
					if(j==7){
						currMaze[i][j].makeNoWall(2);
					}else{
						currMaze[i][j].makeNoWall(0);
					}
				}
				counter++;
			}
		}
		
		Node[] route = null;
		ArrayList<ConnGroup> djgraphs = new ArrayList<ConnGroup>(1);
		// populate neigh
		for(int i = 0; i<mSize; i++){
			for(int j = 0; j<mSize; j++){
				if((i==7 || i==8) && (j==7 || j==8))continue;
				if(i==0 && j==0)continue;
				djgraphs.add(new ConnGroup(currMaze[i][j]));
			}
		}
		// make walls
		for(int i = 0; i<mSize; i++){
			for(int j = 0; j<mSize; j++){
				if((i==7 || i==8) && (j==7 || j==8))continue;
				if(i==0 && j==0)continue;
				//top
				if(!(j == mSize-1 || (i==7||i==8) && j==6)){
					currMaze[i][j].makeWall(0);
				}
				//right
				if(!(i == mSize-1 || (j==7||j==8) && i==6)){
					currMaze[i][j].makeWall(3);
				}
			}
		}
		
		while(djgraphs.size()>1){
			int g1 = gen.nextInt(djgraphs.size());
			ConnGroup cg1 = djgraphs.get(g1);
			ArrayList<ConnGroup> adj = new ArrayList<ConnGroup>(1);
			for(ConnGroup c : djgraphs){
				for(Node nei : cg1.nei){
					if(c.mem.contains(nei))adj.add(c);
				}
			}
			ConnGroup cg2 = adj.get(gen.nextInt(adj.size()));
			if(cg1.combGroup(cg2))djgraphs.remove(cg2);
		}
		
		while(loops > 0){
			int x = gen.nextInt(mSize);
			int y = gen.nextInt(mSize);
			int dir = gen.nextInt(4);
			Node n1 = currMaze[x][y];
			if(n1.nb[dir]!=null)continue;
			if(x==0 && dir==1)continue;
			if(x==15 && dir==3)continue;
			if(y==0 && dir==2)continue;
			if(y==15 && dir==0)continue;
			Node n2 = null;
			switch(dir){
			case 0:
				n2 = currMaze[x][y+1];
				break;
			case 1:
				n2 = currMaze[x-1][y];
				break;
			case 2:
				n2 = currMaze[x][y-1];
				break;
			case 3:
				n2 = currMaze[x+1][y];
				break;
			}
			if((n1.x==7 || n1.x==8) && (n1.y==7 || n1.y==8) || (n2.x==7 || n2.x==8) && (n2.y==7 || n2.y==8))continue;
			if((n1.x==0 && n1.y==0) || (n2.x==0 && n2.y==0))continue;
			boolean lc = n1.nb[(dir+1)%4]==null || n2.nb[(dir+1)%4]==null || n1.nb[(dir+1)%4].nb[dir]==null;
			boolean rc = n1.nb[(dir+3)%4]==null || n2.nb[(dir+3)%4]==null || n1.nb[(dir+3)%4].nb[dir]==null;
			if(lc && rc){
				reconnect(n1,n2,1);
				loops--;
			}
		}
		
		return currMaze;
	}
	
	public static void reconnect(Node n1, Node n2, int state){
		// 0 is unknown, 1 is no wall, 2 is wall

		//n1 on top of n2
		if(n1.x==n2.x && n1.y==n2.y+1){
			n1.nb[2] = n2;
			n1.nbwall[2] = state;
			n2.nb[0] = n1;
			n2.nbwall[0] = state;
		}
		//n1 on left of n2
		if(n1.x==n2.x-1 && n1.y==n2.y){
			n1.nb[3] = n2;
			n1.nbwall[3] = state;
			n2.nb[1] = n1;
			n2.nbwall[1] = state;
		}
		//n1 on bottom of n2
		if(n1.x==n2.x && n1.y==n2.y-1){
			n1.nb[0] = n2;
			n1.nbwall[0] = state;
			n2.nb[2] = n1;
			n2.nbwall[2] = state;
		}
		//n1 on right of n2
		if(n1.x==n2.x+1 && n1.y==n2.y){
			n1.nb[1] = n2;
			n1.nbwall[1] = state;
			n2.nb[3] = n1;
			n2.nbwall[3] = state;
		}
	}
	
	public static class ConnGroup{
		
		ArrayList<Node> mem;
		ArrayList<Node> nei;
		
		public ConnGroup(Node n){
			mem = new ArrayList<Node>(1);
			nei = new ArrayList<Node>(1);
			mem.add(n);
			for(int i = 0; i<4; i++){
				if(n.nb[i]!=null)nei.add(n.nb[i]);
			}
		}
		
		public boolean combGroup(ConnGroup cg){
			ArrayList<Node> overlapIn = new ArrayList<Node>(1);
			for(Node n : nei){
				if(cg.mem.contains(n))overlapIn.add(n);
			}
			if(overlapIn.size()==0)return false;
			Node toConn = overlapIn.get(gen.nextInt(overlapIn.size()));
			int dirStart = gen.nextInt(4);
			for(int i = 0; i<4; i++ ){
				int dir = (dirStart+i)%4;
				Node pot = null;
				for(Node w : mem){
					switch(dir){
					case 0:
						if(w.x==toConn.x && w.y==toConn.y+1)pot = w;
						break;
					case 1:
						if(w.x==toConn.x-1 && w.y==toConn.y)pot = w;
						break;
					case 2:
						if(w.x==toConn.x && w.y==toConn.y-1)pot = w;
						break;
					case 3:
						if(w.x==toConn.x+1 && w.y==toConn.y)pot = w;
						break;
					}
					if(pot!=null)break;
				}
				if(pot==null)continue;
				if(mem.contains(pot)){
					reconnect(pot, toConn, 0);
					toConn.makeNoWall(dir);
					break;
				}
			}
			mem.addAll(cg.mem);
			nei.addAll(cg.nei);
			for(int i = 0; i<nei.size(); i++){
				if(mem.contains(nei.get(i))){
					nei.remove(i);
					i--;
				}
			}
			return true;
		}
		
	}
	
}
