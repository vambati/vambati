package qa;
 
import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
 
public class IRIndexer {

    public static StandardAnalyzer analyzer = new StandardAnalyzer();
    public static String indexpath = "c:/testindex";
    public IRIndexer(String path){
    	//if(!indexpath.equals(""))
    	{
        	indexpath = path; 	
    	}
    }
	// Index Data 
	public void indexData(String qfile, String afile) throws Exception {
		//Directory index = new RAMDirectory(); // In memory index 
		boolean exists = (new File(indexpath)).exists();
		if(!exists){
			IndexWriter w = new IndexWriter(indexpath, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
			
			BufferedReader qReader = new BufferedReader(new FileReader(qfile));
			BufferedReader aReader = new BufferedReader(new FileReader(afile));
			String q = ""; String a="";
			int i=0;
			while((q = qReader.readLine())!=null)
			{
				a = aReader.readLine(); 
				addDoc(w,q,a);
				i++;
				if(i%10000==0){
					System.err.println(i);
				}
			}
			w.close();
			System.err.println("Indexed "+i);
		}else{
			System.err.println("Index already exists. Using "+indexpath);
		}
	}
	
  private void addDoc(IndexWriter w, String question, String answer) throws IOException {
    Document doc = new Document();
    doc.add(new Field("id", "1", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("q", question, Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("a", answer, Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc);
  }
	
	// Retrieve Data (with scores) 
	public static List<Answer> retrieveAnswers(String querystr) throws Exception{
		Query q = new QueryParser("q", analyzer).parse(querystr);

	    int hitsPerPage = 10;
	    IndexSearcher searcher = new IndexSearcher(indexpath);
	    TopDocCollector collector = new TopDocCollector(hitsPerPage);
	    searcher.search(q, collector);
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    
	    // Display results
	    System.out.println("Found " + hits.length + " hits.");
	    for(int i=0;i<hits.length;++i) {
	      int docId = hits[i].doc;
	      Document d = searcher.doc(docId);
	      System.out.println((i + 1) + ". " + d.get("q"));
	    }

	    // searcher can only be closed when there
	    // is no need to access the documents any more. 
	    searcher.close();
	    return null;
	}
}
