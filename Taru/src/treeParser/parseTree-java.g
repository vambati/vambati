header {
package treeParser;
	
import java.util.*;
import java.io.*;
import utils.*;
}

options { language="Java"; }

class ParseTreeParser extends Parser;
options { 
    k=4; 
    buildAST = false;
 }

{

  ParseTreeNode topnode, tmpnode;
  Stack<ParseTreeNode> nodestack;
  int index = 1;
  int identifier = 0;

  public void setTopNode(ParseTreeNode topnode) {
      nodestack = new Stack<ParseTreeNode>();
      this.topnode = topnode;
      topnode.id = identifier++;
      nodestack.push(this.topnode);
  }
}

tree
    : ( 
        
      LPAREN 
            ntype:TOKEN 
            { 
                nodestack.peek().nodetype = MyUtils.convertNodeTypeForTiburon(ntype.getText());                                 
                nodestack.peek().sStart = index;
            }

        (
			{
				tmpnode = new ParseTreeNode();
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
	        			nodestack.peek().sString.add(MyUtils.convertLiteralForTiburon(leaf.getText()));
 	        			nodestack.peek().isTerminal = true;
	        			index++;
	                    //System.out.println("leaf " + leaf.getText()); 
	        }
        )+

      RPAREN 
    )
	{
		ParseTreeNode ptn = nodestack.peek();
		ptn.sEnd = index-1;
		ptn.spanString = ptn.sStart + ":" + ptn.sEnd;
	}

;

class ParseTreeLexer extends Lexer;
options {
    k=3;
    charVocabulary = '\u0001'..'\uffff';
    caseSensitive=false;
    caseSensitiveLiterals=false;
    filter = true;
}

{
	public ParseTreeLexer(){
	}
}

/* ignore comments 
SL_COMMENT
	:	';'
		(~('\n'|'\r'))* ('\n'|'\r'('\n')?)
		{ $setType(Token.SKIP); newline();
        } 
    ;
*/

// Whitespace -- ignored
WS	:	(	' '
		|	'\t'
		|	'\f'
		// handle newlines
		|	(	"\r\n"  // Evil DOS
			|	'\r'    // Macintosh
			|	'\n'    // Unix (the right way)
			)
			{ newline(); }
		)
		{ $setType(Token.SKIP);
        }
	;


LPAREN : '('                              { /*cout << "left paren\n"; */ }   ;
RPAREN : ')'                             { /*cout << "right paren\n"; */  }   ;

/*
ESC :
    '\\'! ('"'|'\\')
    ;

WORD : '"'! (ESC|~('"'|'\\'))* '"'! ;
*/

TOKEN : ~('\r'|'\n'|'\t'|'\f'|' '|'('|')'|'#'|'|') 
            (~('\n'|'\r'|'\t'|' '|'\f'|'('|')'|'#'|'|') )* ;