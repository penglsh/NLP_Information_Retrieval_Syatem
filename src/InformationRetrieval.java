import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class InformationRetrieval {
	
	public static void main(String[] args) throws IOException {
		String indexDir = "E:/java/eclipsesource/Information_retrieval_system/index";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
		String str = null; 
        System.out.println("��������Ҫ�����Ĺؼ��ʣ���enter����������:"); 
        try {
			str = br.readLine();
			System.out.println(); 
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        // ƴд���
        String temp = str;
        String[] suggestions = checkWord(str);
        if (suggestions != null && suggestions.length != 0){
            System.out.println("��������ѯ�Ĺؼ�����:"); 
        	for(int i = 0; i < suggestions.length; i++){
		        System.out.println((i+1) + " : " + suggestions[i]); 
			}

            System.out.println("��ѡ������Ľ���ؼ���(���� 1 ~ 5)�������ԭ��(����0)��������:"); 
            str = br.readLine();
			System.out.println(); 
            if (str != "0"){
            	str = suggestions[str.charAt(0) - '1'];
            }
            else{
            	str = temp;
            }
        }
		
		try {
			search(indexDir,str);
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		     e.printStackTrace();
		}
	}
	
	// ƴд���
	public static String[] checkWord(String queryWord){
		//������Ŀ¼
		String spellIndexPath = "E:/java/eclipsesource/Information_retrieval_system/spellIndex";
		//��������Ŀ¼
		String oriIndexPath = "E:/java/eclipsesource/Information_retrieval_system/index";

		//ƴд���
		try {
			//Ŀ¼
			Directory directory = FSDirectory.open((new File(spellIndexPath)).toPath());

			SpellChecker spellChecker = new SpellChecker(directory);

			//���¼���������ʼ������
			IndexReader reader = DirectoryReader.open(FSDirectory.open((new File(oriIndexPath)).toPath()));
			//������������
			Dictionary dictionary = new LuceneDictionary(reader, LuceneConstants.CONTENTS);
			
			IndexWriterConfig config = new IndexWriterConfig(new SmartChineseAnalyzer());
			spellChecker.indexDictionary(dictionary, config, true);
			
			int numSug = 5;
			String[] suggestions = spellChecker.suggestSimilar(queryWord, numSug);

			reader.close();
			spellChecker.close();
			directory.close();
			return suggestions;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	// ����
	public static void search(String indexDir,String q)throws Exception{
		
		//�õ���ȡ�����ļ���·��
		Directory dir = FSDirectory.open(Paths.get(indexDir));
		
		//ͨ��dir�õ���·���µ����е��ļ�
		IndexReader reader = DirectoryReader.open(dir);
		
		//����������ѯ��
		IndexSearcher is = new IndexSearcher(reader);
		
		// ����ΪTF/IDF ����
		ClassicSimilarity sim = new ClassicSimilarity();
		
		is.setSimilarity(sim);
		//ʵ����������
		Analyzer analyzer = new SmartChineseAnalyzer(); 
		
		//������ѯ������
		/**
		 * ��һ��������Ҫ��ѯ���ֶΣ�
		 * �ڶ��������Ƿ�����Analyzer
		 * */
		QueryParser parser=new QueryParser("contents", analyzer);
		
		//���ݴ�������q����
		Query query=parser.parse(q);

		//����������ʼʱ��
		long start=System.currentTimeMillis();
		
		//��ʼ��ѯ
		/**
		 * ��һ��������ͨ���������Ĳ��������ҵõ���query��
		 * �ڶ���������Ҫ����ѯ������
		 * */
		TopDocs hits=is.search(query, 10);
		
		//������������ʱ��
		long end=System.currentTimeMillis();
		
		System.out.println("ƥ�� " + q + " ��������" + (end-start) + "ms"
							+ "��ѯ��" + hits.totalHits + "����¼");
		
		//����hits.scoreDocs���õ�scoreDoc
		/**
		 * ScoreDoc:�÷��ĵ�,���õ��ĵ�
		 * scoreDocs:�������topDocs����ĵ�����
		 * @throws Exception 
		 * */
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			Document doc=is.doc(scoreDoc.doc);
			System.out.println(doc.get(LuceneConstants.FILE_PATH));
		}
		
		//�ر�reader
		reader.close();
	}
}
