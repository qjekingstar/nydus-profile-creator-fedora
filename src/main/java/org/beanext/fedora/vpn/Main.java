package org.beanext.fedora.vpn;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.beanext.util.AESUtil;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class Main {
	private static Map<String,String> vpns = new LinkedHashMap<String,String>();
	private static String username;
	private static String password;
	private static String path;
	private static String sysUser;
	
	
	private static File configFile;
	private static String host;
	
	private static final Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
	private static final String PRIMARY_KEY = "";
	
	private static CookieStore cookieStore = null;
	private static HttpClientContext context = null;
	
	public static void main(String[] args) throws Exception {
		if(args.length == 0){
			System.out.println("nydus host is null");
			System.exit(0);
		}
		host = args[0];
		//host = "www.nydusnew.com"; 
		config();
		process(args);
	}
	
	public static void getDatas() throws IOException{
		cookieStore = new BasicCookieStore();
		context = HttpClientContext.create();
	    Registry<CookieSpecProvider> registry = RegistryBuilder.<CookieSpecProvider> create().register(CookieSpecs.DEFAULT, new DefaultCookieSpecProvider()).build();
	    context.setCookieSpecRegistry(registry);
	    context.setCookieStore(cookieStore);
	    
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();  
        formparams.add(new BasicNameValuePair("username", username));  
        formparams.add(new BasicNameValuePair("password", password));
        
		HttpClient client = HttpClients.createDefault();
		
		HttpGet get = new HttpGet("http://"+ host + "/login");
		get.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		get.addHeader("Accept-Encoding","gzip, deflate, sdch");
		get.addHeader("Accept-Language","zh-CN,zh;q=0.8");
		get.addHeader("Cache-Control","no-cache");
		get.addHeader("Connection","keep-alive");
		get.addHeader("Host",host);
		get.addHeader("Pragma","no-cache");
		get.addHeader("Upgrade-Insecure-Requests","1");
		get.addHeader("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
		
		HttpResponse response = client.execute(get, context);
		
		get = new HttpGet("http://"+host+"/code");
		get.addHeader("Accept","image/webp,image/*,*/*;q=0.8");
		get.addHeader("Accept-Encoding","gzip, deflate, sdch");
		get.addHeader("Accept-Language","zh-CN,zh;q=0.8");
		get.addHeader("Cache-Control","no-cache");
		get.addHeader("Connection","keep-alive");
		get.addHeader("Host",host);
		get.addHeader("Pragma","no-cache");
		get.addHeader("Referer","http://"+host+"/login");
		get.addHeader("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
		response = client.execute(get, context);
		File tempImg = new File(System.getProperty("java.io.tmpdir")+"/"+ UUID.randomUUID().toString()+".png");
		IOUtils.copy(response.getEntity().getContent(), new FileOutputStream(tempImg));

		Runtime.getRuntime().exec("display "+tempImg.getAbsolutePath());
		
		HttpPost post = new HttpPost("http://" + host + "/ajlogin");
		post.addHeader("Host",host);
		post.addHeader("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
		post.addHeader("Accept","application/json, text/javascript, */*; q=0.01");
		post.addHeader("Accept-Language","en-US,en;q=0.5");
		post.addHeader("Accept-Encoding","gzip, deflate");
		post.addHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
		post.addHeader("X-Requested-With","XMLHttpRequest");
		post.addHeader("Referer","http://"+host+"/login");
		post.addHeader("Connection","keep-alive");
		post.addHeader("Pragma","no-cache");
		post.addHeader("Cache-Control","no-cache");
		
		System.out.println("Vcode:");
        String code = new BufferedReader(new InputStreamReader(System.in)).readLine();
        formparams.add(new BasicNameValuePair("code", code));  
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        post.setEntity(entity);
        response = client.execute(post , context);
        StringWriter res = new StringWriter();
        IOUtils.copy(response.getEntity().getContent(), res);
        if("1000".equals(JSONObject.parseObject(res.toString()).get("code"))){
        	Connection connect = Jsoup.connect("http://"+host+"/serverlist");
        	for(Cookie cookie: cookieStore.getCookies()){
        		connect.cookie(cookie.getName(), cookie.getValue());        		
        	}
        	Elements elements = connect.execute().parse().getElementsByClass("account_body").get(0).getElementsByClass("td_left");
        	for(Element element: elements){
        		Element id = element.nextElementSibling().nextElementSibling();
			vpns.put(id.text().trim().replace("[VIP]", "").replace("-", "").replaceAll(" ", "").trim(), id.nextElementSibling().text().trim());
        	}
        }
        FileUtils.deleteQuietly(tempImg);
	}
	
	private static void config() throws IOException{
		configuration.setLocalizedLookup(false);
		configuration.setTemplateLoader(new ClassTemplateLoader(Main.class, ""));

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		Main.path = readField(reader, "Base path" , System.getProperty("user.dir"));
		StringWriter w = new StringWriter();
		IOUtils.copy(Runtime.getRuntime().exec("whoami").getInputStream(), w);		
		sysUser = w.toString().replace("\n", "");
		
		configFile = new File(path+"/.config");
		String configStr = null;
		if(configFile.exists()) {
			configStr = FileUtils.readFileToString(configFile, "UTF-8");
			if(StringUtils.isNotBlank(configStr)) {
				try{
					configStr = AESUtil.decrypt(configStr,PRIMARY_KEY);
					JSONObject config = JSONObject.parseObject(configStr);
					Main.username = config.getString("username");
					Main.password = config.getString("password");
					Main.sysUser = config.getString("sysUser");
				} catch (Exception e) {
					
				}
			}
		}
		Main.username = readField(reader, "Nydus Username" , Main.username);
		
		Console console = System.console();
		Main.password = readPassword(console == null ? reader : console, "Nydus Password", Main.password);	

		Main.sysUser = readField(reader, "System User", sysUser);
	}
	
	public static void process(String[] args) throws Exception {
		//String dataPath = path +"/data";
		
		File dir = new File(path+"/vpns");
		FileUtils.deleteQuietly(dir);
		dir.mkdirs();
		String path = dir.getAbsolutePath();
		if(path.endsWith("/")){
			path = path.substring(0, path.length()-1);
		}
		Template template = configuration.getTemplate("template.ftl");
		
		/*List<String> lines = FileUtils.readLines(new File(dataPath));
		for (String line : lines) {
			if(StringUtils.isBlank(line)){
				continue;
			}
			String[] parts = line.split("\t");
			vpns.put(parts[2].replace(" ", "").replace("[VIP]", "").replace("-", "").trim(),parts[3].trim());
		}*/
		getDatas();
		JSONObject config = new JSONObject();
		config.put("sysUser",sysUser);
		config.put("username",username);
		config.put("password",password);
		for (Entry<String, String> vpn : vpns.entrySet()) {
			config.put("id",vpn.getKey());
			config.put("uuid",UUID.randomUUID().toString().toLowerCase());
			config.put("host", vpn.getValue());
			File file = new File(path+"/"+vpn.getKey());
			FileWriter out = new FileWriter(file);
			template.process(config, out);
			out.flush();
			out.close();
			Runtime.getRuntime().exec("chmod 600 "+ file.getAbsolutePath());
		}
		
		FileOutputStream configOutput = new FileOutputStream(configFile);
		config.remove("id");
		config.remove("uuid");
		config.remove("host");
		
		IOUtils.write(AESUtil.encrypt(config.toJSONString(),PRIMARY_KEY), configOutput);
		configOutput.flush();
		
		File sh = new File(Main.path + "/install.sh");
		FileOutputStream output = new FileOutputStream(sh);
		IOUtils.write("#!/bin/sh\ncp -rf " + path + "/* /etc/NetworkManager/system-connections\nsystemctl restart NetworkManager", output);
		output.flush();
		IOUtils.closeQuietly(output);
		
		Runtime.getRuntime().exec("chmod 755 "+ sh.getAbsolutePath());
		System.out.println("sudo " + sh.getAbsolutePath());
	}
	
	private static String readField(BufferedReader reader, String title, String defaultVal) throws IOException{
		StringBuilder sb = new StringBuilder(title);
		if(null != defaultVal) {
			sb.append("[").append(defaultVal).append("]");
		}
		sb.append(":\n");
		System.out.print(sb.toString());
		String line = reader.readLine();
		if(StringUtils.isNotBlank(line)) {
			return line.trim();
		}
		return defaultVal;
	}
	
	private static String readPassword(Object reader, String title, String defaultVal) throws IOException{
		String line = null;
		StringBuilder sb = new StringBuilder(title);
		if(null != defaultVal) {
			sb.append("[{read from config}]");
		}
		sb.append(":\n");
		if(reader instanceof BufferedReader) {
			System.out.print(sb.toString());
			line = ((BufferedReader) reader).readLine();
		} else {
			line = new String(((Console) reader).readPassword(sb.toString()));	
		}
		if(StringUtils.isNotBlank(line)) {
			return line.trim();
		}
		return defaultVal;
	}
}
