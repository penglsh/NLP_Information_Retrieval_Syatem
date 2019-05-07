// https://blog.csdn.net/dickdick111/article/details/89639264
// https://github.com/dick20/NLP/tree/master/lecture10--%E5%88%A9%E7%94%A8Lucene%E6%9E%84%E5%BB%BA%E4%BF%A1%E6%81%AF%E6%A3%80%E7%B4%A2%E7%B3%BB%E7%BB%9F/information-retrieval-system
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
	private IndexWriter writer;
	public Indexer(String indexDirectoryPath) throws IOException{
		// ��ȡĿ¼directory
		Directory indexDirectory = FSDirectory.open(FileSystems.getDefault().getPath(indexDirectoryPath));

		// ���ķ�����
		Analyzer analyzer = new SmartChineseAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(indexDirectory, config);
	}

	public void close() throws CorruptIndexException, IOException{
		writer.close();
	}

	private Document getDocument(File file) throws IOException{
		Document document = new Document();
      
		// ����һ��fieldType�������������ԣ��ȱ������ļ���������������
		FieldType fieldType = new FieldType();
		fieldType.setStored(true);
		fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
      
		// ��ȡ file ת string
		StringBuffer buffer = new StringBuffer();
		BufferedReader bf= new BufferedReader(new FileReader(file));
		String s = null;
		while((s = bf.readLine())!=null){//ʹ��readLine������һ�ζ�һ��
			buffer.append(s.trim());
		}

		String xml = buffer.toString();
		// ���ļ�������������������
		Field contentField = new Field(LuceneConstants.CONTENTS, xml,fieldType);
		// ���ļ�����������������
		Field fileNameField = new Field(LuceneConstants.FILE_NAME,file.getName(),fieldType);
		// ���ļ�·����������������
		Field filePathField = new Field(LuceneConstants.FILE_PATH,file.getCanonicalPath(),fieldType);
	  
		// ��ӵ�document
		document.add(contentField);
		document.add(fileNameField);
		document.add(filePathField);

		return document;
	}   

	private void indexFile(File file) throws IOException{
		System.out.println("Indexing "+file.getCanonicalPath());
		Document document = getDocument(file);
		writer.addDocument(document);
	}
   
    
	public int createIndex(String dataDirPath, FileFilter filter) 
			throws IOException{
		//get all files in the data directory
		File[] files = new File(dataDirPath).listFiles();
      
		int count = 0;
		for (File file : files) {
			// System.out.println(file);
			if(!file.isDirectory()
					&& !file.isHidden()
					&& file.exists()
					&& file.canRead()
					&& filter.accept(file)
			){
				indexFile(file);
				count++;
			}
		}
		return count;
	}
}