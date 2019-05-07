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
		// 获取目录directory
		Directory indexDirectory = FSDirectory.open(FileSystems.getDefault().getPath(indexDirectoryPath));

		// 中文分析器
		Analyzer analyzer = new SmartChineseAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(indexDirectory, config);
	}

	public void close() throws CorruptIndexException, IOException{
		writer.close();
	}

	private Document getDocument(File file) throws IOException{
		Document document = new Document();
      
		// 定义一个fieldType，并设置其属性，既保存在文件又用于索引建立
		FieldType fieldType = new FieldType();
		fieldType.setStored(true);
		fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
      
		// 读取 file 转 string
		StringBuffer buffer = new StringBuffer();
		BufferedReader bf= new BufferedReader(new FileReader(file));
		String s = null;
		while((s = bf.readLine())!=null){//使用readLine方法，一次读一行
			buffer.append(s.trim());
		}

		String xml = buffer.toString();
		// 用文件内容来建立倒排索引
		Field contentField = new Field(LuceneConstants.CONTENTS, xml,fieldType);
		// 用文件名来建立倒排索引
		Field fileNameField = new Field(LuceneConstants.FILE_NAME,file.getName(),fieldType);
		// 用文件路径来建立倒排索引
		Field filePathField = new Field(LuceneConstants.FILE_PATH,file.getCanonicalPath(),fieldType);
	  
		// 添加到document
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