import java.util.ArrayList;


public class Algorithm {
	public Node[][] currMaze;
	public Node[] targets;
	public Node currNode;
	
	private ArrayList<Node> toVisit = new ArrayList<Node>();
	
	private Node[] bestRoute = null;
	
	public Algorithm(int mSize){
		currMaze = new Node[mSize][mSize];
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
		currNode = currMaze[0][0];
		currMaze[0][0].makeNoWall(0);
		currMaze[0][0].makeWall(3);
		currMaze[0][0].visited = true;
		currMaze[0][0].needVisit = true;
		currMaze[0][1].needVisit = true;
		toVisit.add(currMaze[0][1]);
		
		targets = new Node[]{currMaze[7][7], currMaze[7][8], currMaze[8][7], currMaze[8][8]};
	}
	
	public boolean verify(){
		for(Node t : targets){
			if(t==currNode)return true;
		}
		return false;
	}
	
	public boolean verifyHome(){
		return currNode==currMaze[0][0];
	}
	
	public int bfsUncertain(){
		Node[] route = bfs(currMaze, currMaze[0][0], targets[0]);
		int unCct = 0;
		for(int i = 0; i<route.length-1; i++){
			Node curr = route[i];
			for(int dir = 0; dir<4; dir++){
				if(curr.nb[dir] == route[i+1]){
					if(curr.nbwall[dir]==0)unCct++;
				}
			}
		}
		if(unCct==0) bestRoute = route;
		return unCct;
	}
	
	public Node nextToMidSquare(){
		int[][] dftarg = dField(currMaze, targets, false);
		int[][] dfme = dField(currMaze, new Node[]{currNode}, true);
		
		int lowestscore = Integer.MAX_VALUE;
		Node lowestnode = null;
		for(int i = 0; i<toVisit.size(); i++){
			Node in = toVisit.get(i);
			int score;
			if(dfme[in.x][in.y]==Integer.MAX_VALUE || dftarg[in.x][in.y]==Integer.MAX_VALUE)score = Integer.MAX_VALUE;
			else score = (dfme[in.x][in.y]+2)*(dftarg[in.x][in.y]); // decreased the weight of closeness to me
			if(score<lowestscore){
				lowestscore = score;
				lowestnode = in;
			}
		}
		Node[] route = bfs(currMaze, currNode, lowestnode);
		return route[1];
	}
	
	public Node nextFinalizeSquare(){
		int[][] dfme = dField(currMaze, new Node[]{currNode}, true);
		ArrayList<Node> uncN = new ArrayList<Node>(1);
		Node[] route = bfs(currMaze, currMaze[0][0], targets[0]);
		for(int i = 0; i<route.length-1; i++){
			Node curr = route[i];
			for(int dir = 0; dir<4; dir++){
				if(curr.nb[dir] == route[i+1]){
					if(curr.nbwall[dir]==0)uncN.add(curr);
				}
			}
		}

		int dtoRoute = Integer.MAX_VALUE;
		Node closestOnRoute = null;
		for(Node n : route){
			if(dfme[n.x][n.y]<dtoRoute){
				dtoRoute = dfme[n.x][n.y];
				closestOnRoute = n;
			}
		}
		
		Node[] route2;
		if(dtoRoute>0){
			route2 = bfs(currMaze, currNode, closestOnRoute);
			return route2[1];
		}else{
			int lowestscore = Integer.MAX_VALUE;
			Node lowestnode = null;
			for(int i = 0; i<uncN.size(); i++){
				Node in = uncN.get(i);
				int score = dfme[in.x][in.y];
				if(score<lowestscore){
					lowestscore = score;
					lowestnode = in;
				}
			}
			route2 = bfs(currMaze, currNode, lowestnode);
			
			int indme=-1;
			for(int i = 0; i<route.length; i++){
				if(route[i]==currNode){
					indme=i;
					break;
				}
			}
			
			int dtoLowest = Integer.MAX_VALUE;
			int indTarg = -1;
			for(int i = 0; i<route.length; i++){
				if(uncN.contains(route[i]) && (Math.abs(i-indme)<dtoLowest)){
					dtoLowest = Math.abs(i-indme);
					indTarg = i;
				}
			}

			if(indTarg < indme)return route[indme-1];
			else return route[indme+1];
		}
	}
	
