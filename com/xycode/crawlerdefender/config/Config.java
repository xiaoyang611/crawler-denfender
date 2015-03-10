package com.xycode.crawlerdefender.config;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Config {
	
	private static String configFilePath="crawler-defender.xml";
	
	private ConfigItem configItem=new ConfigItem();
	
	private static volatile Config instance;
	
	private Config(){
		try {
			this.parseConfigFile();
		} catch (Exception e) {
			throw new RuntimeException("parse config file  occured  an  error£¡",e);
		}
	}
	
	public static Config getInstance(){
		if(instance==null){
			synchronized (Config.class) {
				if(instance==null){
					instance=new Config();
				}
			}
		}
		return instance;
	}
	
	/*protected Config(String configFilePath){
		this.configFilePath=configFilePath;
		if(configFilePath.contains("classpath:")) configFilePath=configFilePath.replace("classpath:", "");
		try {
			this.parseConfigFile();
		} catch (Exception e) {
			throw new RuntimeException("parse config file  occured  an  error£¡",e);
		}
	}*/
	
	private void parseConfigFile() throws Exception{
		
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder=factory.newDocumentBuilder();
		Document doc=builder.parse(this.getClass().getClassLoader().getResourceAsStream(Config.configFilePath));
		
		XPathFactory xpfactory = XPathFactory.newInstance();
		XPath  path = xpfactory.newXPath();
		
	    NodeList thresholdNodes=(NodeList) path.evaluate("//block-validate/thresholds/threshold", doc, XPathConstants.NODESET);
	    Node thresholdNode;
	    String level;
	    int duration,value;
	    
	    for(int i=0,len=thresholdNodes.getLength();i<len;i++){
	    	thresholdNode=thresholdNodes.item(i);
	    	level=path.evaluate("@level", thresholdNode);
	    	duration=Integer.valueOf(path.evaluate("@duration", thresholdNode));
	    	value=Integer.valueOf(path.evaluate("@value", thresholdNode));
	    	configItem.getThresholds().add(configItem.new Threshold(level, duration, value));
	    }
	    
	    NodeList ipWhiteItemNodes=(NodeList) path.evaluate("//ipWhiteList/item", doc, XPathConstants.NODESET);
	    
	    for(int i=0,len=ipWhiteItemNodes.getLength();i<len;i++){
	    	configItem.getIpWhiteList().add(ipWhiteItemNodes.item(i).getTextContent());
	    }
	    
	    NodeList ipBlackItemNodes=(NodeList) path.evaluate("//ipBlackList/item", doc, XPathConstants.NODESET);
	    
	    for(int i=0,len=ipBlackItemNodes.getLength();i<len;i++){
	    	configItem.getIpBlackList().add(ipBlackItemNodes.item(i).getTextContent());
	    }
	    
	    NodeList userAgentWhiteItemNodes=(NodeList) path.evaluate("//userAgentWhiteList/item", doc, XPathConstants.NODESET);
	    
	    for(int i=0,len=userAgentWhiteItemNodes.getLength();i<len;i++){
	    	configItem.getUserAgentWhiteList().add(userAgentWhiteItemNodes.item(i).getTextContent());
	    }
	    
	    NodeList userAgentBlackItemNodes=(NodeList) path.evaluate("//userAgentBlackList/item", doc, XPathConstants.NODESET);
	    
	    for(int i=0,len=userAgentBlackItemNodes.getLength();i<len;i++){
	    	configItem.getUserAgentBlackList().add(userAgentBlackItemNodes.item(i).getTextContent());
	    }
	    
	    Node mongoNode=(Node)path.evaluate("//persistence/mongo", doc, XPathConstants.NODE);
	    configItem.getMongoConfig().setServerAddress(path.evaluate("./serverAddress", mongoNode));
	    configItem.getMongoConfig().setDbname(path.evaluate("./dbname", mongoNode));
	    configItem.getMongoConfig().setUsername(path.evaluate("./username", mongoNode));
	    configItem.getMongoConfig().setPassword(path.evaluate("./password", mongoNode));
	    
	    configItem.setBotTrap((Boolean)path.evaluate("//bot-trap", doc,XPathConstants.BOOLEAN));
	}

	public static void setConfigFilePath(String configFilePath) {
		if(configFilePath.contains("classpath:")){
			Config.configFilePath=configFilePath.replace("classpath:", "");
		}
	}

	public ConfigItem getConfigItem() {
		return configItem;
	}

	public void setConfigItem(ConfigItem configItem) {
		this.configItem = configItem;
	}
	
	
	public static void main(String[] args) {
		Config c=new Config();
	}
}

