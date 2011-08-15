package mosesannotator;

public class Phraselink {
public int srcx;
public int srcy;

public int tgtx;
public int tgty;

Phraselink(String sx,String sy,String tx,String ty){
	this.srcx = Integer.parseInt(sx);
	this.srcy = Integer.parseInt(sy);
	this.tgtx = Integer.parseInt(tx);
	this.tgty = Integer.parseInt(ty);
}
}
