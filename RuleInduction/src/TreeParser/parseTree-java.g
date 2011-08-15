header {
package TreeParser;
	
import java.util.*;
import java.io.*;
import Utils.MyUtils;
}

options { language="Java"; }

class ParseTreeParser extends Parser;
options { 
    k=4; 
    buildAST = false;
    //generateAmbigWarnings=false;
 }

{
  ParseTreeNode topnode, tmpnode;
  Stack<ParseTreeNode> nodestack;
  int index = 1;
  int identifier = 1;
  int mode = 0; // head-annotated, head-type annotated
  
  public void setTopNode(ParseTreeNode topnode) {
      nodestack = new Stack<ParseTreeNode>();
      this.topnode = topnode;
      topnode.id = identifier++;
      nodestack.push(this.topnode);
  }
}

tree
    :(    
      LPAREN 
            ntype:TOKEN
            { 
                nodestack.peek().nodetype = ntype.getText();                                 
                nodestack.peek().sStart = index;
            }
            /*(
            LBRACKET
	            head:TOKEN 
	            {
	                nodestack.peek().head = head.getText();                                 
	            }
	        SLASH
	            headtype:TOKEN 
	            { 
	                nodestack.peek().head_type = headtype.getText();
	            }
	        RBRACKET 
	        )? */
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
	        			nodestack.peek().sString.add(leaf.getText());
	        			nodestack.peek().isTerminal = true;
	        			index++;
	                    //System.out.println("leaf " + leaf.getText()); 
	        }
        )+
      RPAREN 
    )
	{
		nodestack.peek().sEnd = index-1;
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


LPAREN : '('; 
RPAREN : ')';  

/* Original
TOKEN : ~('\r'|'\n'|'\t'|'\f'|' '|'('|')'|'|')
				(~('\n'|'\r'|'\t'|' '|'\f'|'('|')'|'#'|'|') )* ; */
				
TOKEN : ~('\r'|'\n'|'\t'|'\f'|' '|'('|')')
				(~('\n'|'\r'|'\t'|' '|'\f'|'('|')') )* ;

/* This does not allow [,],/, inside the parse trees . So they have to be made into LRB, RRB, SLASH 

LBRACKET : '[';
RBRACKET : ']';
 SLASH : '/';
	
TOKEN : ~('\r'|'\n'|'\t'|'\f'|' '|'('|')'|'|'|'['|']'|'/')
			(~('\n'|'\r'|'\t'|' '|'\f'|'('|')'|'#'|'|'|'['|']'|'/') )* ;
	
/*	
ESC :
    '\\'! ('"'|'\\')
    ;

WORD : '"'! (ESC|~('"'|'\\'))* '"'! ;
*/

/* ignore comments 
SL_COMMENT
	:	';'
		(~('\n'|'\r'))* ('\n'|'\r'('\n')?)
		{ $setType(Token.SKIP); newline();
        } 
    ;
*/