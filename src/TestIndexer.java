// test for indexers
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import java.io.File;


public class TestIndexer {
	String dataDir = "E:/java/eclipsesource/Information_retrieval_system/data/page";
	String indexDir = "E:/java/eclipsesource/Information_retrieval_system/index";
	Indexer indexer;
	
	// create index
	private void createIndex() throws IOException {
		indexer = new Indexer(indexDir);
		int num_of_index = 0;
		long startTime = System.currentTimeMillis();
		num_of_index = indexer.createIndex(dataDir, 
										   new TextFileFilter());
		long endTime = System.currentTimeMillis();
		indexer.close();
		System.out.println(num_of_index + "个文件档被索引，共花费" 
		+ (endTime - startTime) + "ms");
	}
	
	public static void main(String[] args) {
		TestIndexer tester;
		try {
			tester = new TestIndexer();
			tester.createIndex();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
