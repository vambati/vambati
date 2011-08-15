header {
package TreeParser;
	
import java.util.*;
import java.io.*;
import Utils.*;
}

options { language="Java"; }

class RuleParser extends Parser;
options { 
    k=4; 
    buildAST = false;
 }

{

  PatternNode topnode, tmpnode;
  Stack<PatternNode> nodestack;
  int index = 1;
  int identifier = 1;

  private void initialize() {
  	  if(nodestack == null){
	    nodestack = new Stack<PatternNode>();
  	  }
  	  else{
  	  	nodestack.empty();
  	  }

	  index = 1;
	  identifier = 1;
	  
  	  topnode = new PatternNode();
  	  topnode.setRoot();
  	  topnode.setFrontier();
  	  
      topnode.id = identifier++;
      nodestack.push(this.topnode);
  }
  
  public PatternNode parse_rule() throws Exception{
  	initialize();
  	tree();
  	return topnode;
  }
}

tree
    : ( 
     
      LPAREN  
            ntype:TOKEN 
            { 
                nodestack.peek().nodetype = ntype.getText();                                 
                nodestack.peek().sStart = index;
                //System.out.println(ntype.getText());
            }

        (
			{
				tmpnode = new PatternNode();
				tmpnode.id = identifier++;
				tmpnode.parent = nodestack.peek();
				nodestack.peek().children.add(tmpnode);
				tmpnode.parentIndex = nodestack.peek().children.size()-1;
				nodestack.push(tmpnode);            
			}
	        tree    
	        {
	        	nodestack.pop();
	        }
        |
			leaf:TOKEN 
			{ 
	        			nodestack.peek().setS(leaf.getText());
	        			nodestack.peek().setTerminal();
	        			index++;
	                    //System.out.println("leaf " + leaf.getText()); 
	        }
        )*

      RPAREN 
    )
	{
		nodestack.peek().sEnd = index-1;
	}

;

