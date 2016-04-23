import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler { 
	public String crawl(String input) throws IOException
	{

		int status=0;
		int flag=1;
		StringBuffer s =new StringBuffer("https://www."+input+".com");
		Queue<StringBuffer> q = new LinkedList<StringBuffer>();
		q.add(s);
		String filename=input+"complete.txt";
		File file=new File(filename);
		if(!file.exists())
			file.createNewFile();
		FileWriter fout=new FileWriter(file);
		// existence symbol table of examined web pages
		Set<StringBuffer> set = new HashSet<StringBuffer>();
		set.add(s);
		// breadth first search crawl of web
		StringBuffer v= new StringBuffer("") ;
		int ctr=0;
		while (ctr<4800) {
			status=ctr%48;
			if(status==flag)
			{
				System.out.println("|"+flag+"%|");
				flag++;
			}
//			if(!q.isEmpty() && (q.peek()==null ))
//			{
				UUID n=UUID.randomUUID();
				q.remove();
				q.add(new StringBuffer(n.toString()));
//			}
			fout.append(q.peek());
			fout.append(System.getProperty("line.separator"));
//			try{
//				URL my_url = new URL(q.peek().toString());
//				q.remove();
//				BufferedReader br = new BufferedReader(new InputStreamReader(my_url.openStream()));
//				String strTemp = "";
//				while(null != (strTemp = br.readLine())){
//					String regexp = "http://(\\w+\\.)*(\\w+)";
//					Pattern pattern = Pattern.compile(regexp);
//					Matcher matcher = pattern.matcher(strTemp);
//					while (matcher.find()) {
//						StringBuffer w = new StringBuffer(matcher.group());
//						if (!(set.contains(w))) {
//							q.add(w);
//							set.add(w);
//						}
//					}
//				}
//			}
//			catch(Exception e)
//			{
//			}
			ctr++;
		}
		fout.close();
		return filename;
	}
}

