
public class Node {
	
	public int x;
	public int y;
	public boolean visited;
	public boolean needVisit;
	public boolean bfscheck;
	public Node bfsprev;
	public int bfsdist;
	
	public Node[] nb;			//0 top, 1 left, 2 bottom, 3 right (counterclock)
	public int[] nbwall;		// 0 is unknown, 1 is known no wall, 2 is known wall
	
	public Node(int x, int y){
		this.x = x;
		this.y = y;
		visited = false;
		needVisit = false;
		bfscheck = false;
		bfsprev = null;
		nb = new Node[4];
		nbwall = new int[4];
		for(int i = 0; i<4; i++){
			nb[i] = null;
		}
	}
	
	public void makeWall(int dir){
		if(this.nb[dir]==null)return;
		this.nb[dir].nb[(dir+2)%4] = null;
		this.nb[dir].nbwall[(dir+2)%4] = 2;
		this.nb[dir] = null;
		this.nbwall[dir] = 2;
	}
	
	public void makeNoWall(int dir){
		if(this.nb[dir]==null)return;
		this.nb[dir].nbwall[(dir+2)%4] = 1;
		this.nbwall[dir] = 1;
	}
	
}