	public Node nextGoHomeSquare(){
		int nind = -1;
		for(int i = 0; i<bestRoute.length; i++){
			if(bestRoute[i]==currNode)nind = i;
		}
		if(nind==-1){
			int[][] dfme = dField(currMaze, new Node[]{currNode}, true);
			int dtoRoute = Integer.MAX_VALUE;
			Node closestOnRoute = null;
			for(Node n : bestRoute){
				if(dfme[n.x][n.y]<dtoRoute){
					dtoRoute = dfme[n.x][n.y];
					closestOnRoute = n;
				}
			}
			Node[] route = bfs(currMaze, currNode, closestOnRoute);
			return route[1];
		}else{
			return bestRoute[nind-1];
		}
	}
	
	public Node nextGoTargSquare(){
		int nind = -1;
		for(int i = 0; i<bestRoute.length; i++){
			if(bestRoute[i]==currNode)nind = i;
		}
		return bestRoute[nind+1];
	}
	
	public void visitSquare(Node node, boolean wallOnLeft, boolean wallOnRight){
		Node mine = currMaze[node.x][node.y];
		int traveldir;
		for(traveldir = 0; traveldir<4; traveldir++){
			if(currNode.nb[traveldir]==mine)break;
		}
		if(wallOnLeft){
			mine.nb[(traveldir+1)%4].makeWall((traveldir+2)%4);
		}
		if(wallOnRight){
			mine.nb[(traveldir+3)%4].makeWall((traveldir+2)%4);
		}
		currNode = mine;
		if(mine.visited)return;
		mine.visited = true;
		if(toVisit.contains(mine))toVisit.remove(mine);
		for(int i = 0; i<4; i++){
			if(node.nbwall[i]==2){
				mine.makeWall(i);
			}else{
				mine.makeNoWall(i);
				if(!mine.nb[i].needVisit){
					mine.nb[i].needVisit = true;
					toVisit.add(mine.nb[i]);
				}
			}
		}
	}
	
	public static int[][] dField(Node[][] maze, Node[] targets, boolean crossVisited){
		int[][] outF = new int[maze.length][maze.length];
		for(int i = 0; i<outF.length; i++){
			for(int j = 0; j<outF.length; j++){
				outF[i][j] = Integer.MAX_VALUE;
			}
		}
		ArrayList<Node> nei = new ArrayList<Node>(1);
		for(Node targ: targets){
			targ.bfscheck = true;
			outF[targ.x][targ.y] = 0;
		}
		for(Node targ: targets){
			for(int i = 0; i<4; i++){
				if(targ.nb[i]!=null && !targ.nb[i].bfscheck)nei.add(targ.nb[i]);
			}
		}
		while(nei.size()>0){
			Node currn = nei.remove(0);
			currn.bfscheck = true;
			int valueLowest = Integer.MAX_VALUE;
			for(int i = 0; i<4; i++){
				if(currn.nb[i]!=null && !currn.nb[i].bfscheck && (crossVisited || !currn.nb[i].visited)){
					nei.add(currn.nb[i]);
					currn.nb[i].bfscheck = true;
				}
				if(currn.nb[i]!=null){
					if(outF[currn.nb[i].x][currn.nb[i].y]<valueLowest){
						valueLowest = outF[currn.nb[i].x][currn.nb[i].y];
					}
				}
			}
			outF[currn.x][currn.y]=valueLowest==Integer.MAX_VALUE ? valueLowest : valueLowest+1;
		}
		for(int i = 0; i<maze.length; i++){
			for(int j = 0; j<maze.length; j++){
				maze[i][j].bfscheck = false;
			}
		}
		return outF;
	}
	
	public static Node[] bfs(Node[][] maze, Node start, Node end){
		ArrayList<Node> heap = new ArrayList<Node>(1);
		start.bfsdist = 0;
		heap.add(start);
		Node currn = null;
		boolean found = false;
		while(heap.size()>0){
			currn = heap.remove(0);
			if(currn == end){
				found = true;
				break;
			}
			currn.bfscheck = true;
			for(int i = 0; i<4; i++){
				Node nei = currn.nb[i];
				if(nei==null)continue;
				if(!nei.bfscheck){
					nei.bfsprev = currn;
					nei.bfsdist = currn.bfsdist +1;
					nei.bfscheck = true;
					heap.add(nei);
				}
			}
		}
		for(int i = 0; i<maze.length; i++){
			for(int j = 0; j<maze.length; j++){
				maze[i][j].bfscheck = false;
			}
		}
		if(found){
			Node[] ret = new Node[currn.bfsdist+1];
			int counter = 0;
			while(currn!=start){
				ret[ret.length-counter-1] = currn;
				currn = currn.bfsprev;
				counter++;
			}
			ret[0] = currn;
			return ret;
		}
		return null;
	}
}
